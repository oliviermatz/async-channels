//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.Channel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TimeoutQueueEntry implements Delayed {
    private Channel channel;
    private long timestamp;

    public TimeoutQueueEntry(Channel channel, long timestamp) {
        this.channel = channel;
        this.timestamp = timestamp;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(timestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long oTimestamp = ((TimeoutQueueEntry)o).timestamp;
        return timestamp < oTimestamp ? -1 : timestamp == oTimestamp ? 0 : 1;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
