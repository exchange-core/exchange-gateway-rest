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
package exchange.core2.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class RestApiAccountBalanceAdjustment {

    //public final long uid;
    public final long transactionId;
    public final BigDecimal amount;
    public final String currency;

    @JsonCreator
    public RestApiAccountBalanceAdjustment(
            //@JsonProperty("uid") long uid,
            @JsonProperty("transactionId") long transactionId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency) {

        //this.uid = uid;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "[BALANCE_ADJ " + amount + " " + currency + " transactionId:" + transactionId + "]";
    }
}
