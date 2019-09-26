package org.openpredict.exchange.rest.model.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openpredict.exchange.beans.OrderAction;
import org.openpredict.exchange.beans.OrderType;
import org.openpredict.exchange.rest.model.api.OrderState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class GatewayOrder {

    private final long orderId;

    private final long userCookie;

    private final BigDecimal price;
    private final long size;
    private final OrderAction action;
    private final OrderType orderType;

    // mutable fields

    private final List<GatewayDeal> deals = new ArrayList<>();

    @Setter
    private long filled;

    @Setter
    private OrderState state;

}
