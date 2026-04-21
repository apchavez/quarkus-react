package com.products.exception;

import com.products.domain.model.ApiResponse;
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
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: " + details);

        return ApiResponse.unprocessable(
                "Validation error - " + details
        );
    }
}