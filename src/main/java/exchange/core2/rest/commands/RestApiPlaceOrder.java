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
package exchange.core2.rest.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class RestApiPlaceOrder {

    private final BigDecimal price;
    private final long size; // only integer sizes allowed

    private final int userCookie;
    private final OrderAction action;
    private final OrderType orderType;

    @JsonCreator
    public RestApiPlaceOrder(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") long size,
            @JsonProperty("userCookie") int userCookie,
            @JsonProperty("action") OrderAction action,
            @JsonProperty("orderType") OrderType orderType) {

        this.price = price;
        this.size = size;
        this.userCookie = userCookie;
        this.action = action;
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "[ADD " + (action == OrderAction.ASK ? 'A' : 'B') + orderType
                + price + ":" + size + "]";
    }
}
