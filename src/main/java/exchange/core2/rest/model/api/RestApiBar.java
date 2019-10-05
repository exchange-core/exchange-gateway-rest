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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Getter
public class RestApiBar {

    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long volume;
    //    private final int index;
    private final long timestamp;

    @JsonCreator
    @Builder
    public RestApiBar(
            @JsonProperty("open") BigDecimal open,
            @JsonProperty("high") BigDecimal high,
            @JsonProperty("low") BigDecimal low,
            @JsonProperty("close") BigDecimal close,
            @JsonProperty("volume") long volume,
//            @JsonProperty("index") int index,
            @JsonProperty("timestamp") long timestamp) {

        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
//        this.index = index;
        this.timestamp = timestamp;
    }
}
