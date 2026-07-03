package com.products.adapters.in.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(Integer code, String description, Object data) {

    /* ---------- SUCCESS ---------- */

    public static Response ok(Object data) {
        return Response.ok(
                new ApiResponse(200, "Information obtained successfully", data))
                .build();
    }

    public static Response created() {
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse(201, "Information inserted successfully", null))
                .build();
    }

    public static Response created(Object data) {
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse(201, "Information inserted successfully", data))
                .build();
    }

    public static Response updated() {
        return Response.ok(
                new ApiResponse(200, "Information updated successfully", null))
                .build();
    }

    public static Response deleted() {
        return Response.ok(
                new ApiResponse(200, "Information deleted successfully", null))
                .build();
    }

    public static Response health(Object data) {
        return Response.ok(
                new ApiResponse(200, "Health check completed", data))
                .build();
    }

    /* ---------- CLIENT ERRORS ---------- */

    public static Response badRequest(String description) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse(400, description, null))
                .build();
    }

    public static Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ApiResponse(404, "Information not found", null))
                .build();
    }

    public static Response conflictKey() {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ApiResponse(409, "Duplicate key conflict", null))
                .build();
    }

    public static Response unprocessable(String description) {
        return Response.status(422)
                .entity(new ApiResponse(422, description, null))
                .build();
    }

    /* ---------- SERVER ERRORS ---------- */

    public static Response internalError() {
        return Response.serverError()
                .entity(new ApiResponse(500, "Internal server error", null))
                .build();
    }

    /* ---------- GENERIC ---------- */

    public static Response error(int httpStatus, int code, String description) {
        return Response.status(httpStatus)
                .entity(new ApiResponse(code, description, null))
                .build();
    }
}
