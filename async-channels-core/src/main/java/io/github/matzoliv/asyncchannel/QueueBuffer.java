//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel;

import java.util.LinkedList;
import java.util.Queue;

public class QueueBuffer implements Buffer {
    private int maxSize;
    private Queue queue;

    public QueueBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedList();
    }

    @Override
    public boolean isFull() {
        return queue.size() >= maxSize;
    }

    @Override
    public boolean isEmpty() {
        return queue.size() == 0;
    }

    @Override
    public void enqueue(Object value) {
        queue.add(value);
    }

    @Override
    public Object dequeue() {
        return queue.remove();
    }
}
