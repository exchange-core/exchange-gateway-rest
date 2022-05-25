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
package exchange.core2.rest.controllers;

import exchange.core2.core.ExchangeApi;
import exchange.core2.core.ExchangeCore;
import exchange.core2.core.common.Order;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.reports.SingleUserReportQuery;
import exchange.core2.core.common.api.reports.SingleUserReportResult;
import exchange.core2.core.common.api.reports.SingleUserReportResult.QueryExecutionStatus;
import exchange.core2.rest.GatewayState;
import exchange.core2.rest.commands.ApiErrorCodes;
import exchange.core2.rest.commands.util.ArithmeticHelper;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.api.*;
import exchange.core2.rest.model.internal.GatewayAssetSpec;
import exchange.core2.rest.model.internal.GatewaySymbolSpec;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "syncTradeApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncTradeAccountApiController {
    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    @RequestMapping(value = "users/{uid}/state", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getUserState(
            @PathVariable Long uid) throws ExecutionException, InterruptedException {
        log.info("USER REPORT >>> {}", uid);
        final ExchangeApi api = exchangeCore.getApi();
        final SingleUserReportResult reportResult = api.processReport(new SingleUserReportQuery(uid), 123456).get();

        log.debug("{}", reportResult);

        if (reportResult.getQueryExecutionStatus() == QueryExecutionStatus.OK) {

            final List<RestApiOrder> activeOrders = new ArrayList<>();

            Stream<Order> ordersStream = reportResult.getOrders().stream().flatMap(Collection::stream);

            final Map<Long, Integer> userCookies = gatewayState.getOrCreateUserProfile(uid).findUserCookies(ordersStream);

            reportResult.getOrders().forEachKeyValue((symbolId, ordersList) -> {

                final GatewaySymbolSpec symbolSpec = gatewayState.getSymbolSpec(symbolId);
                ordersList.forEach(coreOrder -> activeOrders.add(RestApiOrder.builder()
                        .orderId(coreOrder.orderId)
                        .size(coreOrder.size)
                        .filled(coreOrder.filled)
                        .price(ArithmeticHelper.fromLongPrice(coreOrder.price, symbolSpec))
                        .state(GatewayOrderState.ACTIVE)
                        .action(coreOrder.action)
                        .orderType(OrderType.GTC)
                        .symbol(symbolSpec.symbolCode)
                        .deals(Collections.emptyList()) // TODO add deals
                        .userCookie(userCookies.get(coreOrder.orderId))
                        .build()));
            });

            final IntLongHashMap profileAccounts = reportResult.getAccounts();

            final List<RestApiAccountState> accounts = new ArrayList<>(profileAccounts.size());
            profileAccounts.forEachKeyValue((assetId, balance) -> {
                final GatewayAssetSpec assetSpec = gatewayState.getAssetSpec(assetId);
                accounts.add(RestApiAccountState.builder()
                        .currency(assetSpec.assetCode)
                        .balance(ArithmeticHelper.fromLongPrice(balance, assetSpec))
                        .build());
            });
            final RestApiUserState state = RestApiUserState.builder()
                    .accounts(accounts)
                    .activeOrders(activeOrders)
                    .uid(uid)
                    .build();

            return RestControllerHelper.successResponse(state, HttpStatus.OK);

        } else {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_USER_404);
        }
    }


    @RequestMapping(value = "users/{uid}/history", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getUserTradesHistory(
            @PathVariable Long uid) {
        log.info("TRADES HISTORY >>> {}", uid);

        return gatewayState.getUserProfile(uid).map(up ->
                RestControllerHelper.successResponse(RestApiAccountTradeHistory.builder()
                        .orders(up.mapHistoryOrders(coreOrder ->
                                RestApiOrder.builder()
                                        .orderId(coreOrder.getOrderId())
                                        .size(coreOrder.getSize())
                                        .filled(coreOrder.getFilled())
                                        .price(coreOrder.getPrice())
                                        .state(coreOrder.getState())
                                        .action(coreOrder.getAction())
                                        .orderType(coreOrder.getOrderType())
                                        .symbol(coreOrder.getSymbol())
                                        .deals(coreOrder.getDeals().stream().map(deal -> RestApiDeal.builder()
                                                .party(deal.getMatchingRole())
                                                .price(deal.getPrice())
                                                .size(deal.getSize())
                                                .build()).collect(Collectors.toList()))
                                        .userCookie(coreOrder.getUserCookie())
                                        .build()))
                        .uid(uid)
                        .build(), HttpStatus.OK))
                .orElseGet(() -> RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_USER_404));
    }

}
