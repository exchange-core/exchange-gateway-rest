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
import exchange.core2.core.common.L2MarketData;
import exchange.core2.core.common.cmd.OrderCommand;
import exchange.core2.rest.GatewayState;
import exchange.core2.rest.commands.ApiErrorCodes;
import exchange.core2.rest.commands.RestApiMoveOrder;
import exchange.core2.rest.commands.RestApiPlaceOrder;
import exchange.core2.rest.commands.util.ArithmeticHelper;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.api.GatewayOrderState;
import exchange.core2.rest.model.api.RestApiOrder;
import exchange.core2.rest.model.api.RestApiOrderBook;
import exchange.core2.rest.model.internal.GatewaySymbolSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "syncTradeApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncTradeOrdersApiController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    // TODO per user
    //private ConcurrentHashMap<Long, Long> userCookies = new ConcurrentHashMap<>();

    @RequestMapping(value = "symbols/{symbol}/orderbook", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getOrderBook(
            @PathVariable String symbol,
            @RequestParam Integer depth) throws ExecutionException, InterruptedException {
        log.info("ORDERBOOK >>> {} {}", symbol, depth);

        GatewaySymbolSpec symbolSpec = gatewayState.getSymbolSpec(symbol);
        if (symbolSpec == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL_404);
        }

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<ResponseEntity<RestGenericResponse>> future = new CompletableFuture<>();
        api.orderBookRequest(symbolSpec.symbolId, depth, orderCommand -> {
            L2MarketData marketData = orderCommand.marketData;
            future.complete(RestControllerHelper.coreResponse(
                    orderCommand,
                    () -> RestApiOrderBook.builder()
                            .symbol(symbol)
                            .askPrices(Arrays.stream(marketData.askPrices).mapToObj(p -> ArithmeticHelper.fromLongPrice(p, symbolSpec)).collect(Collectors.toList()))
                            .bidPrices(Arrays.stream(marketData.bidPrices).mapToObj(p -> ArithmeticHelper.fromLongPrice(p, symbolSpec)).collect(Collectors.toList()))
                            .askVolumes(Arrays.stream(marketData.askVolumes).boxed().collect(Collectors.toList()))
                            .bidVolumes(Arrays.stream(marketData.bidVolumes).boxed().collect(Collectors.toList()))
                            .build(),
                    HttpStatus.OK));
        });

        ResponseEntity<RestGenericResponse> response = future.get();
        log.info("<<< ORDERBOOK {}", response);

        return response;
    }


    @RequestMapping(value = "symbols/{symbol}/trade/{uid}/orders", method = RequestMethod.POST)
    public ResponseEntity<RestGenericResponse> placeOrder(
            @PathVariable long uid,
            @PathVariable String symbol,
            @RequestBody RestApiPlaceOrder placeOrder) throws ExecutionException, InterruptedException {

        log.info("PLACE ORDER >>> {}", placeOrder);

        GatewaySymbolSpec symbolSpec = gatewayState.getSymbolSpec(symbol);
        if (symbolSpec == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL_404);
        }

        final BigDecimal priceInQuoteCurrencyUnits = ArithmeticHelper.toBaseUnits(placeOrder.getPrice(), symbolSpec.quoteCurrency);
        if (!ArithmeticHelper.isIntegerNotNegativeValue(priceInQuoteCurrencyUnits)) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.INVALID_PRICE);
        }

        final long price = priceInQuoteCurrencyUnits.longValue();

        // TODO perform conversions

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        long orderId = api.placeNewOrder(
                placeOrder.getUserCookie(),
                price,
                price, // same price (can not move bids up in exchange mode)
                placeOrder.getSize(),
                placeOrder.getAction(),
                placeOrder.getOrderType(),
                symbolSpec.symbolId,
                uid,
                future::complete);
        log.info("placing orderId {}", orderId);

        // TODO can be inserted after events - insert into cookie-based queue first?
        gatewayState.getOrCreateUserProfile(uid).addNewOrder(orderId, symbol, placeOrder);

        OrderCommand orderCommand = future.get();
        log.info("<<< PLACE ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .orderId(orderCommand.orderId)
                .size(orderCommand.size)
                .filled(0)
                .state(GatewayOrderState.NEW)
                .userCookie(orderCommand.userCookie)
                .action(orderCommand.action)
                .orderType(orderCommand.orderType)
                .symbol(gatewayState.getSymbolSpec(orderCommand.symbol).symbolCode)
                .deals(Collections.emptyList())
                .build();

        return RestControllerHelper.coreResponse(orderCommand, () -> result, HttpStatus.CREATED);
    }


    @RequestMapping(value = "symbols/{symbol}/trade/{uid}/orders/{orderId}", method = RequestMethod.PUT)
    public ResponseEntity<RestGenericResponse> moveOrder(
            @PathVariable long uid,
            @PathVariable String symbol,
            @PathVariable long orderId,
            @RequestBody RestApiMoveOrder moveOrder) throws ExecutionException, InterruptedException {

        log.info("MOVE ORDER >>> {} uid={} {}", orderId, uid, moveOrder);

        GatewaySymbolSpec symbolSpec = gatewayState.getSymbolSpec(symbol);
        if (symbolSpec == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL_404);
        }

        final BigDecimal priceInQuoteCurrencyUnits = ArithmeticHelper.toBaseUnits(moveOrder.getPrice(), symbolSpec.quoteCurrency);
        if (!ArithmeticHelper.isIntegerNotNegativeValue(priceInQuoteCurrencyUnits)) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.INVALID_PRICE);
        }

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.moveOrder(
                priceInQuoteCurrencyUnits.longValue(),
                orderId,
                symbolSpec.symbolId,
                uid,
                future::complete);

        OrderCommand orderCommand = future.get();
        log.info("<<< MOVE ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .orderId(orderCommand.orderId)
                .size(orderCommand.size)
                .filled(-1)
                .state(GatewayOrderState.ACTIVE)
                .userCookie(orderCommand.userCookie)
                .action(orderCommand.action)
                .orderType(orderCommand.orderType)
                .symbol(gatewayState.getSymbolSpec(orderCommand.symbol).symbolCode)
                .deals(Collections.emptyList())
                .build();

        return RestControllerHelper.coreResponse(orderCommand, () -> result, HttpStatus.OK);
    }

    @RequestMapping(value = "symbols/{symbol}/trade/{uid}/orders/{orderId}", method = RequestMethod.DELETE)
    public ResponseEntity<RestGenericResponse> cancelOrder(
            @PathVariable long uid,
            @PathVariable String symbol,
            @PathVariable long orderId) throws ExecutionException, InterruptedException {

        log.info("CANCEL ORDER >>> {}", orderId);

        GatewaySymbolSpec specification = gatewayState.getSymbolSpec(symbol);
        if (specification == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL_404);
        }

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.cancelOrder(
                orderId,
                specification.symbolId,
                uid,
                future::complete);

        OrderCommand orderCommand = future.get();
        log.info("<<< CANCEL ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .orderId(orderCommand.orderId)
                .size(orderCommand.size)
                .filled(-1)
                .state(GatewayOrderState.CANCELLED)
                .userCookie(orderCommand.userCookie)
                .action(orderCommand.action)
                .orderType(orderCommand.orderType)
                .symbol(gatewayState.getSymbolSpec(orderCommand.symbol).symbolCode)
                .deals(Collections.emptyList())
                .build();

        return RestControllerHelper.coreResponse(orderCommand, () -> result, HttpStatus.OK);
    }

}
