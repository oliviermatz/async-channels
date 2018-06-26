//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import java.util.function.Consumer;

public class CommitResultPairWithValue {
    private Consumer<Object> left;
    private Consumer<Object> right;
    private Object value;

    public CommitResultPairWithValue(Consumer<Object> left, Consumer<Object> right, Object value) {
        this.left = left;
        this.right = right;
        this.value = value;
    }

    public Consumer<Object> getLeft() {
        return left;
    }

    public Consumer<Object> getRight() {
        return right;
    }

    public Object getValue() {
        return value;
    }
}
