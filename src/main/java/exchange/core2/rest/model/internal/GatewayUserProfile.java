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
package exchange.core2.rest.model.internal;

import exchange.core2.core.IEventsHandler.RejectEvent;
import exchange.core2.core.common.Order;
import exchange.core2.rest.commands.RestApiPlaceOrder;
import exchange.core2.rest.events.MatchingRole;
import exchange.core2.rest.model.api.GatewayOrderState;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.agrona.collections.MutableReference;


/**
 * Thread safe
 */
@Slf4j
public class GatewayUserProfile {

    // orders in status NEW/ACTIVE status
    private final Map<Long, GatewayOrder> openOrders = new HashMap<>();

    // orders in other statuses
    private final Map<Long, GatewayOrder> ordersHistory = new HashMap<>();

    public synchronized Map<Long, Integer> findUserCookies(final Stream<Order> activeOrders) {
        return activeOrders
                .map(Order::getOrderId)
                .collect(Collectors.toMap(
                        orderId -> orderId,
                        orderId -> openOrders.get(orderId).getUserCookie()));
    }

    public synchronized void addNewOrder(long orderId, String symbol, RestApiPlaceOrder restApiPlaceOrder) {

        GatewayOrder order = GatewayOrder.builder()
                .orderId(orderId)
                .userCookie(restApiPlaceOrder.getUserCookie())
                .price(restApiPlaceOrder.getPrice())
                .size(restApiPlaceOrder.getSize())
                .orderType(restApiPlaceOrder.getOrderType())
                .action(restApiPlaceOrder.getAction())
                .filled(0)
                .symbol(symbol)
                .state(GatewayOrderState.NEW)
                .build();

        openOrders.put(orderId, order);
    }

    public synchronized void activateOrder(long orderId) {
        GatewayOrder gatewayOrder = openOrders.get(orderId);
        gatewayOrder.setState(GatewayOrderState.ACTIVE);
    }

    public synchronized void tradeOrder(
            long orderId,
            long size,
            BigDecimal price,
            MatchingRole matchingRole,
            long timestamp,
            long counterOrderId,
            long counterPartyUid,
            Consumer<GatewayOrder> notifier) {

        final GatewayOrder gatewayOrder = openOrders.get(orderId);

        if (gatewayOrder.getState() == GatewayOrderState.ACTIVE) {
            gatewayOrder.setState(GatewayOrderState.PARTIALLY_FILLED);
        }

        long filled = gatewayOrder.getFilled() + size;
        gatewayOrder.setFilled(filled);
        if (gatewayOrder.getSize() == filled) {
            openOrders.remove(orderId);
            ordersHistory.put(orderId, gatewayOrder);
            gatewayOrder.setState(GatewayOrderState.COMPLETED);
            log.debug("MOVED order {} into history section", orderId);
        }

        gatewayOrder.getDeals().add(GatewayDeal.builder()
                .size(size)
                .price(price)
                .matchingRole(matchingRole)
                .timestamp(timestamp)
                .counterOrderId(counterOrderId)
                .counterPartyUid(counterPartyUid)
                .build());

        notifier.accept(gatewayOrder);
    }

    public synchronized void rejectOrder(MutableReference<RejectEvent> evt, Consumer<GatewayOrder> notifier) {
        GatewayOrder gatewayOrder = openOrders.remove(evt.ref.orderId);
        gatewayOrder.setState(GatewayOrderState.REJECTED);
        ordersHistory.put(evt.ref.orderId, gatewayOrder);
        log.debug("MOVED order {} into history section", evt.ref.orderId);
        notifier.accept(gatewayOrder);
    }
//    btc-usdt
//    Ask:   3: 9000

//    Bid:  5: 9100   IOC
//    9000 : 3 ,  2 cancel
//2: reject  1
//3: trade   2

    public synchronized void cancelOrder(long orderId, Consumer<GatewayOrder> notifier) {
        GatewayOrder gatewayOrder = openOrders.remove(orderId);
        ordersHistory.put(orderId, gatewayOrder);
        gatewayOrder.setState(GatewayOrderState.CANCELLED);
        log.debug("MOVED order {} into history section", orderId);
        notifier.accept(gatewayOrder);
    }


    public synchronized void updateOrderPrice(long orderId, BigDecimal newPrice, Consumer<GatewayOrder> notifier) {
        GatewayOrder gatewayOrder = openOrders.get(orderId);
        ordersHistory.put(orderId, gatewayOrder);
        gatewayOrder.setPrice(newPrice);
        notifier.accept(gatewayOrder);
    }

    public synchronized <T> List<T> mapHistoryOrders(Function<GatewayOrder, T> mapper) {
        return ordersHistory.values().stream().map(mapper).collect(Collectors.toList());
    }
}
