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
package exchange.core2.rest.events.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
public final class SymbolUpdateAdminEvent {
    private final String msgType = "adm_symbol_update";

    private final int symbolId;
    private final String symbolCode;

    // unmodifiable properties (gateway level)
    private final int priceStep; // price % priceStep == 0
    private final int priceScale; // decimal point position
    private final int lotSize;


    // modifiable properties (core level)
    // deposit settings
    private final long depositBuy;
    private final long depositSell;

    // order book limits
    private final long priceHighLimit;
    private final long priceLowLimit;
}

