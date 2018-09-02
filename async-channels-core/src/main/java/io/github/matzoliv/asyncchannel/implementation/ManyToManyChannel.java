//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.*;
import io.github.matzoliv.asyncchannel.implementation.results.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ManyToManyChannel implements Channel {

    private Buffer buffer;
    private LinkedList<Putter> puts;
    private LinkedList<Handler> takes;
    private ReentrantLock lock;
    private boolean isClosed;

    public ManyToManyChannel(Buffer buffer)  {
        this.buffer = buffer;

        puts = new LinkedList<>();
        takes = new LinkedList<>();
        lock = new ReentrantLock();
        isClosed = false;
    }

    private void cleanup() {
        assert(lock.isHeldByCurrentThread());

        if (!takes.isEmpty()) {
            Iterator<Handler> it = takes.iterator();
            while (it.hasNext()) {
                if (!it.next().isActive()) {
                    it.remove();
                }
            }
        }

        if (!puts.isEmpty()) {
            Iterator<Putter> it = puts.iterator();
            while (it.hasNext()) {
                if (!it.next().getHandler().isActive()) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void close() {
        lock.lock();
        cleanup();
        if (isClosed) {
            lock.unlock();
        } else {
            isClosed = true;

            Iterator<Handler> it = takes.iterator();
            while (it.hasNext()) {
                Handler handler = it.next();
                handler.lock();
                Consumer<Object> consumer = handler.commit();
                handler.unlock();
                if (consumer != null) {
                    if (!buffer.isEmpty()) {
                        consumer.accept(buffer.dequeue());
                    } else {
                        consumer.accept(null);
                    }
                }
                it.remove();
            }

            lock.unlock();
        }
    }

    @Override
    public PutResult put(Object value, Handler handler) {
        lock.lock();
        cleanup();
        if (isClosed) {
            lock.unlock();
            return PutFailed.value;
        } else {
            if (!buffer.isFull() && !takes.isEmpty()) {
                handler.lock();
                Consumer<Object> consumer = handler.commit();
                handler.unlock();

                if (consumer != null) {
                    LinkedList<Runnable> takeCallbacks = new LinkedList<>();

                    // Enqueue the value in the buffer, then schedule as many
                    // takers as possible with values from the buffer.
                    buffer.enqueue(value);
                    Iterator<Handler> it = takes.iterator();

                    while (it.hasNext() && !buffer.isEmpty()) {
                        Handler takeHandler = it.next();

                        takeHandler.lock();
                        Consumer<Object> takeConsumer = takeHandler.commit();
                        takeHandler.unlock();

                        if (takeConsumer != null) {
                            Object takenValue = buffer.dequeue();
                            it.remove();
                            takeCallbacks.add(() -> takeConsumer.accept(takenValue));
                        }
                    }

                    lock.unlock();
                    for (Runnable r : takeCallbacks) {
                        r.run();
                    }

                    return PutSucceeded.value;
                } else {
                    lock.unlock();
                    return PutCancelled.value;
                }
            } else {
                // Buffer is full and takers are parked (unbuffered channel case).
                // Try to find an available parked taker to match with the putter.
                Iterator<Handler> it = takes.iterator();

                Runnable result = null;
                while (it.hasNext()) {
                    Handler takeHandler = it.next();

                    // We must lock both the taker and the putter to commit then at the same time.
                    // Do it in a predictable order to avoid deadlocks.
                    if (handler.getLockId() < takeHandler.getLockId()) {
                        handler.lock();
                        takeHandler.lock();
                    } else {
                        takeHandler.lock();
                        handler.lock();
                    }

                    if (handler.isActive() && takeHandler.isActive()) {
                        Consumer<Object> takeConsumer = takeHandler.commit();
                        result = () -> takeConsumer.accept(value);
                    }

                    handler.unlock();
                    takeHandler.unlock();

                    if (result != null) {
                        it.remove();
                        break;
                    }
                }

                if (result != null) {
                    // We found a taker, schedule it and acknowledge the put.
                    lock.unlock();
                    result.run();
                    return PutSucceeded.value;
                } else {
                    if (!buffer.isFull()) {
                        // No taker, and the buffer has available space:
                        // write the value in the buffer if the putter is still active.

                        handler.lock();
                        Consumer<Object> consumer = handler.commit();
                        handler.unlock();

                        if (consumer != null) {
                            buffer.enqueue(value);
                            lock.unlock();
                            return PutSucceeded.value;
                        } else {
                            lock.unlock();
                            return PutCancelled.value;
                        }
                    } else {
                        // No taker, no space available in the buffer:
                        // park the putter if still active.
                        if (handler.isActive() && handler.isBlockable()) {
                            puts.add(new Putter(value, handler));
                            lock.unlock();
                            return PutParked.value;
                        } else {
                            lock.unlock();
                            return PutCancelled.value;
                        }
                    }
                }
            }
        }
    }

    @Override
    public ReadPort putAlts(Object value) {
        return new AltWritePortWrapper(this, value);
    }

    @Override
    public TakeResult take(Handler handler) {
        lock.lock();
        cleanup();

        if (!buffer.isEmpty()) {
            handler.lock();
            Consumer<Object> consumer = handler.commit();
            handler.unlock();

            if (consumer != null) {
                // The taker will receive a value from the buffer immediately.
                Object value = buffer.dequeue();
                Iterator<Putter> it = puts.iterator();

                // Because we just consumed a value from the queue, we must schedule
                // active parked putters & enqueue their values until the buffer is full.
                // Note that we can't predict how many putters can be unparked from the single
                // .dequeue() call.
                LinkedList<Consumer<Object>> putterConsumers = new LinkedList<>();
                while (!buffer.isFull() && it.hasNext()) {
                    Putter putter = it.next();
                    Handler putterHandler = putter.getHandler();

                    putterHandler.lock();
                    Consumer<Object> putterConsumer = putterHandler.commit();
                    putterHandler.unlock();

                    if (putterConsumer != null) {
                        buffer.enqueue(putter.getValue());
                        putterConsumers.add(putterConsumer);
                    }
                }

                lock.unlock();
                for (Consumer<Object> c : putterConsumers) {
                    c.accept(null);
                }
                return new TakeSucceeded(value);
            } else {
                // Taker has been committed somewhere else.
                lock.unlock();
                return TakeCancelled.value;
            }
        } else {
            // Buffer is empty, we'll look for an active parked putter to get a value from.
            // (Usually happens in the non-buffered channel case).
            Iterator<Putter> it = puts.iterator();

            PutCallbackWithValue result = null;
            while (result == null && it.hasNext()) {
                Putter putter = it.next();
                Handler putterHandler = putter.getHandler();

                // We must lock both the taker and the putter to commit then at the same time.
                // Do it in a predictable order to avoid deadlocks.
                if (handler.getLockId() < putterHandler.getLockId()) {
                    handler.lock();
                    putterHandler.lock();
                } else {
                    putterHandler.lock();
                    handler.lock();
                }

                if (handler.isActive() && putterHandler.isActive()) {
                    result = new PutCallbackWithValue(putterHandler.commit(), putter.getValue());
                }

                handler.unlock();
                putterHandler.unlock();

                if (result != null || !putterHandler.isActive()) {
                    it.remove();
                }
            }

            if (result != null) {
                lock.unlock();
                result.getPutCallback().accept(null);
                return new TakeSucceeded(result.getValue());
            } else {
                if (isClosed) {
                    handler.lock();
                    Consumer<Object> consumer = handler.commit();
                    handler.unlock();

                    if (consumer != null) {
                        if (!buffer.isEmpty()) {
                            Object value = buffer.dequeue();
                            lock.unlock();
                            return new TakeSucceeded(value);
                        } else {
                            lock.unlock();
                            return new TakeSucceeded(null);
                        }
                    } else {
                        lock.unlock();
                        return TakeCancelled.value;
                    }
                } else if (handler.isBlockable()) {
                    takes.add(handler);
                    lock.unlock();
                    return TakeParked.value;
                } else {
                    lock.unlock();
                    return TakeCancelled.value;
                }
            }
        }
    }
}
