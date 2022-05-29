package exchange.core2.rest;

import exchange.core2.core.ExchangeCore;
import exchange.core2.core.common.config.ExchangeConfiguration;
import exchange.core2.core.common.config.InitialStateConfiguration;
import exchange.core2.core.common.config.LoggingConfiguration;
import exchange.core2.core.common.config.OrdersProcessingConfiguration;
import exchange.core2.core.common.config.PerformanceConfiguration;
import exchange.core2.core.common.config.ReportsQueriesConfiguration;
import exchange.core2.core.common.config.SerializationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Created by qdd on 2022/5/26.
 */
@Configuration
public class Config {

    @Autowired
    CommandEventsRouter eventsRouter;

    @Bean
    public ExchangeCore exchangeCore() {
        final ExchangeConfiguration exchangeConfiguration = ExchangeConfiguration.defaultBuilder()
            .initStateCfg(InitialStateConfiguration.CLEAN_TEST)
            .performanceCfg(PerformanceConfiguration.baseBuilder().build())
            .reportsQueriesCfg(ReportsQueriesConfiguration.createStandardConfig())
            .ordersProcessingCfg(OrdersProcessingConfiguration.DEFAULT)
            .loggingCfg(LoggingConfiguration.DEFAULT)
            .serializationCfg(SerializationConfiguration.DEFAULT)
            .build();

        ExchangeCore core = ExchangeCore.builder()
            .resultsConsumer(eventsRouter)
            .exchangeConfiguration(exchangeConfiguration)
            .build();

        core.startup();

        return core;
    }
}
