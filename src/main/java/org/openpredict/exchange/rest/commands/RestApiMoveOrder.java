package org.openpredict.exchange.rest.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class RestApiMoveOrder {

    private final BigDecimal price;

    @JsonCreator
    public RestApiMoveOrder(
            @JsonProperty("price") BigDecimal price) {

        this.price = price;
    }

    @Override
    public String toString() {
        return "[MOVE " + price + "]";
    }
}
