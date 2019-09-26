package org.openpredict.exchange.rest.model.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderState {

    NEW,
//    PENDING,
    ACTIVE, // new or partiallyFiled
    FILLED, // filled
    CANCELLED, // can be partially filled before cancelled
    REJECTED // can be partially filled before rejected

}
