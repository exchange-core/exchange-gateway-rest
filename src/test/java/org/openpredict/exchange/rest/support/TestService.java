package org.openpredict.exchange.rest.support;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.rest.commands.admin.RestApiAddSymbol;
import org.openpredict.exchange.rest.commands.admin.RestApiAddUser;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Service
@Slf4j
public class TestService extends TestSupport {


    @Autowired
    private ApplicationContext applicationContext;


    //public static final String LOCAL_SERVICE = "http://localhost:8080";
    public static final String SYNC_ADMIN_API_V1 = "/syncAdminApi/v1/";

//    @Autowired
//    private ApplicationContext applicationContext;

    //    private ObjectMapper objectMapper = new ObjectMapper();
//
//    public TestService() {
//        Unirest.config().setObjectMapper(new kong.unirest.ObjectMapper() {
//            @Override
//            public <T> T readValue(String value, Class<T> valueType) {
//                try {
//                    return objectMapper.readValue(value, valueType);
//                } catch (IOException ex) {
//                    throw new IllegalStateException(ex);
//                }
//            }
//
//            @Override
//            public String writeValue(Object value) {
//                try {
//                    return objectMapper.writeValueAsString(value);
//                } catch (JsonProcessingException ex) {
//                    throw new IllegalStateException(ex);
//                }
//            }
//        });
//    }
//
    public void addAsset(RestApiAsset newAsset) throws Exception {

        String triggerUrl = SYNC_ADMIN_API_V1 + "/" + "assets";

        String rawRequest = json(newAsset);
        log.debug("request: \n{}", rawRequest);


        MvcResult result = mockMvc.perform(post(triggerUrl).content(rawRequest).contentType(applicationJson))
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

        String triggerUrl = SYNC_ADMIN_API_V1 + "/" + "symbols";

        String rawRequest = json(newSymbol);
        log.debug("request: \n{}", rawRequest);

        MvcResult result = mockMvc.perform(post(triggerUrl).content(rawRequest).contentType(applicationJson))
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

        String triggerUrl = SYNC_ADMIN_API_V1 + "/" + "users";

        RestApiAddUser request = new RestApiAddUser(uid);

        String rawRequest = json(request);
        log.debug("request: \n{}", rawRequest);


        MvcResult result = mockMvc.perform(post(triggerUrl).content(rawRequest).contentType(applicationJson))
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

}
