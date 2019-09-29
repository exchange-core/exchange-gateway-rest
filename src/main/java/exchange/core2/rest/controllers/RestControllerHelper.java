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
package exchange.core2.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.cmd.OrderCommand;
import exchange.core2.rest.commands.ApiErrorCodes;
import exchange.core2.rest.events.RestGenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

@Slf4j
public class RestControllerHelper {

    public static ResponseEntity<RestGenericResponse> errorResponse(ApiErrorCodes errMessage, String... args) {

        RestGenericResponse<Object> response = RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(errMessage.gatewayErrorCode)
                .coreResultCode(0)
                .description(String.format(errMessage.errorDescription, (Object[]) args))
                .build();

        log.info("return error: " + response);

        return ResponseEntity.status(errMessage.httpStatus).body(response);
    }

    public static ResponseEntity<RestGenericResponse> coreResponse(OrderCommand cmd, Supplier<Object> successMapper, HttpStatus successCode) {
        CommandResultCode resultCode = cmd.resultCode;
        return ResponseEntity
                .status(resultCode == CommandResultCode.SUCCESS ? successCode : HttpStatus.BAD_REQUEST)
                .body(RestGenericResponse.builder()
                        .ticket(0)
                        .gatewayResultCode(0)
                        .coreResultCode(resultCode.getCode())
                        .data(successMapper.get())
                        .description(resultCode.toString())
                        .build());
    }

    public static ResponseEntity<RestGenericResponse> successResponse(Object data, HttpStatus code) {
        return ResponseEntity
                .status(code)
                .body(RestGenericResponse.builder()
                        .ticket(0)
                        .gatewayResultCode(0)
                        .coreResultCode(0)
                        .data(data)
                        //.description(null)
                        .build());

    }

}
