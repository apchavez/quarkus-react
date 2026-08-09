package com.products.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.products.adapters.in.rest.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class JsonProcessingExceptionMapper {

    private static final Logger log = Logger.getLogger(JsonProcessingExceptionMapper.class);

    @ServerExceptionMapper(priority = 1)
    public Response handleJsonParseException(JsonParseException exception) {
        log.warn("Malformed JSON received: " + exception.getOriginalMessage());
        return ApiResponse.badRequest("JSON Processing Error - Malformed JSON syntax");
    }

    @ServerExceptionMapper(priority = 1)
    public Response handleInvalidFormatException(InvalidFormatException exception) {
        String path = buildPath(exception);
        log.warn("Invalid JSON format for field: " + path);
        return ApiResponse.badRequest("JSON Processing Error - Invalid format for field '" + path + "'");
    }

    @ServerExceptionMapper(priority = 1)
    public Response handleMismatchedInputException(MismatchedInputException exception) {
        String path = buildPath(exception);
        log.warn("Mismatched JSON input for field: " + path);
        return ApiResponse.badRequest("JSON Processing Error - Mismatched input for field '" + path + "'");
    }

    @ServerExceptionMapper(priority = 1)
    public Response handleJsonMappingException(JsonMappingException exception) {
        String path = buildPath(exception);
        log.warn("JSON mapping error for field: " + path);
        return ApiResponse.badRequest("JSON Processing Error - Mapping error for field '" + path + "'");
    }

    @ServerExceptionMapper(priority = 1)
    public Response handleJsonProcessingException(JsonProcessingException exception) {
        log.warn("JSON processing error: " + exception.getOriginalMessage());
        return ApiResponse.badRequest("JSON Processing Error - " + exception.getOriginalMessage());
    }

    private String buildPath(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .reduce((a, b) -> a + "." + b)
                .orElse("unknown");
    }
}