package org.openpredict.exchange.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class RestApiAccountBalanceAdjustment {

    public final long uid;
    public final long transactionId;
    public final BigDecimal amount;
    public final String currency;

    @JsonCreator
    public RestApiAccountBalanceAdjustment(
            @JsonProperty("uid") long uid,
            @JsonProperty("transactionId") long transactionId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency) {

        this.uid = uid;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "[BALANCE_ADJ " + uid + " for " + amount + " " + currency + " transactionId:" + transactionId + "]";
    }
}
