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
package exchange.core2.rest.model.api;

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
