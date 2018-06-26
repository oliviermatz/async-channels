//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import io.github.matzoliv.asyncchannel.Channel;
import io.github.matzoliv.asyncchannel.QueueBuffer;
import io.github.matzoliv.asyncchannel.ReadPort;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;

public class Timers {

    private static DelayQueue<TimeoutQueueEntry> timeoutsQueue = new DelayQueue<>();
    private static ConcurrentSkipListMap<Long, TimeoutQueueEntry> timeoutsMap = new ConcurrentSkipListMap<>();

    private static long TIMEOUTS_RESOLUTION_MS = 10;

    private static Thread timeoutWorker = createTimeoutWorker();

    private static Thread createTimeoutWorker() {
        Thread t = new TimeoutWorkerThread(timeoutsQueue, timeoutsMap);
        t.setDaemon(true);
        t.start();
        return t;
    }

    public static ReadPort createTimerChannel(long delay) {
        long timeout = delay + System.currentTimeMillis();
        Map.Entry<Long, TimeoutQueueEntry> tqe = timeoutsMap.ceilingEntry(timeout);
        if (tqe != null) {
            if (tqe.getKey() < (timeout + TIMEOUTS_RESOLUTION_MS)) {
                return tqe.getValue().getChannel();
            }
        }

        Channel c = new ManyToManyChannel(new QueueBuffer(0));
        TimeoutQueueEntry newTqe = new TimeoutQueueEntry(c, timeout);
        timeoutsMap.put(timeout, newTqe);
        timeoutsQueue.put(newTqe);
        return c;
    }

}
