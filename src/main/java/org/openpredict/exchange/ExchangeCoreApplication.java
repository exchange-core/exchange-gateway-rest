package org.openpredict.exchange;

import org.openpredict.exchange.core.ExchangeCore;
import org.openpredict.exchange.core.journalling.DiskSerializationProcessor;
import org.openpredict.exchange.core.orderbook.OrderBookFastImpl;
import org.openpredict.exchange.rest.CommandEventsRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.openpredict.exchange.core.ExchangeCore.DisruptorWaitStrategy.BUSY_SPIN;
import static org.openpredict.exchange.core.ExchangeCore.DisruptorWaitStrategy.YIELDING;
import static org.openpredict.exchange.core.Utils.ThreadAffityMode.THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {
        "org.openpredict.exchange"
})
@PropertySource("application.properties")
@Configuration
public class ExchangeCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeCoreApplication.class, args);
    }

    @Bean
    public ExchangeCore exchangeCore(@Autowired CommandEventsRouter eventsRouter) {


        return ExchangeCore.builder()
                .resultsConsumer(eventsRouter)
                .serializationProcessor(new DiskSerializationProcessor("./dumps"))
                .ringBufferSize(4096)
                .matchingEnginesNum(1)
                .riskEnginesNum(1)
                .msgsInGroupLimit(1024)
                .threadAffityMode(THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE)
                .waitStrategy(YIELDING)
                .orderBookFactory(symbolType -> new OrderBookFastImpl(OrderBookFastImpl.DEFAULT_HOT_WIDTH, symbolType))
//                .orderBookFactory(OrderBookNaiveImpl::new)
//                .loadStateId(stateId) // Loading from persisted state
                .build();

    }


//    @Bean
//    public Consumer<OrderCommand> resultsConsumer() {
//        return cmd -> {
//            System.out.println(">>>" + cmd);
//        };
//    }

}
