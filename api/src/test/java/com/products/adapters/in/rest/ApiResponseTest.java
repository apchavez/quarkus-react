package com.products.adapters.in.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse's factory methods are branch-free (straight-line construction), so this is
 * purely closing a line-coverage gap: created(), health(), unprocessable() and
 * internalError() were never invoked by any existing test.
 */
class ApiResponseTest {

    @Test
    void created_returns201WithNoData() {
        Response response = ApiResponse.created();

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(((ApiResponse) response.getEntity()).data()).isNull();
    }

    @Test
    void health_returns200WithData() {
        Response response = ApiResponse.health("UP");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(((ApiResponse) response.getEntity()).data()).isEqualTo("UP");
    }

    @Test
    void unprocessable_returns422WithDescription() {
        Response response = ApiResponse.unprocessable("cannot process");

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(((ApiResponse) response.getEntity()).description()).isEqualTo("cannot process");
    }

    @Test
    void internalError_returns500() {
        Response response = ApiResponse.internalError();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(((ApiResponse) response.getEntity()).code()).isEqualTo(500);
    }
}
