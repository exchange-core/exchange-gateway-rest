package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.CoreSymbolSpecification;
import org.openpredict.exchange.beans.api.binary.BatchAddSymbolsCommand;
import org.openpredict.exchange.beans.cmd.CommandResultCode;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.admin.RestApiAccountBalanceAdjustment;
import org.openpredict.exchange.rest.commands.admin.RestApiAddSymbol;
import org.openpredict.exchange.rest.commands.admin.RestApiAddUser;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.openpredict.exchange.rest.model.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.GatewaySymbolSpec;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.setup.App;
import org.rapidoid.setup.On;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;

@Service
@Slf4j
public class RestSyncAdminApiController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;

    public static final String SYNC_ADMIN_API_V1 = "/syncAdminApi/v1/";

    @PostConstruct
    public void initRestApi() {

        exchangeCore.startup();

        final ExchangeApi api = exchangeCore.getApi();

//        App.bootstrap(new String[0]);

        App.bootstrap(new String[0], "profiles=mysql,prod", "on.address=0.0.0.0", "on.port=8080" ).jpa();


        On.get(SYNC_ADMIN_API_V1 + "symbols/{symbolName}/orderBook").json((Req req, String symbolName) -> {

            log.info("ORDER BOOK >>> symbolName={}", symbolName);

            final GatewaySymbolSpec spec = gatewayState.getSymbolSpec(symbolName);

            if (spec == null) {
                Resp resp = req.response();
                log.warn("Symbol not present");
                resp.code(404);
                return null;
            }

            final Resp asyncResponse = req.async().response();

            api.orderBookRequest(spec.symbolId, cmd2 -> {
                log.info("<<< ORDER BOOK {}", cmd2);
                asyncResponse.code(200).done();
            });

            return asyncResponse;
        });


        On.post(SYNC_ADMIN_API_V1 + "users").json((final Req req, final RestApiAddUser cmd) -> {
            log.info("ADD USER >>> {}", cmd);


            if (false) {
                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_BASE_ASSET);
            }

            final Resp asyncResponse = req.async().response();

            api.createUser(cmd.getUid(), cmd2 -> {
                log.info("RECV, sleep 3000");

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                log.info("<<< ADD USER {}", cmd2);

                asyncCoreResponseCreated(asyncResponse, cmd, cmd2.resultCode);
            });
            return asyncResponse;
        });


        On.post(SYNC_ADMIN_API_V1 + "users/balance").json((final Req req, final RestApiAccountBalanceAdjustment cmd) -> {
            log.info("ADD BALANCE >>> {}", cmd);

            // TODO currency conversion

            final GatewayAssetSpec currency = gatewayState.getAssetSpec(cmd.currency);
            if (currency == null) {
                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_CURRENCY);
            }


            final BigDecimal amount = cmd.getAmount().scaleByPowerOfTen(currency.scale).stripTrailingZeros();
            if (amount.scale() > 0) {
                return errorResponse(req.response(), ApiErrorCodes.PRECISION_IS_TOO_HIGH);
            }

            final long longAmount = amount.longValue();

            final Resp asyncResponse = req.async().response();

            api.balanceAdjustment(cmd.getUid(), cmd.getTransactionId(), longAmount, cmd2 -> {
                log.info("<<< ADD BALANCE {}", cmd2);
                asyncCoreResponseOk(asyncResponse, cmd, cmd2.resultCode);
            });

            return asyncResponse;
        });


        On.post(SYNC_ADMIN_API_V1 + "symbols").json((final Req req, final RestApiAddSymbol cmd) -> {
            log.info("ADD SYMBOL >>> {}", cmd);

            // TODO Publish through bus

            final GatewayAssetSpec baseAsset = gatewayState.getAssetSpec(cmd.baseAsset);
            if (baseAsset == null) {
                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_BASE_ASSET);
            }

            final GatewayAssetSpec quoteCurrency = gatewayState.getAssetSpec(cmd.quoteCurrency);
            if (quoteCurrency == null) {
                return errorResponse(req.response(), ApiErrorCodes.UNKNOWN_QUOTE_CURRENCY);
            }

            // TODO validations
            final int symbolId = cmd.symbolId;

            final GatewaySymbolSpec spec = GatewaySymbolSpec.builder()
                    .symbolId(symbolId)
                    .symbolCode(cmd.symbolCode)
                    .symbolType(cmd.symbolType)
                    .baseAsset(baseAsset)
                    .quoteCurrency(quoteCurrency)
                    .lotSize(cmd.lotSize)
                    .stepSize(cmd.stepSize)
                    .takerFee(cmd.takerFee)
                    .makerFee(cmd.makerFee)
                    .marginBuy(cmd.marginBuy)
                    .marginSell(cmd.marginSell)
                    .priceHighLimit(cmd.priceHighLimit)
                    .priceLowLimit(cmd.priceLowLimit)
                    .status(GatewaySymbolSpec.GatewaySymbolLifecycle.NEW)
                    .build();

            if (!gatewayState.registerNewSymbol(spec)) {
                return errorResponse(req.response(), ApiErrorCodes.SYMBOL_ALREADY_EXISTS);
            }

            final CoreSymbolSpecification coreSpec = CoreSymbolSpecification.builder()
                    .symbolId(symbolId)
                    .type(cmd.symbolType)
                    .baseCurrency(baseAsset.assetId)
                    .quoteCurrency(quoteCurrency.assetId)
                    // TODO fix
                    .baseScaleK(1)
                    .quoteScaleK(1)
                    .takerFee(1)
                    .makerFee(1)
                    .marginBuy(10)
                    .marginSell(10)
                    .build();

            BatchAddSymbolsCommand batchAddSymbols = new BatchAddSymbolsCommand(coreSpec);

            final Resp asyncResponse = req.async().response();

            api.submitBinaryCommandAsync(batchAddSymbols, 123567, cmd2 -> {
                log.info("<<< ADD SYMBOL {}", cmd2);
                GatewaySymbolSpec newSpec = gatewayState.activateSymbol(symbolId);
                asyncCoreResponseCreated(asyncResponse, newSpec, cmd2.resultCode);
            });

            return asyncResponse;

        });

        On.post(SYNC_ADMIN_API_V1 + "assets").json((Req req, RestApiAsset cmd) -> {
            log.info(">>> {}", cmd);

            // TODO Publish through bus

            final GatewayAssetSpec spec = GatewayAssetSpec.builder()
                    .assetCode(cmd.assetCode)
                    .assetId(cmd.assetId)
                    .scale(cmd.scale)
                    .active(false)
                    .build();

            if (!gatewayState.registerNewAsset(spec)) {

                log.warn("Can not add asset, already exists");
                return errorResponse(req.response(), ApiErrorCodes.ASSET_ALREADY_EXISTS);
            } else {
                return successResponse(req.response(), cmd, 201, "new asset created");
            }
        });
    }

    @PreDestroy
    public void shutdown() {

        exchangeCore.shutdown();
    }

    public static Resp errorResponse(Resp resp, ApiErrorCodes errMessage, String... args) {
        String msg = String.format(errMessage.errorDescription, (Object[]) args);

        RestGenericResponse response = RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(errMessage.gatewayErrorCode)
                .coreResultCode(0)
                .description(msg)
                .build();

        resp.json(response);

        //resp.body(msg.getBytes());
        resp.code(errMessage.httpReturnCode);
        return resp;
    }

    public static Resp successResponse(Resp resp, Object data, int code, String description) {
        resp.code(code);
        resp.json(RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(0)
                .coreResultCode(0)
                .data(data)
                .description(description)
                .build());
        return resp;
    }

    public static void asyncCoreResponseCreated(Resp resp, Object data, CommandResultCode coreResult) {
        asyncCoreResponse(resp, data, coreResult, 201);
    }

    public static void asyncCoreResponseOk(Resp resp, Object data, CommandResultCode coreResult) {
        asyncCoreResponse(resp, data, coreResult, 200);
    }

    public static void asyncCoreResponse(Resp resp, Object data, CommandResultCode coreResult, int successCode) {

        resp.code(coreResult == CommandResultCode.SUCCESS ? successCode : 400);
        resp.json(RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(0)
                .coreResultCode(coreResult.getCode())
                .data(data)
                .description(coreResult.toString())
                .build());
        resp.done();

    }

}
