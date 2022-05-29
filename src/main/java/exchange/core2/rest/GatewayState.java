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
package exchange.core2.rest;

import exchange.core2.core.ExchangeCore;
import exchange.core2.rest.model.api.TimeFrame;
import exchange.core2.rest.model.internal.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


// TODO separate interfaces for admin and user
@Service
@Slf4j
public class GatewayState {

    private final AtomicInteger syncRequestsSequence = new AtomicInteger(0);

    // promises cache (TODO can be changed to queue?)

    private final Map<String, GatewaySymbolSpec> symbolsByCode = new ConcurrentHashMap<>();
    private final Map<Integer, GatewaySymbolSpec> symbolsById = new ConcurrentHashMap<>();

    private final Map<String, GatewayAssetSpec> assetsByCode = new ConcurrentHashMap<>();
    private final Map<Integer, GatewayAssetSpec> assetsById = new ConcurrentHashMap<>();

    private final Map<Long, GatewayUserProfile> userProfiles = new ConcurrentHashMap<>();

    private final Map<String, ChartData> charts = new ConcurrentHashMap<>();

//    @Autowired
//    private ExchangeCore exchangeCore;

    public GatewaySymbolSpec getSymbolSpec(String symbolCode) {
        return symbolsByCode.get(symbolCode);
    }

    public GatewaySymbolSpec getSymbolSpec(int symbolId) {
        return symbolsById.get(symbolId);
    }

    public boolean registerNewSymbol(GatewaySymbolSpec spec) {
        if (symbolsById.putIfAbsent(spec.symbolId, spec) == null) {
            symbolsByCode.put(spec.symbolCode, spec);
            charts.put(spec.symbolCode, new ChartData());
            return true;
        }
        return false;
    }

    public <T> List<T> getActiveAssets(Function<GatewayAssetSpec, T> mapper) {
        return assetsById.values().stream()
                .filter(a -> a.active)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public <T> List<T> getActiveSymbols(Function<GatewaySymbolSpec, T> mapper) {
        return symbolsById.values().stream()
                .filter(s -> s.status == GatewaySymbolSpec.GatewaySymbolLifecycle.ACTIVE)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public boolean registerNewAsset(GatewayAssetSpec spec) {

        // TODO implement validation and lifesycle
        if (assetsByCode.putIfAbsent(spec.assetCode, spec) == null) {
            assetsById.put(spec.assetId, spec);
            return true;
        }
        return false;
    }

    public GatewayAssetSpec getAssetSpec(String assetCode) {
        return assetsByCode.get(assetCode);
    }

    public GatewayAssetSpec getAssetSpec(int assetId) {
        return assetsById.get(assetId);
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

    public Optional<GatewayUserProfile> getUserProfile(long uid) {
        return Optional.ofNullable(userProfiles.get(uid));
    }

    public GatewayUserProfile getOrCreateUserProfile(long uid) {
        return userProfiles.computeIfAbsent(uid, k -> new GatewayUserProfile());
    }

    public void addTicks(String symbol, List<TickRecord> records) {
        charts.compute(symbol, (s, chart) -> chart == null ? new ChartData() : chart).addTicks(records);
    }

    public Optional<List<GatewayBarStatic>> getBars(String symbolCode, int barsNum, TimeFrame timeFrame) {
        return Optional.ofNullable(charts.get(symbolCode)).map(chartData -> chartData.getBarsData(barsNum, timeFrame));
    }
//
//    @PostConstruct
//    public void start() {
//        log.debug("START1");
//        exchangeCore.startup();
//    }
//
//    @PreDestroy
//    public void stop() {
//        log.debug("STOP1");
//        exchangeCore.shutdown();
//    }

}
