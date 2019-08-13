package org.openpredict.exchange.rest;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.core.CoreWaitStrategy;
import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.core.journalling.DiskSerializationProcessor;
import org.openpredict.exchange.core.orderbook.OrderBookFastImpl;
import org.openpredict.exchange.rest.controllers.RestSyncAdminApiController;
import org.openpredict.exchange.rest.controllers.RestSyncTradeApiController;
import org.rapidoid.setup.App;

import static org.openpredict.exchange.core.Utils.ThreadAffityMode.THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE;

@Slf4j
public class RestGatewayApplication {

    private final ExchangeCore exchangeCore;
    private final GatewayState gatewayState;

    public RestGatewayApplication() {

        gatewayState = new GatewayState();
        ;
        CommandEventsRouter commandEventsRouter = new CommandEventsRouter(gatewayState);

        exchangeCore = ExchangeCore.builder()
                .resultsConsumer(commandEventsRouter)
                .serializationProcessor(new DiskSerializationProcessor("./dumps"))
                .ringBufferSize(4096)
                .matchingEnginesNum(1)
                .riskEnginesNum(1)
                .msgsInGroupLimit(1024)
                .threadAffityMode(THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE)
                .waitStrategy(CoreWaitStrategy.SLEEPING)
                .orderBookFactory(symbolType -> new OrderBookFastImpl(OrderBookFastImpl.DEFAULT_HOT_WIDTH, symbolType))
//                .orderBookFactory(OrderBookNaiveImpl::new)
//                .loadStateId(stateId) // Loading from persisted state
                .build();

    }

    public void start() {

        log.info("Initializing Rapidoid...");

        //        App.bootstrap(new String[0]);
        App.bootstrap(new String[0], "profiles=dev", "on.address=0.0.0.0", "on.port=8080").jpa();
//        App.profiles("foo", "bar");

        RestSyncAdminApiController.init(exchangeCore.getApi(), gatewayState);
        RestSyncTradeApiController.init(exchangeCore.getApi(), gatewayState);

        log.info("Initializing exchange core...");

        exchangeCore.startup();

        log.info("Gateway ready");
    }

    public void stop() {
        App.shutdown();
        exchangeCore.shutdown();
    }

    public static void main(String[] args) {

        RestGatewayApplication app = new RestGatewayApplication();

        app.start();

    }

}
