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
package exchange.core2.rest.model.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Wither
@EqualsAndHashCode
public final class RestApiOrderBook {

    //private final long timestamp;

    private final String symbol;

    private final List<BigDecimal> askPrices;
    private final List<Long> askVolumes;
    private final List<BigDecimal> bidPrices;
    private final List<Long> bidVolumes;

    @JsonCreator
    @Builder
    public RestApiOrderBook(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("askPrices") List<BigDecimal> askPrices,
            @JsonProperty("askVolumes") List<Long> askVolumes,
            @JsonProperty("bidPrices") List<BigDecimal> bidPrices,
            @JsonProperty("bidVolumes") List<Long> bidVolumes) {

        this.symbol = symbol;

        this.askPrices = askPrices;
        this.askVolumes = askVolumes;
        this.bidPrices = bidPrices;
        this.bidVolumes = bidVolumes;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return askPrices.isEmpty() && askVolumes.isEmpty() && bidPrices.isEmpty() && bidVolumes.isEmpty();
    }

    @Override
    public String toString() {
        return "RestApiOrderBook{" +
                "symbol='" + symbol + '\'' +
                ", askPrices=" + askPrices +
                ", askVolumes=" + askVolumes +
                ", bidPrices=" + bidPrices +
                ", bidVolumes=" + bidVolumes +
                '}';
    }
}
