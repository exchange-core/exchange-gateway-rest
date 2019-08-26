package org.openpredict.exchange.rest.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.CoreSymbolSpecification;
import org.openpredict.exchange.beans.api.binary.BatchAddSymbolsCommand;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.admin.RestApiAddSymbol;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.model.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.GatewaySymbolSpec;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.setup.On;

@Slf4j
@AllArgsConstructor
public class RestSyncAdminApiSymbolsController {

    private static final String SYNC_ADMIN_API_V1 = "/syncAdminApi/v1/";

    public static void init(ExchangeApi api, GatewayState gatewayState) {

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


        On.post(SYNC_ADMIN_API_V1 + "symbols").json((final Req req, final RestApiAddSymbol cmd) -> {
            log.info("ADD SYMBOL >>> {}", cmd);

            // TODO Publish through bus

            final GatewayAssetSpec baseAsset = gatewayState.getAssetSpec(cmd.baseAsset);
            if (baseAsset == null) {
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.UNKNOWN_BASE_ASSET);
            }

            final GatewayAssetSpec quoteCurrency = gatewayState.getAssetSpec(cmd.quoteCurrency);
            if (quoteCurrency == null) {
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.UNKNOWN_QUOTE_CURRENCY);
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
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.SYMBOL_ALREADY_EXISTS);
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
                RestControllerHelper.asyncCoreResponseCreated(asyncResponse, newSpec, cmd2.resultCode);
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
                return RestControllerHelper.errorResponse(req.response(), ApiErrorCodes.ASSET_ALREADY_EXISTS);
            } else {
                return RestControllerHelper.successResponse(req.response(), cmd, 201, "new asset created");
            }
        });
    }


}
