//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.java;

import io.github.matzoliv.asyncchannel.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public class PutRead {
    @Test
    public void testUnbufferedPutRead() throws ExecutionException, InterruptedException, TimeoutException {
        AsyncChannel c = Channels.create();

        CompletableFuture<Void> putFuture = c.putAsync("hello");
        Thread.sleep(25);
        Assert.assertFalse(putFuture.isDone());

        CompletableFuture<Object> readFuture = c.readAsync();

        CompletableFuture.allOf(putFuture, readFuture).get(1, TimeUnit.SECONDS);

        Assert.assertTrue(putFuture.isDone());
        Assert.assertTrue(readFuture.isDone());
        Assert.assertEquals(readFuture.get(), "hello");
    }

    @Test
    public void testUnbufferedReadPut() throws ExecutionException, InterruptedException, TimeoutException {
        AsyncChannel c = Channels.create();

        CompletableFuture<Object> readFuture = c.readAsync();
        Thread.sleep(25);
        Assert.assertFalse(readFuture.isDone());

        CompletableFuture<Void> putFuture = c.putAsync("hello");

        CompletableFuture.allOf(putFuture, readFuture).get(1, TimeUnit.SECONDS);

        Assert.assertTrue(putFuture.isDone());
        Assert.assertTrue(readFuture.isDone());
        Assert.assertEquals(readFuture.get(), "hello");
    }

    @Test
    public void testBufferedPutsRead() throws ExecutionException, InterruptedException, TimeoutException {
        AsyncChannel c = Channels.create(1);

        CompletableFuture<Void> put1Future = c.putAsync("hello1");
        Assert.assertTrue(put1Future.isDone());

        CompletableFuture<Void> put2Future = c.putAsync("hello2");
        Thread.sleep(25);
        Assert.assertFalse(put2Future.isDone());

        CompletableFuture<Object> read1Future = c.readAsync();

        CompletableFuture.allOf(put2Future, read1Future).get(1, TimeUnit.SECONDS);

        Assert.assertTrue(put2Future.isDone());
        Assert.assertTrue(read1Future.isDone());

        Assert.assertEquals(read1Future.get(), "hello1");

        CompletableFuture<Object> read2Future = c.readAsync();
        Assert.assertEquals(read2Future.get(), "hello2");
    }

    @Test
    public void testPollOffer() {
        AsyncChannel c1 = Channels.create(10);

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(c1.offer(new Integer(i)));
        }
        Assert.assertFalse(c1.offer(new Integer(10)));

        Assert.assertEquals(c1.poll(), new Integer(0));

        Assert.assertTrue(c1.offer(new Integer(10)));
        Assert.assertFalse(c1.offer(new Integer(11)));

        for (int i = 1; i < 11; i++) {
            Assert.assertEquals(c1.poll(), new Integer(i));
        }

        Assert.assertEquals(c1.poll(), null);
        Assert.assertTrue(c1.offer(new Integer(11)));
        Assert.assertEquals(c1.poll(), new Integer(11));
        Assert.assertEquals(c1.poll(), null);
    }

    public CompletableFuture<Void> pingPongLoop(String id, AsyncChannel in, AsyncChannel out) {
        return in.readAsync()
                .thenComposeAsync(msg -> {
                    System.out.println(String.format("Task %s: received %s", id, msg));
                    return out.putAsync(msg);
                })
                .thenComposeAsync((Void x) -> pingPongLoop(id, in, out));
    }

    @Test
    public void testPingPong() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        CompletableFuture.allOf(
                pingPongLoop("A", c1, c2),
                pingPongLoop("B", c2, c1),
                c1.putAsync("pingPong")
        ).get(120, TimeUnit.SECONDS);
    }
}
