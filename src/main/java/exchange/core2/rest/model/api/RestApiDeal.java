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

@Getter
public final class RestApiDeal {

    private final String price;
    private final String size;
    private final DealParty party;


// TODO add more fields

    @JsonCreator
    @Builder
    public RestApiDeal(
            @JsonProperty("price") String price,
            @JsonProperty("size") String size,
            @JsonProperty("party") DealParty party) {

        this.price = price;
        this.size = size;
        this.party = party;
    }
}
