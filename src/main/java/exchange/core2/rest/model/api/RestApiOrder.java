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
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public final class RestApiOrder {

    private final long orderId;

    private final BigDecimal price;
    private final long size;
    private final long filled;

    private final GatewayOrderState state;

    private final int userCookie;

    private final OrderAction action;
    private final OrderType orderType;

    private final String symbol;

    private final List<RestApiDeal> deals;

    // TODO add more fields

    @JsonCreator
    @Builder
    public RestApiOrder(
            @JsonProperty("orderId") long orderId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") long size,
            @JsonProperty("filled") long filled,
            @JsonProperty("userCookie") int userCookie,
            @JsonProperty("state") GatewayOrderState state,
            @JsonProperty("action") OrderAction action,
            @JsonProperty("orderType") OrderType orderType,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("deals") List<RestApiDeal> deals) {

        this.orderId = orderId;
        this.price = price;
        this.size = size;
        this.filled = filled;
        this.userCookie = userCookie;
        this.state = state;
        this.action = action;
        this.orderType = orderType;
        this.symbol = symbol;
        this.deals = deals;
    }
}
