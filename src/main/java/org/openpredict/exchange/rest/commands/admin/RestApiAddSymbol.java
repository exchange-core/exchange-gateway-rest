package org.openpredict.exchange.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.openpredict.exchange.beans.SymbolType;

import java.math.BigDecimal;

public final class RestApiAddSymbol {

    public final int symbolId;
    public final String symbolCode;

    public final SymbolType symbolType;

    public final String baseAsset;     // base asset
    public final String quoteCurrency; // quote/counter currency (OR futures contract currency)
    public final BigDecimal lotSize;
    public final BigDecimal stepSize;

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
