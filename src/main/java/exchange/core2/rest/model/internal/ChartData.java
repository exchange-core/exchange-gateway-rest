/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.rest.model.internal;

import exchange.core2.rest.model.api.TimeFrame;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


@NoArgsConstructor
public class ChartData {

    private final BarsData bars = new BarsData(TimeFrame.M1);

    private final LinkedBlockingQueue<TickRecord> ticksQueue = new LinkedBlockingQueue<>();

    private volatile boolean flushingTicks = false;

    /**
     * Thread safe
     */
    public void addTicks(List<TickRecord> ticksToAdd) {
        ticksQueue.addAll(ticksToAdd);
    }

    /**
     * Thread safe
     */
    public List<GatewayBarStatic> getBarsData(int barsNum, TimeFrame timeFrame) {

        if (!ticksQueue.isEmpty() && !flushingTicks) {
            // flush ticks queue if needed
            flushingTicks = true;
            synchronized (bars) {
                ticksQueue.forEach(tick -> bars.addTick(tick.getPrice(), tick.getSize(), tick.getTimestamp()));
            }
            flushingTicks = false;
        }

        return bars.getRecentBars(barsNum);
    }
}
