/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.rest.events;


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
