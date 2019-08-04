package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.RestApiCancelOrder;
import org.openpredict.exchange.rest.commands.RestApiMoveOrder;
import org.openpredict.exchange.rest.commands.RestApiPlaceOrder;
import org.openpredict.exchange.rest.model.GatewaySymbolSpec;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.setup.On;
import org.rapidoid.u.U;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
@Slf4j
public class RestSyncTradeApiController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    // TODO per user
    //private ConcurrentHashMap<Long, Long> userCookies = new ConcurrentHashMap<>();

    @PostConstruct
    public void initRestApi() {

        final ExchangeApi api = exchangeCore.getApi();

        On.post("/syncTradeApi/v1/orders").json((Req req, RestApiPlaceOrder placeOrder) -> {
            log.info("PLACE >>> {}", placeOrder);

            GatewaySymbolSpec specification = gatewayState.getSymbolSpec(placeOrder.getSymbol());
            if (specification == null) {
                return U.map("status", "failed", "description", "unknown symbol");
            }
            final BigDecimal price = new BigDecimal(placeOrder.getPrice());
            final long longPrice = price.longValue();

            final BigDecimal size = new BigDecimal(placeOrder.getSize());
            final long longSize = size.longValue();

            final Resp resp = req.async().response();

            api.placeNewOrder(
                    0,
                    longPrice,
                    longSize,
                    placeOrder.getAction(),
                    placeOrder.getOrderType(),
                    specification.symbolId,
                    placeOrder.getUid(),
                    cmd2 -> {
                        log.info("PLACE <<< " + cmd2);
                        resp.code(200).done();

                    });

            return resp;
        });

        On.put("/syncTradeApi/v1/orders").json((Req req, RestApiMoveOrder moveOrder) -> {
            log.info("MOVE >>> {}", moveOrder);

            GatewaySymbolSpec specification = gatewayState.getSymbolSpec(moveOrder.getSymbol());
            if (specification == null) {
                return U.map("status", "failed", "description", "unknown symbol");
            }

            final BigDecimal price = new BigDecimal(moveOrder.getPrice());
            final long longPrice = price.longValue();

            final Resp resp = req.async().response();

            api.moveOrder(
                    0,
                    longPrice,
                    moveOrder.getOrderId(),
                    specification.symbolId,
                    moveOrder.getUid(),
                    cmd2 -> {
                        log.info("MOVE <<< " + cmd2);
                        resp.code(200).done();

                    });

            return resp;
        });

        On.delete("/syncTradeApi/v1/orders").json((Req req, RestApiCancelOrder cancelOrder) -> {
            log.info("CANCEL >>> {}", cancelOrder);

            GatewaySymbolSpec specification = gatewayState.getSymbolSpec(cancelOrder.getSymbol());
            if (specification == null) {
                return U.map("status", "failed", "description", "unknown symbol");
            }

            final Resp resp = req.async().response();

            api.cancelOrder(
                    0,
                    cancelOrder.getOrderId(),
                    specification.symbolId,
                    cancelOrder.getUid(),
                    cmd2 -> {
                        log.info("CANCEL <<< " + cmd2);
                        resp.code(200).done();
                    });

            return resp;
        });

    }


}
