package org.openpredict.exchange.rest.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;
import org.openpredict.exchange.beans.SymbolType;

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
