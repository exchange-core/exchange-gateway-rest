package org.openpredict.exchange.rest.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.admin.RestApiAccountBalanceAdjustment;
import org.openpredict.exchange.rest.commands.admin.RestApiAddUser;
import org.openpredict.exchange.rest.model.GatewayAssetSpec;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.setup.On;

import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
public class RestSyncAdminApiUsersController {

    private static final String SYNC_ADMIN_API_V1 = "/syncAdminApi/v1/";

    public static void init(ExchangeApi api, GatewayState gatewayState) {


        On.post(SYNC_ADMIN_API_V1 + "users").json((final Req req, final RestApiAddUser cmd) -> {
            log.info("ADD USER >>> {}", cmd);


//            if (false) {
//                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_BASE_ASSET);
//            }

            final Resp asyncResponse = req.async().response();

            api.createUser(cmd.getUid(), cmd2 -> {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                log.info("<<< ADD USER {}", cmd2);

                RestControllerHelper.asyncCoreResponseCreated(asyncResponse, cmd, cmd2.resultCode);
            });
            return asyncResponse;
        });


        On.post(SYNC_ADMIN_API_V1 + "users/balance").json((final Req req, final RestApiAccountBalanceAdjustment cmd) -> {
            log.info("ADD BALANCE >>> {}", cmd);

            // TODO currency conversion

            final GatewayAssetSpec currency = gatewayState.getAssetSpec(cmd.currency);
            if (currency == null) {
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.UNKNOWN_CURRENCY);
            }


            final BigDecimal amount = cmd.getAmount().scaleByPowerOfTen(currency.scale).stripTrailingZeros();
            if (amount.scale() > 0) {
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.PRECISION_IS_TOO_HIGH);
            }

            final long longAmount = amount.longValue();

            final Resp asyncResponse = req.async().response();

            api.balanceAdjustment(cmd.getUid(), cmd.getTransactionId(), longAmount, cmd2 -> {
                log.info("<<< ADD BALANCE {}", cmd2);
                RestControllerHelper.asyncCoreResponseOk(asyncResponse, cmd, cmd2.resultCode);
            });

            return asyncResponse;
        });


    }


}
