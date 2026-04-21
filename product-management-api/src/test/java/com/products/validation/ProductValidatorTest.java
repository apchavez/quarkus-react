package com.products.validation;

import com.products.application.dto.ProductRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProductValidatorTest {

    @Inject
    ProductValidator validator;

    @Test
    void validateInsert_shouldPass_whenRequestIsValid() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.description()).thenReturn("High performance laptop");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isEmpty();
    }

    @Test
    void validateInsert_shouldFail_whenRequestIsNull() {
        Optional<Response> result = validator.validateInsert(null);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenSkuIsBlank() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn(" ");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenSkuFormatIsInvalid() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU 001!");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenNameIsBlank() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn(" ");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenCategoryIsBlank() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn(" ");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenPriceIsNull() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(null);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenPriceIsNegative() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(-1.0);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenStockIsNull() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(null);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenStockIsNegative() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(-1);
        when(request.active()).thenReturn(true);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateInsert_shouldFail_whenActiveIsNull() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(null);

        Optional<Response> result = validator.validateInsert(request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateUpdate_shouldPass_whenIdAndRequestAreValid() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.sku()).thenReturn("SKU-001");
        when(request.name()).thenReturn("Laptop Pro");
        when(request.description()).thenReturn("High performance laptop");
        when(request.category()).thenReturn("Technology");
        when(request.price()).thenReturn(14999.99);
        when(request.stock()).thenReturn(10);
        when(request.active()).thenReturn(true);

        String validId = new ObjectId().toHexString();

        Optional<Response> result = validator.validateUpdate(validId, request);

        assertThat(result).isEmpty();
    }

    @Test
    void validateUpdate_shouldFail_whenIdIsInvalid() {
        ProductRequest request = mock(ProductRequest.class);

        Optional<Response> result = validator.validateUpdate("invalid-id", request);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateDelete_shouldPass_whenIdIsValid() {
        String validId = new ObjectId().toHexString();

        Optional<Response> result = validator.validateDelete(validId);

        assertThat(result).isEmpty();
    }

    @Test
    void validateDelete_shouldFail_whenIdIsInvalid() {
        Optional<Response> result = validator.validateDelete("bad-id");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateFind_shouldPass_whenIdIsValid() {
        String validId = new ObjectId().toHexString();

        Optional<Response> result = validator.validateFind(validId);

        assertThat(result).isEmpty();
    }

    @Test
    void validateFind_shouldFail_whenIdIsBlank() {
        Optional<Response> result = validator.validateFind(" ");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateFindBySku_shouldPass_whenSkuIsValid() {
        Optional<Response> result = validator.validateFindBySku("SKU-001");

        assertThat(result).isEmpty();
    }

    @Test
    void validateFindBySku_shouldFail_whenSkuIsInvalid() {
        Optional<Response> result = validator.validateFindBySku("SKU 001!");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateFindByNamePrefix_shouldPass_whenPrefixIsValid() {
        Optional<Response> result = validator.validateFindByNamePrefix("Lap");

        assertThat(result).isEmpty();
    }

    @Test
    void validateFindByNamePrefix_shouldFail_whenPrefixIsBlank() {
        Optional<Response> result = validator.validateFindByNamePrefix(" ");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateFindAll_shouldPass_whenPageAndSizeAreValid() {
        Optional<Response> result = validator.validateFindAll(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void validateFindAll_shouldFail_whenPageIsNegative() {
        Optional<Response> result = validator.validateFindAll(-1, 10);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }

    @Test
    void validateFindAll_shouldFail_whenSizeIsZero() {
        Optional<Response> result = validator.validateFindAll(0, 0);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(400);
    }
}