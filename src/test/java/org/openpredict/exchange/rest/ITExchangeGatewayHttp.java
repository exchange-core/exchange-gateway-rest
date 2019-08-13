package org.openpredict.exchange.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpredict.exchange.rest.commands.admin.RestApiAsset;
import org.openpredict.exchange.rest.support.TestService;

@Slf4j
public class ITExchangeGatewayHttp {

    private TestService testService;

    private RestGatewayApplication gateway;

    @BeforeEach
    public void before() {
        gateway = new RestGatewayApplication();
        gateway.start();
        testService = new TestService();
    }

    @AfterEach
    public void after(){
        gateway.stop();
    }


    @Test
    public void contextStarts() {

    }

    @Test
    public void shouldCreateUser() throws Exception {

        testService.createAccount(123);

    }

    @Test
    public void shouldAddNewAsset() throws Exception {
        testService.addAsset(new RestApiAsset("XBT", 123, 8));
    }

}