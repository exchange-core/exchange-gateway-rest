package org.openpredict.exchange.rest.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RestApiAccountState {

    //    public final BigDecimal balanceHold;
//    public final BigDecimal balanceAvailable;
    public final String currency;
    public final BigDecimal balance;


    @JsonCreator
    @Builder
    public RestApiAccountState(
            @JsonProperty("currency") String currency,
            @JsonProperty("balance") BigDecimal balance) {

        this.balance = balance;
        this.currency = currency;
    }
}
