package com.products.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class HealthCheckResourceTest {

    @Test
    void livenessCheck_returnsUp() {
        given()
            .when().get("/api/v1/q/health/live")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void readinessCheck_returnsUp() {
        given()
            .when().get("/api/v1/q/health/ready")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }
}
