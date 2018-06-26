//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.*;
import io.github.matzoliv.asyncchannel.implementation.results.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AsyncChannelImpl implements AsyncChannel {
    private Channel underlying;
    private AsyncReadPort underlyingReadPort;
    private AsyncWritePort underlyingWritePort;

    public AsyncChannelImpl(Channel underlying) {
        this.underlying = underlying;
        this.underlyingReadPort = new AsyncReadPortImpl(underlying);
        this.underlyingWritePort = new AsyncWritePortImpl(underlying);
    }

    @Override
    public void close() {
        underlying.close();
    }

    @Override
    public TakeResult take(Handler<Object> handler) {
        return underlying.take(handler);
    }

    @Override
    public PutResult put(Object value, Handler<Object> handler) {
        return underlying.put(value, handler);
    }

    @Override
    public ReadPort putAlts(Object value) {
        return underlying.putAlts(value);
    }

    @Override
    public CompletableFuture<Object> readAsync() {
        return underlyingReadPort.readAsync();
    }

    @Override
    public Object poll() { return underlyingReadPort.poll(); }

    @Override
    public CompletableFuture<Void> putAsync(Object value) {
        return underlyingWritePort.putAsync(value);
    }

    @Override
    public boolean offer(Object value) { return underlyingWritePort.offer(value); }
}
