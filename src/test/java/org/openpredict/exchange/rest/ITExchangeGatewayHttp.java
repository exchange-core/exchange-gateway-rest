package org.openpredict.exchange.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpredict.exchange.rest.support.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {
        "org.openpredict.exchange.rest",
})
// @TestPropertySource(locations = "classpath:it.properties")
//@ActiveProfiles("local")
//@TestPropertySource(locations = "classpath:./it-local.properties")
@Slf4j
public class ITExchangeGatewayHttp {

//    @MockBean
//    private Consumer<OrderCommand> resultsConsumerMock;

    @Autowired
    private TestService testService;

    @Test
    public void contextStarts() {

    }

    @Test
    public void shouldCreateUser() throws Exception {
        testService.createUser(123);
    }

    @Test
    public void shouldAddNewAsset() throws Exception {
        //testService.addAsset(new RestApiAsset("XBT", 123, 8));
    }

}