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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Timers {
    @Test
    public void testTimers() throws ExecutionException, InterruptedException {
        AsyncReadPort c = Channels.timeout(Duration.ofMillis(300L));

        long before = System.currentTimeMillis();
        CompletableFuture<Object> readFuture = c.readAsync();
        readFuture.get();
        long after = System.currentTimeMillis();

        Assert.assertTrue(String.format("Expected 300ms. lapse, got %d", Math.abs(after - before)),Math.abs((after - before) - 300) < 50);
    }
}
