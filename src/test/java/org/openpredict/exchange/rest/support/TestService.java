package org.openpredict.exchange.rest.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.rest.commands.admin.RestApiAddUser;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.events.RestGenericResponse;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


//import kong.unirest.ObjectMapper;


@Slf4j
public class TestService extends TestSupport {

    //public static final String LOCAL_SERVICE = "http://localhost:8080";
    public static final String LOCAL_SERVICE = "http://192.168.0.51:8080";
    public static final String SYNC_ADMIN_API_V1 = LOCAL_SERVICE + "/syncAdminApi/v1/";

//    @Autowired
//    private ApplicationContext applicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    public TestService() {
        Unirest.config().setObjectMapper(new kong.unirest.ObjectMapper() {
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return objectMapper.readValue(value, valueType);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            @Override
            public String writeValue(Object value) {
                try {
                    return objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
    }

    public void addAsset(RestApiAsset addAsset) throws Exception {

        HttpResponse<String> response = Unirest.post(SYNC_ADMIN_API_V1 + "assets")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(addAsset)
                .asString();

        assertThat(response.getStatus(), is(201));

        RestGenericResponse<RestApiAsset> respJson = objectMapper.readValue(response.getBody(), new TypeReference<RestGenericResponse<RestApiAsset>>() {
        });

        assertThat(respJson.getGatewayResultCode(), is(0));
        assertThat(respJson.getCoreResultCode(), is(0));

        RestApiAsset asset = respJson.getData();
        assertThat(asset.assetCode, is(addAsset.assetCode));
        assertThat(asset.assetId, is(addAsset.assetId));
        assertThat(asset.scale, is(addAsset.scale));

    }

    public void getOrderBook(String symbol) throws Exception {

        HttpResponse<String> accept = Unirest.get(SYNC_ADMIN_API_V1 + "symbols/" + symbol + "/orderBook")
                .header("accept", "application/json")
                .asString();

        log.debug("Response: " + accept);

        // TODO implement
    }

    public void createAccount(long uid) throws Exception {

        HttpResponse<String> accept1 = Unirest.post(SYNC_ADMIN_API_V1 + "users")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(new RestApiAddUser(uid))
                .asString();

        log.debug("Response: " + accept1.getBody());

//        Thread.sleep(100000000);

    }

}
