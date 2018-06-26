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

public class AsyncReadPortImpl implements AsyncReadPort {
    private ReadPort underlying;

    public AsyncReadPortImpl(ReadPort underlying) {
        this.underlying = underlying;
    }

    @Override
    public CompletableFuture<Object> readAsync() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        Handler handler = new ConsumerHandler(x -> future.complete(x));
        TakeResult result = take(handler);
        if (result instanceof TakeSucceeded) {
            return CompletableFuture.completedFuture(((TakeSucceeded) result).getResult());
        } else {
            return future;
        }
    }

    @Override
    public Object poll() {
        Handler handler = new ConsumerHandler(x -> {}, false);
        TakeResult result = take(handler);
        if (result instanceof TakeSucceeded) {
            return ((TakeSucceeded) result).getResult();
        } else {
            return null;
        }
    }

    @Override
    public TakeResult take(Handler handler) {
        return underlying.take(handler);
    }
}
