package org.openpredict.exchange.rest.controllers;

import org.openpredict.exchange.beans.cmd.CommandResultCode;
import org.openpredict.exchange.rest.commands.ApiErrorCodes;
import org.openpredict.exchange.rest.events.RestGenericResponse;
import org.rapidoid.http.Resp;

public class RestControllerHelper {


    public static Resp errorResponse(Resp resp, ApiErrorCodes errMessage, String... args) {
        String msg = String.format(errMessage.errorDescription, (Object[]) args);

        RestGenericResponse response = RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(errMessage.gatewayErrorCode)
                .coreResultCode(0)
                .description(msg)
                .build();

        resp.json(response);

        //resp.body(msg.getBytes());
        resp.code(errMessage.httpReturnCode);
        return resp;
    }

    public static Resp successResponse(Resp resp, Object data, int code, String description) {
        resp.code(code);
        resp.json(RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(0)
                .coreResultCode(0)
                .data(data)
                .description(description)
                .build());
        return resp;
    }

    public static void asyncCoreResponseCreated(Resp resp, Object data, CommandResultCode coreResult) {
        asyncCoreResponse(resp, data, coreResult, 201);
    }

    public static void asyncCoreResponseOk(Resp resp, Object data, CommandResultCode coreResult) {
        asyncCoreResponse(resp, data, coreResult, 200);
    }

    public static void asyncCoreResponse(Resp resp, Object data, CommandResultCode coreResult, int successCode) {

        resp.code(coreResult == CommandResultCode.SUCCESS ? successCode : 400);
        resp.json(RestGenericResponse.builder()
                .ticket(0)
                .gatewayResultCode(0)
                .coreResultCode(coreResult.getCode())
                .data(data)
                .description(coreResult.toString())
                .build());
        resp.done();
    }

}
