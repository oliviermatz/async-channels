//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.Handler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ConsumerHandler<T> extends ReentrantLock implements Handler<T> {
    private static AtomicLong nextId = new AtomicLong(0);

    private boolean active;
    private long lockId;
    private Consumer<T> callback;
    private boolean blockable;

    public ConsumerHandler(Consumer<T> callback) {
        this(callback, true);
    }

    public ConsumerHandler(Consumer<T> callback, boolean blockable)  {
        active = true;
        lockId = nextId.getAndIncrement();
        this.callback = callback;
        this.blockable = blockable;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Consumer<T> commit() {
        if (active) {
            active = false;
            return callback;
        } else {
            return null;
        }
    }

    @Override
    public long getLockId() {
        return lockId;
    }

    @Override
    public boolean isBlockable() {
        return blockable;
    }
}
