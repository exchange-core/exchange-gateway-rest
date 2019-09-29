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


import exchange.core2.core.common.SymbolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;

import java.math.BigDecimal;

@Builder
@Wither
@AllArgsConstructor
public final class GatewaySymbolSpec {

    public final int symbolId;
    public final String symbolCode;

    public final SymbolType symbolType;

    public final GatewayAssetSpec baseAsset;     // base asset
    public final GatewayAssetSpec quoteCurrency; // quote/counter currency (OR futures contract currency)
    public final BigDecimal lotSize;
    public final BigDecimal stepSize;

    public final BigDecimal takerFee; // TODO check invariant: taker fee is not less than maker fee
    public final BigDecimal makerFee;

    public final BigDecimal marginBuy;
    public final BigDecimal marginSell;

    public final BigDecimal priceHighLimit;
    public final BigDecimal priceLowLimit;

    public final GatewaySymbolLifecycle status;

    public enum GatewaySymbolLifecycle {
        NEW,
        ACTIVE,
        INACTIVE;
    }
}
