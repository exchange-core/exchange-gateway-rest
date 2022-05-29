package exchange.core2.rest.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.rest.commands.RestApiMoveOrder;
import exchange.core2.rest.commands.RestApiPlaceOrder;
import exchange.core2.rest.commands.admin.RestApiAccountBalanceAdjustment;
import exchange.core2.rest.commands.admin.RestApiAddSymbol;
import exchange.core2.rest.commands.admin.RestApiAddUser;
import exchange.core2.rest.commands.admin.RestApiAdminAsset;
import exchange.core2.rest.events.RestGenericResponse;
import exchange.core2.rest.model.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Service
@Slf4j
public class TestService extends TestSupport {


    @Autowired
    private ApplicationContext applicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    //public static final String LOCAL_SERVICE = "http://localhost:8080";
    public static final String SYNC_ADMIN_API_V1 = "/syncAdminApi/v1/";
    public static final String SYNC_TRADE_API_V1 = "/syncTradeApi/v1/";

    public static final String STOMP_TOPIC_TICKS_PREFIX = "/topic/ticks/";
    public static final String STOMP_TOPIC_ORDERS_PREFIX = "/topic/orders/uid/";

//    @Autowired
//    private ApplicationContext applicationContext;

    public void addAsset(RestApiAdminAsset newAsset) throws Exception {

        String url = SYNC_ADMIN_API_V1 + "/" + "assets";

        String rawRequest = json(newAsset);
        log.debug("request: \n{}", rawRequest);


        MvcResult result = mockMvc.perform(post(url).content(rawRequest).contentType(applicationJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(applicationJson))
                .andExpect(jsonPath("$.data.assetCode", is(newAsset.assetCode)))
                .andExpect(jsonPath("$.data.assetId", is(newAsset.assetId)))
                .andExpect(jsonPath("$.data.scale", is(newAsset.scale)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(0)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();

        log.debug("contentAsString=" + contentAsString);
    }

    public void addSymbol(RestApiAddSymbol newSymbol) throws Exception {

        String url = SYNC_ADMIN_API_V1 + "/" + "symbols";

        String rawRequest = json(newSymbol);
        log.debug("request: \n{}", rawRequest);

        MvcResult result = mockMvc.perform(post(url).content(rawRequest).contentType(applicationJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(applicationJson))
//                .andExpect(jsonPath("$.data.assetCode", is(newAsset.assetCode)))
//                .andExpect(jsonPath("$.data.assetId", is(newAsset.assetId)))
//                .andExpect(jsonPath("$.data.scale", is(newAsset.scale)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();

        log.debug("contentAsString=" + contentAsString);
    }

    //    public void getOrderBook(String symbol) throws Exception {
//
//        HttpResponse<String> accept = Unirest.get(SYNC_ADMIN_API_V1 + "symbols/" + symbol + "/orderBook")
//                .header("accept", "application/json")
//                .asString();
//
//        log.debug("Response: " + accept);
//
//        // TODO implement
//    }
//
    public void createUser(long uid) throws Exception {

        String url = SYNC_ADMIN_API_V1 + "/" + "users";

        RestApiAddUser request = new RestApiAddUser(uid);

        String rawRequest = json(request);
        log.debug("request: \n{}", rawRequest);

        ResultActions perform = mockMvc.perform(post(url).content(rawRequest).contentType(applicationJson));
        MvcResult result = perform
                .andExpect(status().isCreated())
                .andExpect(content().contentType(applicationJson))
                .andExpect(jsonPath("$.data", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();

        log.debug("contentAsString=" + contentAsString);

        // ZonedDateTime startTime = ZonedDateTime.parse(JsonPath.read(contentAsString, "$.task.updatedTime"));

//        HttpResponse<String> accept1 = Unirest.post(SYNC_ADMIN_API_V1 + "users")
//                .header("accept", "application/json")
//                .header("Content-Type", "application/json")
//                .body(new RestApiAddUser(uid))
//                .asString();
//
//        log.debug("Response: " + accept1.getBody());

//        Thread.sleep(100000000);
    }


    public void adjustUserBalance(long uid, String currency, BigDecimal amount, long transactionId) throws Exception {

        String url = SYNC_ADMIN_API_V1 + "/users/" + uid + "/accounts";

        RestApiAccountBalanceAdjustment request = new RestApiAccountBalanceAdjustment(transactionId, amount, currency);

        String rawRequest = json(request);
        log.debug("request: \n{}", rawRequest);


        MvcResult result = mockMvc.perform(post(url).content(rawRequest).contentType(applicationJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(applicationJson))
                //.andExpect(jsonPath("$.data", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();

        log.debug("contentAsString=" + contentAsString);
    }

    public long placeOrder(String symbol, long uid, BigDecimal price, long size, int userCookie, OrderAction action, OrderType type) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/symbols/%s/trade/%d/orders", symbol, uid);

        RestApiPlaceOrder request = new RestApiPlaceOrder(price, size, userCookie, action, type);

        String rawRequest = json(request);
        log.debug("request: \n{}", rawRequest);

        MvcResult result = mockMvc.perform(post(url).content(rawRequest).contentType(applicationJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(applicationJson))
                //.andExpect(jsonPath("$.data", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        log.debug("contentAsString=" + contentAsString);

        long orderId = JsonPath.parse(contentAsString).read("$.data.orderId", Long.class);
        log.debug("orderId=" + contentAsString);
        return orderId;
    }

    public void moveOrder(long orderId, String symbol, long uid, BigDecimal price) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/symbols/%s/trade/%d/orders/%d", symbol, uid, orderId);

        RestApiMoveOrder request = new RestApiMoveOrder(price);

        String rawRequest = json(request);
        log.debug("request: \n{}", rawRequest);


        MvcResult result = mockMvc.perform(put(url).content(rawRequest).contentType(applicationJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
                //.andExpect(jsonPath("$.data", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        log.debug("contentAsString=" + contentAsString);
    }


    public void cancelOrder(long orderId, String symbol, long uid) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/symbols/%s/trade/%d/orders/%d", symbol, uid, orderId);

        MvcResult result = mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
                //.andExpect(jsonPath("$.data", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        log.debug("contentAsString=" + contentAsString);
    }

    public RestApiOrderBook getOrderBook(String symbol) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/symbols/%s/orderbook", symbol);

        MvcResult result = mockMvc.perform(get(url).param("depth", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
                .andExpect(jsonPath("$.data.symbol", is(symbol)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        log.debug("contentAsString=" + contentAsString);
        TypeReference<RestGenericResponse<RestApiOrderBook>> typeReference = new TypeReference<RestGenericResponse<RestApiOrderBook>>() {
        };
        RestGenericResponse<RestApiOrderBook> response = objectMapper.readValue(contentAsString, typeReference);
        return response.getData();
    }

    public RestApiUserState getUserState(long uid) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/users/%d/state", uid);

        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
                .andExpect(jsonPath("$.data.uid", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
//                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        TypeReference<RestGenericResponse<RestApiUserState>> typeReference = new TypeReference<RestGenericResponse<RestApiUserState>>() {
        };
        RestGenericResponse<RestApiUserState> response = objectMapper.readValue(contentAsString, typeReference);
        return response.getData();
    }


    public RestApiUserTradesHistory getUserTradesHistory(long uid) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/users/%d/history", uid);

        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
                .andExpect(jsonPath("$.data.uid", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
//                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        TypeReference<RestGenericResponse<RestApiUserTradesHistory>> typeReference = new TypeReference<RestGenericResponse<RestApiUserTradesHistory>>() {
        };
        RestGenericResponse<RestApiUserTradesHistory> response = objectMapper.readValue(contentAsString, typeReference);
        return response.getData();
    }

    public List<RestApiBar> getBars(String symbolCode, TimeFrame timeFrame, int barsNum) throws Exception {

        String url = SYNC_TRADE_API_V1 + String.format("/symbols/%s/bars/%s/", symbolCode, timeFrame);

        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
//                .andExpect(jsonPath("$.data.uid", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
//                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        TypeReference<RestGenericResponse<List<RestApiBar>>> typeReference = new TypeReference<RestGenericResponse<List<RestApiBar>>>() {
        };
        RestGenericResponse<List<RestApiBar>> response = objectMapper.readValue(contentAsString, typeReference);
        return response.getData();
    }

    public RestApiExchangeInfo getExchangeInfo() throws Exception {

        String url = SYNC_TRADE_API_V1 + "/info/";

        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(applicationJson))
//                .andExpect(jsonPath("$.data.uid", is((int) uid)))
                .andExpect(jsonPath("$.gatewayResultCode", is(0)))
//                .andExpect(jsonPath("$.coreResultCode", is(100)))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        TypeReference<RestGenericResponse<RestApiExchangeInfo>> typeReference = new TypeReference<RestGenericResponse<RestApiExchangeInfo>>() {
        };
        RestGenericResponse<RestApiExchangeInfo> response = objectMapper.readValue(contentAsString, typeReference);
        return response.getData();
    }

}