package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.cmd.OrderCommand;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.RestApiMoveOrder;
import org.openpredict.exchange.rest.commands.RestApiPlaceOrder;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.openpredict.exchange.rest.model.api.OrderState;
import org.openpredict.exchange.rest.model.api.RestApiOrder;
import org.openpredict.exchange.rest.model.internal.GatewaySymbolSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "syncTradeApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncTradeApiController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    // TODO per user
    //private ConcurrentHashMap<Long, Long> userCookies = new ConcurrentHashMap<>();

    @RequestMapping(value = "symbols/{symbol}/trade/{uid}/orders", method = RequestMethod.POST)
    public ResponseEntity<RestGenericResponse> placeOrder(
            @PathVariable long uid,
            @PathVariable String symbol,
            @Valid @RequestBody RestApiPlaceOrder placeOrder) throws ExecutionException, InterruptedException {

        log.info("PLACE ORDER >>> {}", placeOrder);

        GatewaySymbolSpec specification = gatewayState.getSymbolSpec(symbol);
        if (specification == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL);
        }
        final long longPrice = placeOrder.getPrice().longValue();

        final long longSize = placeOrder.getSize().longValue();

        // TODO perform conversions

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.placeNewOrder(
                0,
                longPrice,
                longSize,
                placeOrder.getAction(),
                placeOrder.getOrderType(),
                specification.symbolId,
                uid,
                future::complete);

        OrderCommand orderCommand = future.get();
        log.info("<<< PLACE ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .size(BigDecimal.valueOf(orderCommand.size))
                .filled(BigDecimal.valueOf(-1))
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
    public ResponseEntity<RestGenericResponse> placeOrder(
            @PathVariable long uid,
            @PathVariable String symbol,
            @PathVariable long orderId,
            @Valid @RequestBody RestApiMoveOrder moveOrder) throws ExecutionException, InterruptedException {

        log.info("MOVE ORDER >>> {}", moveOrder);

        GatewaySymbolSpec specification = gatewayState.getSymbolSpec(symbol);
        if (specification == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL);
        }
        final BigDecimal price = moveOrder.getPrice();
        final long longPrice = price.longValue();

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.moveOrder(
                longPrice,
                orderId,
                specification.symbolId,
                uid,
                future::complete);

        OrderCommand orderCommand = future.get();
        log.info("<<< PLACE ORDER {}", orderCommand);

        // TODO extract method and fix values
        RestApiOrder result = RestApiOrder.builder()
                .size(BigDecimal.valueOf(orderCommand.size))
                .filled(BigDecimal.valueOf(-1))
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
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL);
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
                .size(BigDecimal.valueOf(orderCommand.size))
                .filled(BigDecimal.valueOf(-1))
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
