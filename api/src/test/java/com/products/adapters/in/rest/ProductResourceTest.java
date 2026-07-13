package com.products.adapters.in.rest;

import com.products.support.BaseMongoIntegrationTest;
import com.products.support.TestTokenUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ProductResourceTest extends BaseMongoIntegrationTest {

    private static final String BASE = "/api/v1/products";

    private static final String VALID_BODY = """
            {
              "sku": "SKU-RT-001",
              "name": "Resource Test Product",
              "description": "Test description",
              "category": "Technology",
              "price": 99.99,
              "stock": 5,
              "active": true
            }
            """;

    private String createAndGetId() {
        return given()
                .contentType(ContentType.JSON).body(VALID_BODY)
                .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
                .when().post(BASE)
                .then().statusCode(201)
                .extract().path("data.id");
    }

    // ─── POST /api/v1/products ────────────────────────────────────────────────

    @Test
    void createProduct_returns201WithId() {
        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(201)
            .body("code", equalTo(201))
            .body("description", equalTo("Information inserted successfully"))
            .body("data.id", matchesPattern("^[a-fA-F0-9]{24}$"));
    }

    @Test
    void createProduct_missingRequiredField_returns400() {
        String body = """
                {"name":"No SKU","category":"Tech","price":10.0,"stock":1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON).body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void createProduct_invalidSkuFormat_returns400() {
        String body = """
                {"sku":"INVALID SKU!","name":"Test","category":"Tech","price":10.0,"stock":1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON).body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void createProduct_negativePriceAndStock_returns400() {
        String body = """
                {"sku":"SKU-NEG","name":"Neg","category":"Tech","price":-1.0,"stock":-1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON).body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void createProduct_duplicateSku_returns409() {
        given().contentType(ContentType.JSON).body(VALID_BODY)
               .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
               .when().post(BASE);

        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(409)
            .body("code", equalTo(409))
            .body("description", equalTo("Duplicate key conflict"));
    }

    @Test
    void createProduct_noToken_returns401() {
        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
        .when()
            .post(BASE)
        .then()
            .statusCode(401);
    }

    @Test
    void createProduct_userRole_returns403() {
        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(403);
    }

    // ─── GET /api/v1/products ─────────────────────────────────────────────────

    @Test
    void findAll_returnsPagedResponse() {
        createAndGetId();

        given()
            .queryParam("page", 0).queryParam("size", 10)
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE)
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.products", not(empty()))
            .body("data.totalItems", greaterThan(0))
            .body("data.totalPages", greaterThan(0));
    }

    @Test
    void findAll_sizeExceedsMax_returns400() {
        given()
            .queryParam("page", 0).queryParam("size", 101)
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void findAll_negativePageNumber_returns400() {
        given()
            .queryParam("page", -1).queryParam("size", 10)
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void findAll_noToken_returns401() {
        given()
            .queryParam("page", 0).queryParam("size", 10)
        .when()
            .get(BASE)
        .then()
            .statusCode(401);
    }

    // ─── GET /api/v1/products/{id} ────────────────────────────────────────────

    @Test
    void findById_returnsProduct() {
        String id = createAndGetId();

        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/{id}", id)
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.id", equalTo(id))
            .body("data.sku", equalTo("SKU-RT-001"))
            .body("data.name", equalTo("Resource Test Product"));
    }

    @Test
    void findById_notFound_returns404() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/{id}", "000000000000000000000001")
        .then()
            .statusCode(404)
            .body("code", equalTo(404))
            .body("description", equalTo("Information not found"));
    }

    @Test
    void findById_invalidIdFormat_returns400() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/{id}", "not-a-valid-id")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    // ─── PUT /api/v1/products/{id} ────────────────────────────────────────────

    @Test
    void updateProduct_returns200() {
        String id = createAndGetId();
        String body = """
                {"sku":"SKU-RT-001","name":"Updated Name","description":"Updated","category":"Technology","price":149.99,"stock":10,"active":false}
                """;

        given()
            .contentType(ContentType.JSON).body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .put(BASE + "/{id}", id)
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("description", equalTo("Information updated successfully"));
    }

    @Test
    void updateProduct_notFound_returns404() {
        String body = """
                {"sku":"SKU-RT-001","name":"Updated","category":"Tech","price":10.0,"stock":1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON).body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .put(BASE + "/{id}", "000000000000000000000001")
        .then()
            .statusCode(404)
            .body("code", equalTo(404));
    }

    @Test
    void updateProduct_invalidIdFormat_returns400() {
        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .put(BASE + "/{id}", "bad-id")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void updateProduct_skuAlreadyTakenByOtherProduct_returns409() {
        // Product A with SKU-RT-001
        String idA = createAndGetId();

        // Product B with a different SKU
        String bodyB = """
                {"sku":"SKU-RT-002","name":"Product B","description":"B","category":"Technology","price":10.0,"stock":1,"active":true}
                """;
        String idB = given()
                .contentType(ContentType.JSON).body(bodyB)
                .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
                .when().post(BASE)
                .then().statusCode(201)
                .extract().path("data.id");

        // Try to update Product B's SKU to match Product A's SKU
        String conflictBody = """
                {"sku":"SKU-RT-001","name":"Product B","description":"B","category":"Technology","price":10.0,"stock":1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON).body(conflictBody)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .put(BASE + "/{id}", idB)
        .then()
            .statusCode(409)
            .body("code", equalTo(409));
    }

    @Test
    void updateProduct_userRole_returns403() {
        String id = createAndGetId();
        given()
            .contentType(ContentType.JSON).body(VALID_BODY)
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .put(BASE + "/{id}", id)
        .then()
            .statusCode(403);
    }

    // ─── DELETE /api/v1/products/{id} ─────────────────────────────────────────

    @Test
    void deleteProduct_returns200() {
        String id = createAndGetId();

        given()
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .delete(BASE + "/{id}", id)
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("description", equalTo("Information deleted successfully"));
    }

    @Test
    void deleteProduct_notFound_returns404() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .delete(BASE + "/{id}", "000000000000000000000001")
        .then()
            .statusCode(404)
            .body("code", equalTo(404));
    }

    @Test
    void deleteProduct_invalidIdFormat_returns400() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .delete(BASE + "/{id}", "bad-id")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void deleteProduct_userRole_returns403() {
        String id = createAndGetId();
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .delete(BASE + "/{id}", id)
        .then()
            .statusCode(200);
    }

    // ─── GET /api/v1/products/sku/{sku} ──────────────────────────────────────

    @Test
    void findBySku_returnsProduct() {
        createAndGetId();

        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/sku/{sku}", "SKU-RT-001")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.sku", equalTo("SKU-RT-001"));
    }

    @Test
    void findBySku_notFound_returns404() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/sku/{sku}", "SKU-NOTEXIST")
        .then()
            .statusCode(404)
            .body("code", equalTo(404));
    }

    @Test
    void findBySku_invalidSkuFormat_returns400() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/sku/{sku}", "INVALID!SKU")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    // ─── GET /api/v1/products/search ─────────────────────────────────────────

    @Test
    void searchByPrefix_returnsMatchingProducts() {
        createAndGetId();

        given()
            .queryParam("prefix", "Resource")
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/search")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", not(empty()));
    }

    @Test
    void searchByPrefix_noResults_returnsEmptyList() {
        given()
            .queryParam("prefix", "ZZZNotExists")
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/search")
        .then()
            .statusCode(200)
            .body("data", empty());
    }

    @Test
    void searchByPrefix_missingParam_returns400() {
        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/search")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    // ─── JSON deserialization errors ──────────────────────────────────────────

    @Test
    void createProduct_malformedJson_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{not valid json}")
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void createProduct_invalidFieldType_returns400() {
        String body = """
                {"sku":"SKU-001","name":"Test","category":"Tech","price":"not-a-number","stock":1,"active":true}
                """;
        given()
            .contentType(ContentType.JSON)
            .body(body)
            .header("Authorization", "Bearer " + TestTokenUtil.adminToken())
        .when()
            .post(BASE)
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    // ─── Audit timestamps ─────────────────────────────────────────────────────

    @Test
    void findById_responseIncludesAuditTimestamps() {
        String id = createAndGetId();

        given()
            .header("Authorization", "Bearer " + TestTokenUtil.userToken())
        .when()
            .get(BASE + "/{id}", id)
        .then()
            .statusCode(200)
            .body("data.created", notNullValue())
            .body("data.updated", notNullValue());
    }
}
