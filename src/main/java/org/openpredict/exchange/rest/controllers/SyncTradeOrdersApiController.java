package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.L2MarketData;
import org.openpredict.exchange.beans.cmd.OrderCommand;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.RestApiMoveOrder;
import org.openpredict.exchange.rest.commands.RestApiPlaceOrder;
import org.openpredict.exchange.rest.commands.util.ArithmeticHelper;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.openpredict.exchange.rest.model.api.OrderState;
import org.openpredict.exchange.rest.model.api.RestApiOrder;
import org.openpredict.exchange.rest.model.api.RestApiOrderBook;
import org.openpredict.exchange.rest.model.internal.GatewaySymbolSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
            @Valid @RequestBody RestApiPlaceOrder placeOrder) throws ExecutionException, InterruptedException {

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
                0,
                price,
                price, // same price (can not move bids up in exchange mode)
                placeOrder.getSize(),
                placeOrder.getAction(),
                placeOrder.getOrderType(),
                symbolSpec.symbolId,
                uid,
                future::complete);
        log.info("placing orderId {}", orderId);

        OrderCommand orderCommand = future.get();
        log.info("<<< PLACE ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .orderId(orderCommand.orderId)
                .size(orderCommand.size)
                .filled(0)
                .state(OrderState.NEW)
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
            @Valid @RequestBody RestApiMoveOrder moveOrder) throws ExecutionException, InterruptedException {

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
                .state(OrderState.ACTIVE)
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
                .state(OrderState.CANCELLED)
                .userCookie(orderCommand.userCookie)
                .action(orderCommand.action)
                .orderType(orderCommand.orderType)
                .symbol(gatewayState.getSymbolSpec(orderCommand.symbol).symbolCode)
                .deals(Collections.emptyList())
                .build();

        return RestControllerHelper.coreResponse(orderCommand, () -> result, HttpStatus.OK);
    }

}
