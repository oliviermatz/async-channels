package io.github.matzoliv.asyncchannel.java;

import io.github.matzoliv.asyncchannel.AsyncChannel;
import io.github.matzoliv.asyncchannel.Channels;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Test {
    public static CompletableFuture<Void> pingPongLoop(String id, int loopLeft, AsyncChannel in, AsyncChannel out) {
        return in.readAsync()
                .thenComposeAsync(msg -> {
                    System.out.println(String.format("Task %s: received %s, left %d", id, msg, loopLeft));
                    if (msg.equals("ping")) {
                        return out.putAsync("pong");
                    } else {
                        return out.putAsync("ping");
                    }
                })
                .thenComposeAsync((Void x) -> {
                    if (loopLeft > 1) {
                        return pingPongLoop(id, loopLeft - 1, in, out);
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        c1.putAsync("ping");

        CompletableFuture.anyOf(
            pingPongLoop("A", 100, c1, c2),
            pingPongLoop("B", 100, c2, c1)
        ).get();
    }
}