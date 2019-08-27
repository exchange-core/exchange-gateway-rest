package org.openpredict.exchange.rest.commands;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApiErrorCodes {

    SYMBOL_ALREADY_EXISTS(1000, 400, "symbol already exists"),
    UNKNOWN_BASE_ASSET(1001, 400, "unknown base asset"),
    UNKNOWN_QUOTE_CURRENCY(1002, 400, "unknown quote currency"),
    ASSET_ALREADY_EXISTS(1003, 400, "asset already exists"),
    UNKNOWN_CURRENCY(1004, 400, "unknown currency"),
    PRECISION_IS_TOO_HIGH(1005, 400, "precision is too high, reduce precision");

    public final int gatewayErrorCode;
    public final int httpStatus;
    public final String errorDescription;
}
