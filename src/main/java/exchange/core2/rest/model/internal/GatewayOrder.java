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

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.rest.model.api.OrderState;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class GatewayOrder {

    private final long orderId;

    private final int userCookie;

    private final BigDecimal price;
    private final long size;
    private final OrderAction action;
    private final OrderType orderType;

    // mutable fields

    private final List<GatewayDeal> deals = new ArrayList<>();

    @Setter
    private long filled;

    @Setter
    private OrderState state;

}
