package com.products.adapters.in.rest;

import com.products.support.MongoDbTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(MongoDbTestResource.class)
class AuthResourceTest {

    private static final String BASE = "/api/v1/auth/login";

    @Test
    void login_withValidAdminCredentials_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {"username":"admin","password":"admin123"}
                    """)
        .when()
            .post(BASE)
        .then()
            .statusCode(200)
            .body("data.token", notNullValue())
            .body("data.tokenType", equalTo("Bearer"))
            .body("data.username", equalTo("admin"))
            .body("data.roles", hasItem("ADMIN"));
    }

    @Test
    void login_withValidUserCredentials_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {"username":"user","password":"user123"}
                    """)
        .when()
            .post(BASE)
        .then()
            .statusCode(200)
            .body("data.token", notNullValue())
            .body("data.username", equalTo("user"))
            .body("data.roles", hasItem("USER"));
    }

    @Test
    void login_withWrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {"username":"admin","password":"wrong-password"}
                    """)
        .when()
            .post(BASE)
        .then()
            .statusCode(401)
            .body("code", equalTo(401));
    }

    @Test
    void login_withUnknownUser_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {"username":"nobody","password":"whatever"}
                    """)
        .when()
            .post(BASE)
        .then()
            .statusCode(401)
            .body("code", equalTo(401));
    }

    @Test
    void login_withMissingFields_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {"username":"admin"}
                    """)
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }
}
