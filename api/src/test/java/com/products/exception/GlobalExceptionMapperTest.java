package com.products.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * toResponse() has three independent conditions (instanceof WebApplicationException,
 * response.hasEntity(), message != null). The REST-level tests only ever exercise a
 * WebApplicationException with no pre-built entity and a non-null message, so this adds
 * the three cases that fall through to the other side of each branch.
 */
class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    void toResponse_returnsInternalError_forNonWebApplicationException() {
        Response response = mapper.toResponse(new IllegalStateException("boom"));

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void toResponse_returnsResponseAsIs_whenWebApplicationExceptionAlreadyHasEntity() {
        Response entityResponse = Response.status(404).entity("already built").build();
        WebApplicationException exception = new WebApplicationException("not found", entityResponse);

        Response response = mapper.toResponse(exception);

        assertThat(response).isSameAs(entityResponse);
    }

    @Test
    void toResponse_fallsBackToStatusMessage_whenExceptionMessageIsNull() {
        WebApplicationException exception = new WebApplicationException((String) null, Response.status(503).build());

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getEntity().toString()).contains("HTTP 503 Error");
    }
}
