package org.openpredict.exchange.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.CoreSymbolSpecification;
import org.openpredict.exchange.beans.api.binary.BatchAddSymbolsCommand;
import org.openpredict.exchange.beans.cmd.OrderCommand;
import org.openpredict.exchange.core.ExchangeApi;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.GatewayState;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.commands.admin.RestApiAddSymbol;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.openpredict.exchange.rest.model.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.GatewaySymbolSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "syncAdminApi/v1/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class SyncAdminApiSymbolsController {

    @Autowired
    private ExchangeCore exchangeCore;

    @Autowired
    private GatewayState gatewayState;


    @RequestMapping(value = "assets", method = RequestMethod.POST)
    public ResponseEntity<RestGenericResponse> createAsset(@Valid @RequestBody RestApiAsset request) throws ExecutionException, InterruptedException {

        log.info(">>> ADD ASSET {}", request);

        // TODO Publish through bus
        final GatewayAssetSpec spec = GatewayAssetSpec.builder()
                .assetCode(request.assetCode)
                .assetId(request.assetId)
                .scale(request.scale)
                .active(false)
                .build();

        if (!gatewayState.registerNewAsset(spec)) {
            log.warn("Can not add asset, already exists");
            return RestControllerHelper.errorResponse(ApiErrorCodes.ASSET_ALREADY_EXISTS);
        } else {
            return RestControllerHelper.successResponse(request, HttpStatus.CREATED);
        }

    }

    @RequestMapping(value = "symbols", method = RequestMethod.POST)
    public ResponseEntity<RestGenericResponse> createSymbol(@Valid @RequestBody RestApiAddSymbol request) throws ExecutionException, InterruptedException {

        log.info("ADD SYMBOL >>> {}", request);

        // TODO Publish through bus

        final GatewayAssetSpec baseAsset = gatewayState.getAssetSpec(request.baseAsset);
        if (baseAsset == null) {
            log.warn("UNKNOWN_BASE_ASSET : " + request.baseAsset);
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_BASE_ASSET);
        }

        final GatewayAssetSpec quoteCurrency = gatewayState.getAssetSpec(request.quoteCurrency);
        if (quoteCurrency == null) {
            log.warn("UNKNOWN_QUOTE_CURRENCY : " + request.quoteCurrency);
            return RestControllerHelper.errorResponse(ApiErrorCodes.UNKNOWN_QUOTE_CURRENCY);
        }

        // TODO validations
        final int symbolId = request.symbolId;

        final GatewaySymbolSpec spec = GatewaySymbolSpec.builder()
                .symbolId(symbolId)
                .symbolCode(request.symbolCode)
                .symbolType(request.symbolType)
                .baseAsset(baseAsset)
                .quoteCurrency(quoteCurrency)
                .lotSize(request.lotSize)
                .stepSize(request.stepSize)
                .takerFee(request.takerFee)
                .makerFee(request.makerFee)
                .marginBuy(request.marginBuy)
                .marginSell(request.marginSell)
                .priceHighLimit(request.priceHighLimit)
                .priceLowLimit(request.priceLowLimit)
                .status(GatewaySymbolSpec.GatewaySymbolLifecycle.NEW)
                .build();

        if (!gatewayState.registerNewSymbol(spec)) {
            log.warn("SYMBOL_ALREADY_EXISTS : id={} code={}", symbolId, request.symbolCode);
            return RestControllerHelper.errorResponse(ApiErrorCodes.SYMBOL_ALREADY_EXISTS);
        }

        final CoreSymbolSpecification coreSpec = CoreSymbolSpecification.builder()
                .symbolId(symbolId)
                .type(request.symbolType)
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

        ExchangeApi api = exchangeCore.getApi();
        CompletableFuture<OrderCommand> future = new CompletableFuture<>();

        api.submitBinaryCommandAsync(batchAddSymbols, 123567, future::complete);

        //asyncCoreResponseCreated(asyncResponse, newSpec, cmd2.resultCode);

        OrderCommand orderCommand = future.get();

        log.info("<<< ADD SYMBOL {}", orderCommand);

        GatewaySymbolSpec newSpec = gatewayState.activateSymbol(symbolId);

        return RestControllerHelper.coreResponse(orderCommand, () -> newSpec, HttpStatus.CREATED);

    }


}
