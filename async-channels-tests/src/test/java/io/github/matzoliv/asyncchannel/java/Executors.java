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
        ThreadLocal threadLocal = new ThreadLocal<String>();
        threadLocal.set("testThread");

        CompletableFuture<Object> putFuture = c.putAsync("hello1").thenApply(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        CompletableFuture<Object> readFuture = c.readAsync();
        threadLocal.set("afterReadAsync");

        String result = (String)putFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, "beforeReadAsync");
    }

    @Test
    public void testPutAsyncContinuationYields() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal threadLocal = new ThreadLocal<String>();
        threadLocal.set("testThread");

        CompletableFuture<Object> putFuture = c.putAsync("hello1").thenApplyAsync(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        CompletableFuture<Object> readFuture = c.readAsync();
        threadLocal.set("afterReadAsync");

        String result = (String)putFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, null);
    }

    @Test
    public void testReadContinuationInSameThread() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal threadLocal = new ThreadLocal<String>();
        threadLocal.set("testThread");

        CompletableFuture<Object> readFuture = c.readAsync().thenApply(x ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        CompletableFuture<Void> putFuture = c.putAsync("hello");
        threadLocal.set("afterReadAsync");

        String result = (String)readFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, "beforeReadAsync");
    }

    @Test
    public void testReadAsyncContinuationYields() throws InterruptedException, ExecutionException, TimeoutException {
        AsyncChannel c = Channels.create();
        ThreadLocal threadLocal = new ThreadLocal<String>();
        threadLocal.set("testThread");

        CompletableFuture<Object> readFuture = c.readAsync().thenApplyAsync(v ->
            threadLocal.get()
        );

        threadLocal.set("beforeReadAsync");
        CompletableFuture<Void> putFuture = c.putAsync("hello");
        threadLocal.set("afterReadAsync");

        String result = (String)readFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(result, null);
    }
}
