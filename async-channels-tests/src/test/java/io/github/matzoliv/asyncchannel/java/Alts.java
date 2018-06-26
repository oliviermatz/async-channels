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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Alts {
    @Test
    public void testAltsUnbufferedChoice1() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        c1.putAsync("hello1");

        Choice2<Object, Object> result = future.get();

        Assert.assertTrue(result instanceof Choice1Of2);
    }

    @Test
    public void testAltsUnbufferedChoice2() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        c2.putAsync("hello1");

        Choice2<Object, Object> result = future.get();

        Assert.assertTrue(result instanceof Choice2Of2);
    }

    @Test
    public void testAltsUnbufferedChoice1NoWait() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        c1.putAsync("hello1");

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        Choice2<Object, Object> result = future.get();
        Assert.assertTrue(result instanceof Choice1Of2);
    }

    @Test
    public void testAltsUnbufferedChoice2NoWait() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create();
        AsyncChannel c2 = Channels.create();

        c2.putAsync("hello1");

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        Choice2<Object, Object> result = future.get();
        Assert.assertTrue(result instanceof Choice2Of2);
    }

    @Test
    public void testAltsBufferedChoice1() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create(10);
        AsyncChannel c2 = Channels.create(10);

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        c1.putAsync("hello1");

        Choice2<Object, Object> result = future.get();

        Assert.assertTrue(result instanceof Choice1Of2);
    }

    @Test
    public void testAltsBufferedChoice2() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create(10);
        AsyncChannel c2 = Channels.create(10);

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        c2.putAsync("hello1");

        Choice2<Object, Object> result = future.get();

        Assert.assertTrue(result instanceof Choice2Of2);
    }

    @Test
    public void testAltsBufferedChoice1NoWait() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create(10);
        AsyncChannel c2 = Channels.create(10);

        c1.putAsync("hello1");

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        Choice2<Object, Object> result = future.get();
        Assert.assertTrue(result instanceof Choice1Of2);
    }

    @Test
    public void testAltsBufferedChoice2NoWait() throws ExecutionException, InterruptedException {
        AsyncChannel c1 = Channels.create(10);
        AsyncChannel c2 = Channels.create(10);

        c2.putAsync("hello1");

        CompletableFuture<Choice2<Object, Object>> future = Channels.alts(
            new ReadPortAndWrapper(c1, x -> new Choice1Of2(x)),
            new ReadPortAndWrapper(c2, x -> new Choice2Of2(x))
        );

        Choice2<Object, Object> result = future.get();
        Assert.assertTrue(result instanceof Choice2Of2);
    }
}
