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
package exchange.core2.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import exchange.core2.core.common.SymbolType;
import lombok.Builder;

import java.math.BigDecimal;

public final class RestApiAddSymbol {

    public final int symbolId;
    public final String symbolCode;

    public final SymbolType symbolType;

    public final String baseAsset;     // base asset
    public final String quoteCurrency; // quote/counter currency (OR futures contract currency)
    public final BigDecimal lotSize; // lot size in base asset units
    public final BigDecimal stepSize; // step size in quote currency units

    public final BigDecimal takerFee; // TODO check invariant: taker fee is not less than maker fee
    public final BigDecimal makerFee;

    public final BigDecimal marginBuy;
    public final BigDecimal marginSell;

    public final BigDecimal priceHighLimit;
    public final BigDecimal priceLowLimit;

    @JsonCreator
    @Builder
    public RestApiAddSymbol(
            @JsonProperty("symbolCode") String symbolCode,
            @JsonProperty("symbolId") int symbolId,
            @JsonProperty("symbolType") SymbolType symbolType,
            @JsonProperty("baseAsset") String baseAsset,
            @JsonProperty("quoteCurrency") String quoteCurrency,
            @JsonProperty("lotSize") BigDecimal lotSize,
            @JsonProperty("stepSize") BigDecimal stepSize,
            @JsonProperty("takerFee") BigDecimal takerFee,
            @JsonProperty("makerFee") BigDecimal makerFee,
            @JsonProperty("marginBuy") BigDecimal marginBuy,
            @JsonProperty("marginSell") BigDecimal marginSell,
            @JsonProperty("priceHighLimit") BigDecimal priceHighLimit,
            @JsonProperty("priceLowLimit") BigDecimal priceLowLimit) {

        this.symbolCode = symbolCode;
        this.symbolId = symbolId;

        this.symbolType = symbolType;

        this.baseAsset = baseAsset;
        this.quoteCurrency = quoteCurrency;
        this.lotSize = lotSize;
        this.stepSize = stepSize;
        this.takerFee = takerFee;
        this.makerFee = makerFee;

        this.marginBuy = marginBuy;
        this.marginSell = marginSell;
        this.priceHighLimit = priceHighLimit;
        this.priceLowLimit = priceLowLimit;
    }

    @Override
    public String toString() {
        return "[ADDSYMBOL " + symbolCode + " " + symbolId + "]";
    }
}
