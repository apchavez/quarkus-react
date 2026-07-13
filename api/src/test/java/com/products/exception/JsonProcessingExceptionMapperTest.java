package com.products.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each handler here is a plain instance method, so they can be exercised directly
 * without a REST round-trip. Doing so also makes it possible to reach buildPath()'s
 * per-reference branch (a field-name reference vs. an index-only/array reference),
 * which the REST-level tests never hit because Product's payload is flat (no nested
 * objects/arrays), so Jackson never reports a JsonMappingException with more than an
 * empty path there.
 */
class JsonProcessingExceptionMapperTest {

    private final JsonProcessingExceptionMapper mapper = new JsonProcessingExceptionMapper();

    @Test
    void handleJsonParseException_returnsBadRequest() {
        Response response = mapper.handleJsonParseException(new JsonParseException("Unexpected character"));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void handleInvalidFormatException_returnsBadRequest_andBuildsPathFromNamedAndIndexedReferences() {
        InvalidFormatException exception = new InvalidFormatException(null, "not-a-number", Double.class);
        // One reference identified by field name, one identified only by array index -
        // exercises both branches of buildPath()'s per-reference ternary.
        exception.prependPath(new JsonMappingException.Reference(this, 2));
        exception.prependPath(new JsonMappingException.Reference(this, "price"));

        Response response = mapper.handleInvalidFormatException(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity().toString()).contains("price").contains("[2]");
    }

    @Test
    void handleMismatchedInputException_returnsBadRequest() {
        // InvalidFormatException IS-A MismatchedInputException; reused here to avoid
        // constructing one from a real JsonParser just to reach this handler.
        InvalidFormatException exception = new InvalidFormatException(null, "not-a-number", Double.class);
        exception.prependPath(new JsonMappingException.Reference(this, "stock"));

        Response response = mapper.handleMismatchedInputException(exception);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void handleJsonMappingException_returnsBadRequest_whenPathIsEmpty() {
        InvalidFormatException exception = new InvalidFormatException(null, "not-a-number", Double.class);

        Response response = mapper.handleJsonMappingException(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity().toString()).contains("unknown");
    }

    @Test
    void handleJsonProcessingException_returnsBadRequest() {
        InvalidFormatException exception = new InvalidFormatException(null, "not-a-number", Double.class);

        Response response = mapper.handleJsonProcessingException(exception);

        assertThat(response.getStatus()).isEqualTo(400);
    }
}
