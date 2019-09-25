package org.openpredict.exchange.rest.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.openpredict.exchange.beans.OrderAction;
import org.openpredict.exchange.beans.OrderType;

import java.math.BigDecimal;

@Getter
public final class RestApiPlaceOrder {

    private final BigDecimal price;
    private final long size; // only integer sizes allowed

    private final int userCookie;
    private final OrderAction action;
    private final OrderType orderType;

    @JsonCreator
    public RestApiPlaceOrder(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") long size,
            @JsonProperty("userCookie") int userCookie,
            @JsonProperty("action") OrderAction action,
            @JsonProperty("orderType") OrderType orderType) {

        this.price = price;
        this.size = size;
        this.userCookie = userCookie;
        this.action = action;
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "[ADD " + (action == OrderAction.ASK ? 'A' : 'B') + orderType
                + price + ":" + size + "]";
    }
}
