//   Copyright (c) Olivier Matz, Rich Hickey and contributors. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package io.github.matzoliv.asyncchannel.implementation;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;

public class TimeoutWorkerThread extends Thread {

    private DelayQueue<TimeoutQueueEntry> timeoutsQueue;
    private ConcurrentSkipListMap<Long, TimeoutQueueEntry> timeoutsMap;

    public TimeoutWorkerThread(DelayQueue<TimeoutQueueEntry> timeoutsQueue, ConcurrentSkipListMap<Long, TimeoutQueueEntry> timeoutsMap) {
        this.timeoutsQueue = timeoutsQueue;
        this.timeoutsMap = timeoutsMap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeoutQueueEntry tqe = timeoutsQueue.take();
                timeoutsMap.remove(tqe.getTimestamp(), tqe);
                tqe.getChannel().close();
            } catch (InterruptedException e) {

            }
        }
    }
}
