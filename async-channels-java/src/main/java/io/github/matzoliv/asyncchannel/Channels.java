//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.github.matzoliv.asyncchannel.implementation.*;
import io.github.matzoliv.asyncchannel.implementation.results.*;

public class Channels {

    public static AsyncChannel create(Channel underlying) { return new AsyncChannelImpl(underlying); }

    public static AsyncChannel create(Buffer buffer) {
        return new AsyncChannelImpl(new ManyToManyChannel(buffer));
    }

    public static AsyncChannel create(int bufferSize) {
        return create(new QueueBuffer(bufferSize));
    }

    public static AsyncChannel create() {
        return create(0);
    }

    public static AsyncReadPort timeout(Duration duration) {
        return new AsyncReadPortImpl(Timers.createTimerChannel(duration.toMillis()));
    }

    public static CompletableFuture alts(ReadPortAndWrapper... portsAndWrapper) {
        CompletableFuture future = new CompletableFuture<>();

        for (ReadPortAndWrapper x : portsAndWrapper) {
            ReadPort port = x.getReadPort();
            Function<Object, Object> wrapper = x.getWrapper();

            Handler handler = new ConsumerHandler(arg -> {});
            TakeResult result = port.take(new AltHandler(handler, arg -> future.complete(wrapper.apply(arg))));

            if (result instanceof TakeSucceeded) {
                return CompletableFuture.completedFuture(wrapper.apply(((TakeSucceeded) result).getResult()));
            }
        }
        return future;
    }
}
