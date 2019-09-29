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
import exchange.core2.core.common.CoreWaitStrategy;
import exchange.core2.core.orderbook.OrderBookFastImpl;
import exchange.core2.core.processors.journalling.DiskSerializationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static exchange.core2.core.utils.UnsafeUtils.ThreadAffinityMode.THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"exchange.core2.rest"})
//@PropertySource("application.properties")
@Configuration
@Slf4j
public class RestGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestGatewayApplication.class, args);
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
                .threadAffinityMode(THREAD_AFFINITY_ENABLE_PER_LOGICAL_CORE)
                .waitStrategy(CoreWaitStrategy.SLEEPING)
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
