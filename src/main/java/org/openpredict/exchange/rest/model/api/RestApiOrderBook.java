package org.openpredict.exchange.rest.model.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Wither
@EqualsAndHashCode
public final class RestApiOrderBook {

    //private final long timestamp;

    private final String symbol;

    private final List<BigDecimal> askPrices;
    private final List<Long> askVolumes;
    private final List<BigDecimal> bidPrices;
    private final List<Long> bidVolumes;

    @JsonCreator
    @Builder
    public RestApiOrderBook(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("askPrices") List<BigDecimal> askPrices,
            @JsonProperty("askVolumes") List<Long> askVolumes,
            @JsonProperty("bidPrices") List<BigDecimal> bidPrices,
            @JsonProperty("bidVolumes") List<Long> bidVolumes) {

        this.symbol = symbol;

        this.askPrices = askPrices;
        this.askVolumes = askVolumes;
        this.bidPrices = bidPrices;
        this.bidVolumes = bidVolumes;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return askPrices.isEmpty() && askVolumes.isEmpty() && bidPrices.isEmpty() && bidVolumes.isEmpty();
    }

    @Override
    public String toString() {
        return "RestApiOrderBook{" +
                "symbol='" + symbol + '\'' +
                ", askPrices=" + askPrices +
                ", askVolumes=" + askVolumes +
                ", bidPrices=" + bidPrices +
                ", bidVolumes=" + bidVolumes +
                '}';
    }
}
