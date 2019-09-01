package org.openpredict.exchange.rest.model.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class RestApiDeal {

    private final String price;
    private final String size;
    private final DealParty party;


// TODO add more fields

    @JsonCreator
    @Builder
    public RestApiDeal(
            @JsonProperty("price") String price,
            @JsonProperty("size") String size,
            @JsonProperty("party") DealParty party) {

        this.price = price;
        this.size = size;
        this.party = party;
    }
}
