//   Copyright (c) Olivier Matz. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.java;

public class Choice1Of2<A, B> extends Choice2<A, B> {
    private A value;

    public Choice1Of2(A value) {
        this.value = value;
    }

    public A getValue() {
        return value;
    }
}
