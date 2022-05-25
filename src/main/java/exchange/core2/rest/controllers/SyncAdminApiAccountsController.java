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

import exchange.core2.core.common.BalanceAdjustmentType;
import lombok.extern.slf4j.Slf4j;
import exchange.core2.core.common.cmd.OrderCommand;
import exchange.core2.core.ExchangeApi;
import exchange.core2.core.ExchangeCore;
import exchange.core2.rest.GatewayState;
import exchange.core2.rest.commands.ApiErrorCodes;
import exchange.core2.rest.commands.admin.RestApiAccountBalanceAdjustment;
import exchange.core2.rest.commands.admin.RestApiAddUser;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.internal.GatewayAssetSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "syncAdminApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncAdminApiAccountsController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;


//    @RequestMapping(value = "users2", method = RequestMethod.POST)
//    @ResponseStatus(HttpStatus.CREATED)
//    public CompletableFuture<RestGenericResponse<Object>> createUser2(@Valid @RequestBody RestApiAddUser request) throws Exception {
//
//
//        log.info("ADD USER >>> {}", request);
//
////            if (false) {
////                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_BASE_ASSET);
////            }
//
//        CompletableFuture<RestGenericResponse<Object>> future = new CompletableFuture<>();
//
//        final ExchangeApi api = exchangeCore.getApi();
//
//        api.createUser(request.getUid(), cmd2 -> {
//            log.info("<<< ADD USER {}", cmd2);
//
//            RestGenericResponse<Object> resp = RestGenericResponse.builder()
//                    .ticket(0)
//                    .gatewayResultCode(0)
//                    .coreResultCode(0)
//                    .data(cmd2.uid)
//                    .description(cmd2.resultCode.toString())
//                    .build();
//
//            future.complete(resp);
//        });
//        return future;
//    }

    @RequestMapping(value = "users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public RestGenericResponse createUser(@Valid @RequestBody RestApiAddUser request) throws ExecutionException, InterruptedException {

        log.info("ADD USER >>> {}", request);

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.createUser(request.getUid(), future::complete);

        OrderCommand cmd = future.get();
        log.info("<<< ADD USER {}", cmd);

        return RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(0)
                .coreResultCode(cmd.resultCode.getCode())
                .data(cmd.uid)
                .description(cmd.resultCode.toString())
                .build();

    }

    @RequestMapping(value = "users/{uid}/accounts", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RestGenericResponse> adjustBalance(
            @PathVariable long uid,
            @Valid @RequestBody RestApiAccountBalanceAdjustment request) throws ExecutionException, InterruptedException {

        log.info("ADD BALANCE >>> {} {}", uid, request);

        // TODO currency conversion

        final GatewayAssetSpec currency = gatewayState.getAssetSpec(request.currency);
        if (currency == null) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_CURRENCY);
        }


        final BigDecimal amount = request.getAmount().scaleByPowerOfTen(currency.scale).stripTrailingZeros();
        if (amount.scale() > 0) {
            return RestControllerHelper.errorResponse(ApiErrorCodes.PRECISION_IS_TOO_HIGH);
        }

        final long longAmount = amount.longValue();

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();
        api.balanceAdjustment(uid, request.getTransactionId(), currency.assetId, longAmount, BalanceAdjustmentType.ADJUSTMENT, future::complete);

        OrderCommand orderCommand = future.get();
        log.info("<<< ADD BALANCE {}", orderCommand);
        //asyncCoreResponseOk(asyncResponse, cmd, cmd2.resultCode);

        return RestControllerHelper.coreResponse(orderCommand, () -> uid, HttpStatus.CREATED);
    }


}
