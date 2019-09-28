package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.openpredict.exchange.beans.Order;
import org.openpredict.exchange.beans.OrderType;
import org.openpredict.exchange.beans.api.reports.SingleUserReportQuery;
import org.openpredict.exchange.beans.api.reports.SingleUserReportResult;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.util.ArithmeticHelper;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.openpredict.exchange.rest.model.api.OrderState;
import org.openpredict.exchange.rest.model.api.RestApiAccountState;
import org.openpredict.exchange.rest.model.api.RestApiOrder;
import org.openpredict.exchange.rest.model.api.RestApiUserState;
import org.openpredict.exchange.rest.model.internal.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.internal.GatewaySymbolSpec;
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
import java.util.concurrent.Future;
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

        if (reportResult.getStatus() == SingleUserReportResult.ExecutionStatus.OK) {

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
                        .state(OrderState.ACTIVE)
                        .action(coreOrder.action)
                        .orderType(OrderType.GTC)
                        .symbol(symbolSpec.symbolCode)
                        .deals(Collections.emptyList())
                        .userCookie(userCookies.get(coreOrder.orderId))
                        .build()));
            });

            final IntLongHashMap profileAccounts = reportResult.getUserProfile().accounts;

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


}
