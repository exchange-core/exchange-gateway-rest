package org.openpredict.exchange.rest.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
public final class GatewayAssetSpec {

    public final String assetCode;
    public final int assetId;
    public final int scale; // asset scale - normally 2 for currencies, 8 for BTC, etc

    // TODO lifecycle - new, active, ceased
    public final boolean active;

}
