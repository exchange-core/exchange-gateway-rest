package org.openpredict.exchange.rest;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.rest.model.internal.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.internal.GatewaySymbolSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


// TODO separate interfaces for admin and user
@Service
@Slf4j
public class GatewayState {

    private final AtomicInteger syncRequestsSequence = new AtomicInteger(0);

    // promises cache (TODO can be changed to queue?)

    private final Map<String, GatewaySymbolSpec> symbolsByCode = new ConcurrentHashMap<>();
    private final Map<Integer, GatewaySymbolSpec> symbolsById = new ConcurrentHashMap<>();

    private final Map<String, GatewayAssetSpec> assetsByCode = new ConcurrentHashMap<>();

    @Autowired
    private ExchangeCore exchangeCore;

    public GatewaySymbolSpec getSymbolSpec(String symbolCode) {
        return symbolsByCode.get(symbolCode);
    }

    public GatewaySymbolSpec getSymbolSpec(int symbolId) {
        return symbolsById.get(symbolId);
    }

    public boolean registerNewSymbol(GatewaySymbolSpec spec) {
        if (symbolsById.putIfAbsent(spec.symbolId, spec) == null) {
            symbolsByCode.put(spec.symbolCode, spec);
            return true;
        }
        return false;
    }

    public boolean registerNewAsset(GatewayAssetSpec spec) {

        // TODO implement validation and lifesycle
        return assetsByCode.putIfAbsent(spec.assetCode, spec) == null;
    }

    public GatewayAssetSpec getAssetSpec(String assetCode) {
        return assetsByCode.get(assetCode);
    }


    public GatewaySymbolSpec activateSymbol(final int symbolId) {

        final GatewaySymbolSpec newSpec = symbolsById.compute(symbolId, (k, v) ->
                (v != null && v.status == GatewaySymbolSpec.GatewaySymbolLifecycle.NEW)
                        ? v.withStatus(GatewaySymbolSpec.GatewaySymbolLifecycle.ACTIVE)
                        : null);

        if (newSpec != null) {
            symbolsByCode.put(newSpec.symbolCode, newSpec);
        }

        return newSpec;
    }

    @PostConstruct
    public void start(){
        log.debug("START1");
        exchangeCore.startup();
    }

    @PreDestroy
    public void stop(){
        log.debug("STOP1");
        exchangeCore.shutdown();
    }

}
