package org.openpredict.exchange.rest.model.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.openpredict.exchange.beans.OrderAction;
import org.openpredict.exchange.beans.OrderType;

import java.math.BigDecimal;
import java.util.List;

@Getter
public final class RestApiOrder {

    private final long orderId;

    private final BigDecimal price;
    private final BigDecimal size;
    private final BigDecimal filled;

    private final OrderState state;

    private final long userCookie;

    private final OrderAction action;
    private final OrderType orderType;

    private final String symbol;

    private final List<RestApiDeal> deals;

    // TODO add more fields

    @JsonCreator
    @Builder
    public RestApiOrder(
            @JsonProperty("orderId") long orderId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") BigDecimal size,
            @JsonProperty("filled") BigDecimal filled,
            @JsonProperty("userCookie") long userCookie,
            @JsonProperty("state") OrderState state,
            @JsonProperty("action") OrderAction action,
            @JsonProperty("orderType") OrderType orderType,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("deals") List<RestApiDeal> deals) {

        this.orderId = orderId;
        this.price = price;
        this.size = size;
        this.filled = filled;
        this.userCookie = userCookie;
        this.state = state;
        this.action = action;
        this.orderType = orderType;
        this.symbol = symbol;
        this.deals = deals;
    }
}
