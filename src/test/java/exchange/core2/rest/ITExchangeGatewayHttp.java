package exchange.core2.rest;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.SymbolType;
import exchange.core2.rest.commands.admin.RestApiAddSymbol;
import exchange.core2.rest.commands.admin.RestApiAsset;
import exchange.core2.rest.model.api.*;
import exchange.core2.rest.support.TestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {
        "exchange.core2.rest",
})
//@TestPropertySource(locations = "classpath:it.properties")
//@ActiveProfiles("local")
//@TestPropertySource(locations = "classpath:./it-local.properties")
@Slf4j
public class ITExchangeGatewayHttp {

    public static final String SYMBOL_XBTC_USDT = "XBTC_USDT";

    @Autowired
    private TestService testService;

    @Test
    @DirtiesContext
    public void contextStarts() {
    }

    @Test
    @DirtiesContext
    public void shouldAddNewAsset() throws Exception {
        testService.addAsset(new RestApiAsset("XBTC", 123, 8));
    }

    @Test
    @DirtiesContext
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
    @DirtiesContext
    public void shouldCreateUser() throws Exception {
        testService.createUser(123);
    }

    @Test
    @DirtiesContext
    public void shouldAdjustUserBalance() throws Exception {
        testService.createUser(7332);
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));
        testService.adjustUserBalance(7332, "USDT", new BigDecimal("192.44"), 59282713223L);
    }

    @Test
    @DirtiesContext
    public void shouldPlaceMoveCancelLimitOrder() throws Exception {
        int uid = 1001;
        testService.createUser(uid);
        testService.addAsset(new RestApiAsset("XBTC", 9123, 8));
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));

        final BigDecimal initialBalance = new BigDecimal("2692.44");
        testService.adjustUserBalance(uid, "USDT", initialBalance, 713223L);

        {
            final RestApiUserState userState = testService.getUserState(uid);
            assertThat(userState.accounts.size(), is(1));
            assertThat(userState.accounts.get(0).currency, is("USDT"));
            assertThat(userState.accounts.get(0).balance, is(initialBalance));
            assertTrue(userState.activeOrders.isEmpty());
        }

        final BigDecimal takerFee = new BigDecimal("0.08");
        final BigDecimal makerFee = new BigDecimal("0.03");

        testService.addSymbol(new RestApiAddSymbol(
                SYMBOL_XBTC_USDT,
                3199,
                SymbolType.CURRENCY_EXCHANGE_PAIR,
                "XBTC",
                "USDT",
                new BigDecimal("0.1"), // lot size
                new BigDecimal("0.01"), // step size
                takerFee,
                makerFee,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50000"),
                new BigDecimal("1000")));


        // place order
        BigDecimal price = new BigDecimal("829.33");
        long size = 3;

        int userCookie = 4124;
        long orderId = testService.placeOrder(SYMBOL_XBTC_USDT, uid, price, size, userCookie, OrderAction.BID, OrderType.GTC);

        RestApiOrderBook expected = RestApiOrderBook.builder()
                .symbol(SYMBOL_XBTC_USDT)
                .askPrices(Collections.emptyList())
                .askVolumes(Collections.emptyList())
                .bidPrices(Collections.singletonList(price))
                .bidVolumes(Collections.singletonList(3L))
                .build();

        assertThat(testService.getOrderBook(SYMBOL_XBTC_USDT), is(expected));

        {
            final RestApiUserState userState = testService.getUserState(uid);
            assertThat(userState.accounts.size(), is(1));
            final RestApiAccountState accountState = userState.accounts.get(0);
            assertThat(accountState.currency, is("USDT"));
            assertThat(accountState.balance, is(initialBalance.subtract(price.add(takerFee).multiply(BigDecimal.valueOf(size)))));

            assertThat(userState.activeOrders.size(), is(1));
            final RestApiOrder order = userState.activeOrders.get(0);
            assertThat(order.getAction(), is(OrderAction.BID));
            assertThat(order.getFilled(), is(0L));
            assertThat(order.getOrderId(), is(orderId));
            assertThat(order.getOrderType(), is(OrderType.GTC));
            assertThat(order.getPrice(), is(price));
            assertThat(order.getSize(), is(size));
            assertThat(order.getState(), is(OrderState.ACTIVE));
            assertThat(order.getUserCookie(), is(userCookie));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            assertTrue(order.getDeals().isEmpty());
        }

        // move order

        testService.moveOrder(orderId, "XBTC_USDT", uid, BigDecimal.valueOf(829.29));

        assertThat(testService.getOrderBook(SYMBOL_XBTC_USDT), is(expected.withBidPrices(Collections.singletonList(new BigDecimal("829.29")))));


        // cancel order

        testService.cancelOrder(orderId, "XBTC_USDT", uid);

        assertThat(testService.getOrderBook(SYMBOL_XBTC_USDT), is(expected.withBidPrices(Collections.emptyList()).withBidVolumes(Collections.emptyList())));

    }

    @Test
    @DirtiesContext
    public void shouldTradeLimitOrder() throws Exception {
        int uid1 = 1001;
        int uid2 = 1002;
        testService.createUser(uid1);
        testService.createUser(uid2);

        testService.addAsset(new RestApiAsset("XBTC", 9123, 8));
        testService.addAsset(new RestApiAsset("USDT", 3412, 2));

        final BigDecimal initialBalanceXbtc1 = new BigDecimal("0.31047729");
        testService.adjustUserBalance(uid1, "XBTC", initialBalanceXbtc1, 927910L);

        final BigDecimal initialBalanceUsdt2 = new BigDecimal("3627.29");
        testService.adjustUserBalance(uid2, "USDT", initialBalanceUsdt2, 713223L);

        final BigDecimal takerFee = new BigDecimal("0.08");
        final BigDecimal makerFee = new BigDecimal("0.03");

        final BigDecimal lotSize = new BigDecimal("0.1");

        testService.addSymbol(new RestApiAddSymbol(
                SYMBOL_XBTC_USDT,
                3199,
                SymbolType.CURRENCY_EXCHANGE_PAIR,
                "XBTC",
                "USDT",
                lotSize, // lot size
                new BigDecimal("0.01"), // step size
                takerFee,
                makerFee,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50000"),
                new BigDecimal("1000")));

        // place GTC ASK order 1
        BigDecimal price1 = new BigDecimal("829.33");
        long size1 = 3;

        int userCookie1 = 123;
        long orderId1 = testService.placeOrder(SYMBOL_XBTC_USDT, uid1, price1, size1, userCookie1, OrderAction.ASK, OrderType.GTC);

        BigDecimal expectedBalanceXbtc1 = initialBalanceXbtc1.subtract(lotSize.multiply(BigDecimal.valueOf(size1)));

        {
            final RestApiUserState userState = testService.getUserState(uid1);
            assertThat(userState.accounts.size(), is(1));
            final RestApiAccountState accountState = userState.accounts.get(0);
            assertThat(accountState.currency, is("XBTC"));
            assertThat(accountState.balance, comparesEqualTo(expectedBalanceXbtc1));

            assertThat(userState.activeOrders.size(), is(1));
            final RestApiOrder order = userState.activeOrders.get(0);
            assertThat(order.getAction(), is(OrderAction.ASK));
            assertThat(order.getFilled(), is(0L));
            assertThat(order.getOrderId(), is(orderId1));
            assertThat(order.getOrderType(), is(OrderType.GTC));
            assertThat(order.getPrice(), is(price1));
            assertThat(order.getSize(), is(size1));
            assertThat(order.getState(), is(OrderState.ACTIVE));
            assertThat(order.getUserCookie(), is(userCookie1));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            assertTrue(order.getDeals().isEmpty());
        }

        // sumbmit IoC BID order 2
        BigDecimal price2 = new BigDecimal("829.41");
        long size2 = 4; // 1 lot will be rejected


        int userCookie2 = 123; // other user can use the same cookie
        long orderId2 = testService.placeOrder(SYMBOL_XBTC_USDT, uid2, price2, size2, userCookie2, OrderAction.BID, OrderType.IOC);
        assertTrue(testService.getOrderBook(SYMBOL_XBTC_USDT).isEmpty());

        // check user1 state
        {
            final RestApiUserState userState = testService.getUserState(uid1);
            assertThat(userState.accounts.size(), is(2));
            Map<String, RestApiAccountState> accounts = userState.accounts.stream().collect(Collectors.toMap(a -> a.currency, a -> a));
            // 0.31047729 - (3 * 0.1)
            assertThat(accounts.get("XBTC").balance, comparesEqualTo(expectedBalanceXbtc1));
            // 0 + (829.33 - 0.03) * 3
            assertThat(accounts.get("USDT").balance, comparesEqualTo(price1.subtract(makerFee).multiply(BigDecimal.valueOf(size1))));

            assertThat(userState.activeOrders.size(), is(0));
        }

        // check user2 state
        {
            final RestApiUserState userState = testService.getUserState(uid2);
            assertThat(userState.accounts.size(), is(2));
            Map<String, RestApiAccountState> accounts = userState.accounts.stream().collect(Collectors.toMap(a -> a.currency, a -> a));
            // 0 + (3 * 0.1)
            assertThat(accounts.get("XBTC").balance, comparesEqualTo(lotSize.multiply(BigDecimal.valueOf(size1))));

            // /// 3627.29 - (829.33 + 0.07) * 3
            assertThat(accounts.get("USDT").balance, comparesEqualTo(initialBalanceUsdt2.subtract(price1.add(takerFee).multiply(BigDecimal.valueOf(size1)))));
            assertThat(userState.activeOrders.size(), is(0));
        }

    }
}