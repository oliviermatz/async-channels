package io.github.matzoliv.asyncchannel.java;

import io.github.matzoliv.asyncchannel.AsyncChannel;
import io.github.matzoliv.asyncchannel.Channels;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Executors {
    @Test
    public void testPutContinuationInSameThread() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("testThread");

        CompletableFuture<Object> putFuture = c.putAsync("hello1").thenApply(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        c.readAsync();
        threadLocal.set("afterReadAsync");

        String result = (String)putFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, "beforeReadAsync");
    }

    @Test
    public void testPutAsyncContinuationYields() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("testThread");

        CompletableFuture<Object> putFuture = c.putAsync("hello1").thenApplyAsync(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        c.readAsync();
        threadLocal.set("afterReadAsync");

        String result = (String)putFuture.get(1, TimeUnit.SECONDS);
        Assert.assertNull(result);
    }

    @Test
    public void testReadContinuationInSameThread() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("testThread");

        CompletableFuture<Object> readFuture = c.readAsync().thenApply(x ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        c.putAsync("hello");
        threadLocal.set("afterReadAsync");

        String result = (String)readFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, "beforeReadAsync");
    }

    @Test
    public void testReadAsyncContinuationYields() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("testThread");

        CompletableFuture<Object> readFuture = c.readAsync().thenApplyAsync(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        c.putAsync("hello");
        threadLocal.set("afterReadAsync");

        String result = (String)readFuture.get(1, TimeUnit.SECONDS);
        Assert.assertNull(result);
    }
}
