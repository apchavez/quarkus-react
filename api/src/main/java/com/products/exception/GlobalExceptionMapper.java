package com.products.exception;

import com.products.adapters.in.rest.ApiResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof WebApplicationException webException) {
            Response response = webException.getResponse();

            log.warn("HTTP exception intercepted: status=" + response.getStatus() + ", message=" + exception.getMessage());

            if (!response.hasEntity()) {
                String message = exception.getMessage() != null
                        ? exception.getMessage()
                        : "HTTP " + response.getStatus() + " Error";

                return ApiResponse.error(response.getStatus(), response.getStatus(), message);
            }

            return response;
        }

        log.error("Unhandled exception: " + exception.getMessage(), exception);
        return ApiResponse.internalError();
    }
}