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

import java.util.List;

@Getter
public final class RestApiExchangeInfo {

    public final RestApiTime serverTime;
    public final List<RestApiAsset> assets;
    public final List<RestApiSymbol> symbols;

    @JsonCreator
    @Builder
    public RestApiExchangeInfo(
            @JsonProperty("serverTime") RestApiTime serverTime,
            @JsonProperty("assets") List<RestApiAsset> assets,
            @JsonProperty("symbols") List<RestApiSymbol> symbols) {

        this.serverTime = serverTime;
        this.assets = assets;
        this.symbols = symbols;
    }

}
