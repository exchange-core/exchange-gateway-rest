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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Not thread safe
 */
public class BarsData {

    private final TimeFrame timeFrame;

    private final List<GatewayBarStatic> bars = new ArrayList<>();

    private long volume = 0;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    private Instant startTimestamp;
    private long endTimestamp;


    public BarsData(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public void addTick(BigDecimal price, long size, long timestamp) {

        if (timestamp < endTimestamp) {
            // just update values

            if (price.compareTo(high) > 0) {
                high = price;
            }
            if (price.compareTo(low) > 0) {
                low = price;
            }
            close = price;
            volume += size;

        } else {

            // time to close last bar and create a new one
            if (size > 0) {
                bars.add(new GatewayBarStatic(open, high, low, close, volume, startTimestamp));
            }

            startTimestamp = Instant.ofEpochSecond(timestamp).truncatedTo(timeFrame.getTruncateUnit());
            endTimestamp = timestamp + timeFrame.getDuration().toMillis();

            open = price;
            high = price;
            low = price;
            close = price;
            volume = size;
        }
    }

    public List<GatewayBarStatic> getRecentBars(int numBars) {
        final int size = bars.size();
        final int startFrom = Math.max(0, size - numBars);
        List<GatewayBarStatic> result = new ArrayList<>(Math.min(size, numBars));
        for (int i = startFrom; i < size; i++) {
            result.add(bars.get(i));
        }
        return result;
    }


}
