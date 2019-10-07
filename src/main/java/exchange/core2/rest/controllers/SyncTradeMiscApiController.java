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

import exchange.core2.core.ExchangeCore;
import exchange.core2.rest.GatewayState;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.api.RestApiAsset;
import exchange.core2.rest.model.api.RestApiExchangeInfo;
import exchange.core2.rest.model.api.RestApiSymbol;
import exchange.core2.rest.model.api.RestApiTime;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "syncTradeApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncTradeMiscApiController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    @RequestMapping(value = "ping", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getPing() {
        log.info("PING >>>");
        return RestControllerHelper.successResponse(null, HttpStatus.OK);
    }

    @RequestMapping(value = "time", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getTime() {
        log.info("TIME >>>");
        return RestControllerHelper.successResponse(
                getRestApiTime(),
                HttpStatus.OK);
    }

    @NotNull
    private RestApiTime getRestApiTime() {
        final Instant now = Instant.now();
        return new RestApiTime(DateTimeFormatter.ISO_INSTANT.format(now), now.toEpochMilli());
    }

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getExchangeInfo() {
        log.info("EXCHANGE INFO >>>");

        final List<RestApiAsset> activeAssets = gatewayState.getActiveAssets(c -> new RestApiAsset(c.assetCode, c.scale));

        log.info("EXCHANGE activeAssets >>>", activeAssets);

        final List<RestApiSymbol> activeSymbols = gatewayState.getActiveSymbols(s -> new RestApiSymbol(
                s.symbolCode,
                s.symbolType,
                s.baseAsset.assetCode,
                s.quoteCurrency.assetCode,
                s.lotSize,
                s.stepSize,
                s.takerFee,
                s.makerFee,
                s.marginBuy,
                s.marginSell,
                s.priceHighLimit,
                s.priceLowLimit
        ));

        final RestApiExchangeInfo restApiExchangeInfo = RestApiExchangeInfo.builder()
                .assets(activeAssets)
                .symbols(activeSymbols)
                .serverTime(getRestApiTime())
                .build();

        return RestControllerHelper.successResponse(
                restApiExchangeInfo,
                HttpStatus.OK);
    }


}
