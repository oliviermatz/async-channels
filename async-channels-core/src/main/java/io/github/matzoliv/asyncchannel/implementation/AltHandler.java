//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import java.util.function.Consumer;

import io.github.matzoliv.asyncchannel.Handler;

public class AltHandler<T> implements Handler<T> {

    private final Consumer<T> callback;
    private Handler underlying;

    public AltHandler(Handler underlying, Consumer<T> callback) {
        this.underlying = underlying;
        this.callback = callback;
    }

    @Override
    public boolean isActive() {
        return underlying.isActive();
    }

    @Override
    public void lock() {
        underlying.lock();
    }

    @Override
    public void unlock() {
        underlying.unlock();
    }

    @Override
    public Consumer<T> commit() {
        if (underlying.commit() != null) {
            return callback;
        } else {
            return null;
        }
    }

    @Override
    public long getLockId() {
        return underlying.getLockId();
    }

    @Override
    public boolean isBlockable() { return underlying.isBlockable(); }
}
