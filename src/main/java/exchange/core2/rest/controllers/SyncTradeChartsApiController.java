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

import exchange.core2.rest.GatewayState;
import exchange.core2.rest.commands.ApiErrorCodes;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.api.RestApiBar;
import exchange.core2.rest.model.api.TimeFrame;
import exchange.core2.rest.model.internal.GatewayBarStatic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "syncTradeApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncTradeChartsApiController {

    @Autowired
    private GatewayState gatewayState;

    @RequestMapping(value = "symbols/{symbol}/bars/{timeFrame}", method = RequestMethod.GET)
    public ResponseEntity<RestGenericResponse> getBars(
            @PathVariable String symbol,
            @PathVariable TimeFrame timeFrame) {
        log.info("GET BARS >>> {} - {}", symbol, timeFrame);

        return gatewayState.getBars(symbol, 100, timeFrame)
                .map(bars -> RestControllerHelper.successResponse(
                        bars.stream()
                                .map((GatewayBarStatic bar) -> new RestApiBar(
                                        bar.getOpen(),
                                        bar.getHigh(),
                                        bar.getLow(),
                                        bar.getClose(),
                                        bar.getVolume(),
                                        bar.getTimestamp()))
                                .collect(Collectors.toList()),
                        HttpStatus.OK))
                .orElseGet(() -> RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_SYMBOL_404));
    }

}
