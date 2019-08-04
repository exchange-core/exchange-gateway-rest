package org.openpredict.exchange.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class RestApiAsset {

    public final String assetCode;
    public final int assetId;
    public final int scale; // asset scale - 2 for most currencies, 8 for BTC, 0 for JPY, etc

    @JsonCreator
    public RestApiAsset(
            @JsonProperty("assetCode") String assetCode,
            @JsonProperty("assetId") int assetId,
            @JsonProperty("scale") int scale) {

        this.assetCode = assetCode;
        this.assetId = assetId;
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "[ASSET " + assetCode + " " + assetId + "]";
    }
}
