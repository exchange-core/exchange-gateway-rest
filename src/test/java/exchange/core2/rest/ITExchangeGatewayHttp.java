package exchange.core2.rest;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.SymbolType;
import exchange.core2.rest.commands.admin.RestApiAddSymbol;
import exchange.core2.rest.commands.admin.RestApiAdminAsset;
import exchange.core2.rest.events.MatchingRole;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
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
    private TestService gatewayTestClient;

    @Test
    @DirtiesContext
    public void contextStarts() {
    }

    @Test
    @DirtiesContext
    public void shouldAddNewAsset() throws Exception {
        gatewayTestClient.addAsset(new RestApiAdminAsset("XBTC", 123, 8));
    }

    @Test
    @DirtiesContext
    public void shouldAddNewSymbol() throws Exception {

        gatewayTestClient.addAsset(new RestApiAdminAsset("XBTC", 9123, 8));
        gatewayTestClient.addAsset(new RestApiAdminAsset("USDT", 3412, 2));

        gatewayTestClient.addSymbol(new RestApiAddSymbol(
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
        gatewayTestClient.createUser(123);
    }

    @Test
    @DirtiesContext
    public void shouldAdjustUserBalance() throws Exception {
        gatewayTestClient.createUser(7332);
        gatewayTestClient.addAsset(new RestApiAdminAsset("USDT", 3412, 2));
        gatewayTestClient.adjustUserBalance(7332, "USDT", new BigDecimal("192.44"), 59282713223L);
    }

    @Test
    @DirtiesContext
    public void shouldPlaceMoveCancelLimitOrder() throws Exception {
        final int uid = 1001;
        gatewayTestClient.createUser(uid);
        gatewayTestClient.addAsset(new RestApiAdminAsset("XBTC", 9123, 8));
        gatewayTestClient.addAsset(new RestApiAdminAsset("USDT", 3412, 2));

        final BigDecimal initialBalance = new BigDecimal("2692.44");
        gatewayTestClient.adjustUserBalance(uid, "USDT", initialBalance, 713223L);

        {
            final RestApiUserState userState = gatewayTestClient.getUserState(uid);
            assertThat(userState.accounts.size(), is(1));
            assertThat(userState.accounts.get(0).currency, is("USDT"));
            assertThat(userState.accounts.get(0).balance, is(initialBalance));
            assertTrue(userState.activeOrders.isEmpty());
        }

        final BigDecimal takerFee = new BigDecimal("0.08");
        final BigDecimal makerFee = new BigDecimal("0.03");

        gatewayTestClient.addSymbol(new RestApiAddSymbol(
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
        final BigDecimal price = new BigDecimal("829.33");
        final long size = 3;

        final int userCookie = 4124;
        final long orderId = gatewayTestClient.placeOrder(SYMBOL_XBTC_USDT, uid, price, size, userCookie, OrderAction.BID, OrderType.GTC);

        RestApiOrderBook expected = RestApiOrderBook.builder()
                .symbol(SYMBOL_XBTC_USDT)
                .askPrices(Collections.emptyList())
                .askVolumes(Collections.emptyList())
                .bidPrices(Collections.singletonList(price))
                .bidVolumes(Collections.singletonList(3L))
                .build();

        assertThat(gatewayTestClient.getOrderBook(SYMBOL_XBTC_USDT), is(expected));

        {
            final RestApiUserState userState = gatewayTestClient.getUserState(uid);
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
            assertThat(order.getState(), is(GatewayOrderState.ACTIVE));
            assertThat(order.getUserCookie(), is(userCookie));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            assertTrue(order.getDeals().isEmpty());
        }

        // move order

        gatewayTestClient.moveOrder(orderId, "XBTC_USDT", uid, BigDecimal.valueOf(829.29));

        final BigDecimal newPrice = new BigDecimal("829.29");
        assertThat(gatewayTestClient.getOrderBook(SYMBOL_XBTC_USDT), is(expected.withBidPrices(Collections.singletonList(newPrice))));
        assertThat(gatewayTestClient.getUserState(uid).activeOrders.get(0).getPrice(), is(newPrice));

        // cancel order

        gatewayTestClient.cancelOrder(orderId, "XBTC_USDT", uid);

        assertThat(gatewayTestClient.getOrderBook(SYMBOL_XBTC_USDT), is(expected.withBidPrices(Collections.emptyList()).withBidVolumes(Collections.emptyList())));
        assertThat(gatewayTestClient.getUserState(uid).activeOrders.size(), is(0));

        final RestApiUserTradesHistory history = gatewayTestClient.getUserTradesHistory(uid);
        assertThat(history.orders.size(), is(1));
        final RestApiOrder order = history.orders.get(0);
        assertThat(order.getOrderId(), is(orderId));
        assertThat(order.getPrice(), is(newPrice));
        assertThat(order.getSize(), is(3L));
        assertThat(order.getFilled(), is(0L));
        assertThat(order.getState(), is(GatewayOrderState.CANCELLED));
        assertThat(order.getUserCookie(), is(userCookie));
        assertThat(order.getAction(), is(OrderAction.BID));
        assertThat(order.getOrderType(), is(OrderType.GTC));
        assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
        assertThat(order.getDeals().size(), is(0));

    }

    @Test
    @DirtiesContext
    public void shouldTradeLimitOrder() throws Exception {
        final int uid1 = 1001;
        final int uid2 = 1002;
        gatewayTestClient.createUser(uid1);
        gatewayTestClient.createUser(uid2);

        gatewayTestClient.addAsset(new RestApiAdminAsset("XBTC", 9123, 8));
        gatewayTestClient.addAsset(new RestApiAdminAsset("USDT", 3412, 2));

        final BigDecimal initialBalanceXbtc1 = new BigDecimal("0.31047729");
        gatewayTestClient.adjustUserBalance(uid1, "XBTC", initialBalanceXbtc1, 927910L);

        final BigDecimal initialBalanceUsdt2 = new BigDecimal("3627.29");
        gatewayTestClient.adjustUserBalance(uid2, "USDT", initialBalanceUsdt2, 713223L);

        final BigDecimal takerFee = new BigDecimal("0.08");
        final BigDecimal makerFee = new BigDecimal("0.03");

        final BigDecimal lotSize = new BigDecimal("0.1");

        gatewayTestClient.addSymbol(new RestApiAddSymbol(
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

        {
            // check exchange info data
            RestApiExchangeInfo exchangeInfo = gatewayTestClient.getExchangeInfo();
            assertThat(exchangeInfo.getAssets().size(), is(2));
            final Map<String, RestApiAsset> assets = exchangeInfo.getAssets().stream().collect(Collectors.toMap(RestApiAsset::getAssetCode, a -> a));
            assertThat(assets.get("XBTC").getScale(), is(8));
            assertThat(assets.get("USDT").getScale(), is(2));

            assertThat(exchangeInfo.getSymbols().size(), is(1));
            RestApiSymbol symbol = exchangeInfo.getSymbols().get(0);

            assertThat(symbol.getSymbolCode(), is(SYMBOL_XBTC_USDT));
            assertThat(symbol.getSymbolType(), is(SymbolType.CURRENCY_EXCHANGE_PAIR));
            assertThat(symbol.getBaseAsset(), is("XBTC"));
            assertThat(symbol.getQuoteCurrency(), is("USDT"));
            assertThat(symbol.getLotSize(), is(lotSize));
            assertThat(symbol.getStepSize(), is(new BigDecimal("0.01")));
            assertThat(symbol.getTakerFee(), is(takerFee));
            assertThat(symbol.getMakerFee(), is(makerFee));
            assertThat(symbol.getMarginBuy(), is(BigDecimal.ZERO));
            assertThat(symbol.getMarginSell(), is(BigDecimal.ZERO));
            assertThat(symbol.getPriceHighLimit(), is(new BigDecimal("50000")));
            assertThat(symbol.getPriceLowLimit(), is(new BigDecimal("1000")));
        }

        // place GTC ASK order 1
        final BigDecimal price1 = new BigDecimal("829.33");
        final long size1 = 3;

        final int userCookie1 = 123;
        final long orderId1 = gatewayTestClient.placeOrder(SYMBOL_XBTC_USDT, uid1, price1, size1, userCookie1, OrderAction.ASK, OrderType.GTC);

        final BigDecimal expectedBalanceXbtc1 = initialBalanceXbtc1.subtract(lotSize.multiply(BigDecimal.valueOf(size1)));

        {
            final RestApiUserState userState = gatewayTestClient.getUserState(uid1);
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
            assertThat(order.getState(), is(GatewayOrderState.ACTIVE));
            assertThat(order.getUserCookie(), is(userCookie1));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            assertTrue(order.getDeals().isEmpty());

            // no bars yet
            List<RestApiBar> bars = gatewayTestClient.getBars(SYMBOL_XBTC_USDT, TimeFrame.M1, 100);
            assertTrue(bars.isEmpty());
        }

        // submit IoC BID order 2
        final BigDecimal price2 = new BigDecimal("829.41");
        final long size2 = 4; // 1 lot will be rejected


        final int userCookie2 = 123; // other user can use the same cookie
        final long orderId2 = gatewayTestClient.placeOrder(SYMBOL_XBTC_USDT, uid2, price2, size2, userCookie2, OrderAction.BID, OrderType.IOC);
        assertTrue(gatewayTestClient.getOrderBook(SYMBOL_XBTC_USDT).isEmpty());

        {
            // check user1 state
            final RestApiUserState userState = gatewayTestClient.getUserState(uid1);
            assertThat(userState.accounts.size(), is(2));
            final Map<String, RestApiAccountState> accounts = userState.accounts.stream().collect(Collectors.toMap(a -> a.currency, a -> a));
            // 0.31047729 - (3 * 0.1)
            assertThat(accounts.get("XBTC").balance, comparesEqualTo(expectedBalanceXbtc1));
            // 0 + (829.33 - 0.03) * 3
            assertThat(accounts.get("USDT").balance, comparesEqualTo(price1.subtract(makerFee).multiply(BigDecimal.valueOf(size1))));

            assertThat(userState.activeOrders.size(), is(0));

            // check user1 history
            final RestApiUserTradesHistory history = gatewayTestClient.getUserTradesHistory(uid1);
            assertThat(history.orders.size(), is(1));
            final RestApiOrder order = history.orders.get(0);
            assertThat(order.getOrderId(), is(orderId1));
            assertThat(order.getPrice(), is(price1));
            assertThat(order.getSize(), is(3L));
            assertThat(order.getFilled(), is(3L));
            assertThat(order.getState(), is(GatewayOrderState.COMPLETED));
            assertThat(order.getUserCookie(), is(userCookie1));
            assertThat(order.getAction(), is(OrderAction.ASK));
            assertThat(order.getOrderType(), is(OrderType.GTC));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            final List<RestApiDeal> deals = order.getDeals();
            assertThat(deals.size(), is(1));
            assertThat(deals.get(0).getParty(), is(MatchingRole.MAKER));
            assertThat(deals.get(0).getPrice(), is(price1));
            assertThat(deals.get(0).getSize(), is(3L));
        }

        {
            // check user2 state
            final RestApiUserState userState = gatewayTestClient.getUserState(uid2);
            assertThat(userState.accounts.size(), is(2));
            final Map<String, RestApiAccountState> accounts = userState.accounts.stream().collect(Collectors.toMap(a -> a.currency, a -> a));
            // 0 + (3 * 0.1)
            assertThat(accounts.get("XBTC").balance, comparesEqualTo(lotSize.multiply(BigDecimal.valueOf(size1))));

            // /// 3627.29 - (829.33 + 0.07) * 3
            assertThat(accounts.get("USDT").balance, comparesEqualTo(initialBalanceUsdt2.subtract(price1.add(takerFee).multiply(BigDecimal.valueOf(size1)))));
            assertThat(userState.activeOrders.size(), is(0));

            // check user2 history
            final RestApiUserTradesHistory history = gatewayTestClient.getUserTradesHistory(uid2);
            assertThat(history.orders.size(), is(1));
            final RestApiOrder order = history.orders.get(0);
            assertThat(order.getOrderId(), is(orderId2));
            assertThat(order.getPrice(), is(price2));
            assertThat(order.getSize(), is(4L));
            assertThat(order.getFilled(), is(3L));
            assertThat(order.getState(), is(GatewayOrderState.REJECTED));
            assertThat(order.getUserCookie(), is(userCookie2));
            assertThat(order.getAction(), is(OrderAction.BID));
            assertThat(order.getOrderType(), is(OrderType.IOC));
            assertThat(order.getSymbol(), is(SYMBOL_XBTC_USDT));
            final List<RestApiDeal> deals = order.getDeals();
            assertThat(deals.size(), is(1));
            assertThat(deals.get(0).getParty(), is(MatchingRole.TAKER));
            assertThat(deals.get(0).getPrice(), is(price1));
            assertThat(deals.get(0).getSize(), is(3L));
        }

        {
            // verify bars
            List<RestApiBar> bars = gatewayTestClient.getBars(SYMBOL_XBTC_USDT, TimeFrame.M1, 100);
            assertThat(bars.size(), is(1));
            RestApiBar bar = bars.get(0);
            log.debug("Bar: {}", bar);
            assertThat(bar.getOpen(), is(price1));
            assertThat(bar.getHigh(), is(price1));
            assertThat(bar.getLow(), is(price1));
            assertThat(bar.getClose(), is(price1));
            assertThat(bar.getVolume(), is(3L));
            assertThat(bar.getTimestamp(), greaterThan(0L));
        }

    }
}