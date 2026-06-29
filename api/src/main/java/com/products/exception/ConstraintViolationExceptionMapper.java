package com.products.exception;

import com.products.adapters.in.rest.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log =
            Logger.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        String details = exception.getConstraintViolations()
                .stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    // Strip the method-name prefix (e.g. "insert.request.sku" -> "sku")
                    int lastDot = path.lastIndexOf('.');
                    String field = lastDot >= 0 ? path.substring(lastDot + 1) : path;
                    return field + ": " + v.getMessage();
                })
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: " + details);

        return ApiResponse.badRequest("Validation error: " + details);
    }
}
