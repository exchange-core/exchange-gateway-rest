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

@ToString
@Getter
public class RestApiAsset {

    private final String assetCode;
    private final int scale; // asset scale - normally 2 for currencies, 8 for BTC, etc

    @JsonCreator
    @Builder
    public RestApiAsset(@JsonProperty("assetCode") String assetCode,
                        @JsonProperty("scale") int scale) {
        this.assetCode = assetCode;
        this.scale = scale;
    }
}
