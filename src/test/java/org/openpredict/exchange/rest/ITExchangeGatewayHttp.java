package org.openpredict.exchange.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpredict.exchange.beans.OrderAction;
import org.openpredict.exchange.beans.OrderType;
import org.openpredict.exchange.beans.SymbolType;
import org.openpredict.exchange.rest.commands.admin.RestApiAddSymbol;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.support.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {
        "org.openpredict.exchange.rest",
})
//@TestPropertySource(locations = "classpath:it.properties")
//@ActiveProfiles("local")
//@TestPropertySource(locations = "classpath:./it-local.properties")
@Slf4j
public class ITExchangeGatewayHttp {

    @Autowired
    private TestService testService;

    @Test
    public void contextStarts() {
    }

    @Test
    public void shouldAddNewAsset() throws Exception {
        testService.addAsset(new RestApiAsset("XBTC", 123, 8));
    }

    @Test
    public void shouldAddNewSymbol() throws Exception {

        testService.addAsset(new RestApiAsset("XBTC", 9123, 8));
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));

        testService.addSymbol(new RestApiAddSymbol(
                "XBTC_USDT",
                3199,
                SymbolType.CURRENCY_EXCHANGE_PAIR,
                "XBTC",
                "USDT",
                new BigDecimal("1000"),
                new BigDecimal("1"),
                new BigDecimal("0.08"),
                new BigDecimal("0.03"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50000"),
                new BigDecimal("1000")));
    }

    @Test
    public void shouldCreateUser() throws Exception {
        testService.createUser(123);
    }

    @Test
    public void shouldAdjustUserBalance() throws Exception {
        testService.createUser(7332);
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));
        testService.adjustUserBalance(7332, "USDT", new BigDecimal("192.44"), 59282713223L);
    }

    @Test
    public void shouldPlaceMoveCancelLimitOrder() throws Exception {
        testService.createUser(1001);
        testService.addAsset(new RestApiAsset("XBTC", 9123, 8));
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));

        testService.adjustUserBalance(1001, "USDT", new BigDecimal("2692.44"), 713223L);

        testService.addSymbol(new RestApiAddSymbol(
                "XBTC_USDT",
                3199,
                SymbolType.CURRENCY_EXCHANGE_PAIR,
                "XBTC",
                "USDT",
                new BigDecimal("0.1"),
                new BigDecimal("0.01"),
                new BigDecimal("0.08"),
                new BigDecimal("0.03"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50000"),
                new BigDecimal("1000")));


        long orderId = testService.placeOrder("XBTC_USDT", 1001, BigDecimal.valueOf(829.33), 3, 4124, OrderAction.BID, OrderType.GTC);
        testService.moveOrder(orderId, "XBTC_USDT", 1001, BigDecimal.valueOf(829.29));
        testService.cancelOrder(orderId, "XBTC_USDT", 1001);
    }
}