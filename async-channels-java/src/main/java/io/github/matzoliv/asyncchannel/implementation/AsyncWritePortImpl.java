//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import java.util.concurrent.CompletableFuture;

import io.github.matzoliv.asyncchannel.*;
import io.github.matzoliv.asyncchannel.implementation.results.*;

public class AsyncWritePortImpl implements AsyncWritePort {
    private WritePort underlying;

    public AsyncWritePortImpl(WritePort underlying) {
        this.underlying = underlying;
    }

    @Override
    public CompletableFuture<Void> putAsync(Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Handler<Object> handler = new ConsumerHandler<>(x -> future.complete(null));
        PutResult result = put(value, handler);
        if (result instanceof PutParked) {
            return future;
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public boolean offer(Object value) {
        Handler<Object> handler = new ConsumerHandler<>(x -> {}, false);
        PutResult result = put(value, handler);
        return (result instanceof PutSucceeded);
    }

    @Override
    public PutResult put(Object value, Handler<Object> handler) {
        return underlying.put(value, handler);
    }

    @Override
    public ReadPort putAlts(Object value) {
        return underlying.putAlts(value);
    }
}
