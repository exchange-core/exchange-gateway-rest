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

import com.fasterxml.jackson.annotation.JsonProperty;
import exchange.core2.core.common.SymbolType;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Getter
public class RestApiSymbol {

    private final String symbolCode;

    private final SymbolType symbolType;

    private final String baseAsset;     // base asset
    private final String quoteCurrency; // quote/counter currency (OR futures contract currency)
    private final BigDecimal lotSize;
    private final BigDecimal stepSize;

    private final BigDecimal takerFee;
    private final BigDecimal makerFee;

    private final BigDecimal marginBuy;
    private final BigDecimal marginSell;

    private final BigDecimal priceHighLimit;
    private final BigDecimal priceLowLimit;

    public RestApiSymbol(
            @JsonProperty("symbolCode") String symbolCode,
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
}
