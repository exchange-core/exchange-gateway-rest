package org.openpredict.exchange.rest.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class RestGenericResponse<T> {

    private final long ticket;
    private final int gatewayResultCode;
    private final int coreResultCode;
    private final String description;
    private final T data;

    public RestGenericResponse(
            @JsonProperty("ticket") long ticket,
            @JsonProperty("gatewayResultCode") int gatewayResultCode,
            @JsonProperty("coreResultCode") int coreResultCode,
            @JsonProperty("description") String description,
            @JsonProperty("data") T data) {

        this.ticket = ticket;
        this.gatewayResultCode = gatewayResultCode;
        this.coreResultCode = coreResultCode;
        this.description = description;
        this.data = data;
    }

    @Override
    public String toString() {
        return "[RESPONSE T:" + ticket + " RES:" + gatewayResultCode + " " + coreResultCode + " " + description + "]";
    }
}
