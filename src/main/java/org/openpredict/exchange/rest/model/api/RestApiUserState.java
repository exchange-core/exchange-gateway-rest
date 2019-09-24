package org.openpredict.exchange.rest.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public final class RestApiUserState {

    public final long uid;
    public final List<RestApiOrder> activeOrders;
    public final List<RestApiAccountState> accounts;

    @JsonCreator
    @Builder
    public RestApiUserState(
            @JsonProperty("uid") long uid,
            @JsonProperty("activeOrders") List<RestApiOrder> activeOrders,
            @JsonProperty("accounts") List<RestApiAccountState> accounts) {

        this.uid = uid;
        this.activeOrders = activeOrders;
        this.accounts = accounts;
    }

}
