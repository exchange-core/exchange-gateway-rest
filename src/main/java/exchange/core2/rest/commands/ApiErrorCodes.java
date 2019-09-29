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
package exchange.core2.rest.commands;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApiErrorCodes {

    SYMBOL_ALREADY_EXISTS(1000, 400, "symbol already exists"),
    UNKNOWN_BASE_ASSET(1001, 400, "unknown base asset"),
    UNKNOWN_QUOTE_CURRENCY(1002, 400, "unknown quote currency"),
    ASSET_ALREADY_EXISTS(1003, 400, "asset already exists"),
    UNKNOWN_CURRENCY(1004, 400, "unknown currency"),
    PRECISION_IS_TOO_HIGH(1005, 400, "precision is too high, reduce precision"),
    UNKNOWN_SYMBOL(1006, 400, "unknown symbol"),
    UNKNOWN_SYMBOL_404(1007, 404, "symbol not found"),

    INVALID_CONFIGURATION(1008, 400, "invalid configuration: %s"),

    INVALID_PRICE(1009, 400, "invalid price"),
    UNKNOWN_USER_404(1010, 404, "unknown user"),
    ;

    public final int gatewayErrorCode;
    public final int httpStatus;
    public final String errorDescription;
}
