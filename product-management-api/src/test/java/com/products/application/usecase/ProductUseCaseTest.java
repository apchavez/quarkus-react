package com.products.application.usecase;

import com.products.adapters.out.persistence.MongoProductRepository;
import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.mapper.ProductMapper;
import com.products.domain.model.ApiResponse;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProductUseCaseTest {

    @InjectMock
    MongoProductRepository productRepository;

    @InjectMock
    ProductMapper productMapper;

    @Inject
    ProductUseCase productUseCase;

    @Test
    void insert_shouldReturnCreated_whenProductIsInserted() {
        ProductRequest request = mock(ProductRequest.class);
        Product product = new Product();
        product.id = new ObjectId();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.insert(product)).thenReturn(true);

        Response response = productUseCase.insert(request);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getEntity()).isInstanceOf(ApiResponse.class);

        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertThat(apiResponse.data()).isNotNull();
    }

    @Test
    void insert_shouldReturnConflict_whenProductAlreadyExists() {
        ProductRequest request = mock(ProductRequest.class);
        Product product = new Product();
        product.id = new ObjectId();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.insert(product)).thenReturn(false);

        Response response = productUseCase.insert(request);

        assertThat(response.getStatus()).isEqualTo(409);
    }

    @Test
    void findById_shouldReturnOk_whenProductExists() {
        ObjectId id = new ObjectId();
        Product product = new Product();
        ProductResponse productResponse = mock(ProductResponse.class);

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        Response response = productUseCase.findById(id.toHexString());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void findById_shouldReturnBadRequest_whenIdIsInvalid() {
        Response response = productUseCase.findById("invalid-id");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void findAll_shouldReturnOk() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);
        PagedResponse<Product> pagedResponse = new PagedResponse<>(List.of(product), 0, 1, 1);

        when(productRepository.findAllProducts(0, 10)).thenReturn(pagedResponse);
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        Response response = productUseCase.findAll(0, 10);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void findBySku_shouldReturnOk_whenProductExists() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);

        when(productRepository.findBySku("SKU-001")).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(responseDto);

        Response response = productUseCase.findBySku("SKU-001");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void findBySku_shouldReturnNotFound_whenProductDoesNotExist() {
        when(productRepository.findBySku("SKU-404")).thenReturn(null);

        Response response = productUseCase.findBySku("SKU-404");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void findByNamePrefix_shouldReturnOk_whenProductsExist() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);

        when(productRepository.findByNamePrefix("Lap")).thenReturn(List.of(product));
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        Response response = productUseCase.findByNamePrefix("Lap");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void findByNamePrefix_shouldReturnNotFound_whenNoProductsExist() {
        when(productRepository.findByNamePrefix("XYZ")).thenReturn(List.of());

        Response response = productUseCase.findByNamePrefix("XYZ");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void update_shouldReturnUpdated_whenProductExists() {
        ObjectId id = new ObjectId();
        ProductRequest request = mock(ProductRequest.class);
        Product existing = new Product();
        Product updated = new Product();

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(existing);
        doNothing().when(productMapper).updateEntity(request, existing);
        when(productRepository.update(eq(id), any())).thenReturn(updated);

        Response response = productUseCase.update(id.toHexString(), request);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void update_shouldReturnNotFound_whenProductDoesNotExist() {
        ObjectId id = new ObjectId();
        ProductRequest request = mock(ProductRequest.class);

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(null);

        Response response = productUseCase.update(id.toHexString(), request);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void delete_shouldReturnDeleted_whenProductExists() {
        ObjectId id = new ObjectId();
        Product existing = new Product();

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(existing);
        when(productRepository.deleteByObjectId(id)).thenReturn(true);

        Response response = productUseCase.delete(id.toHexString());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void delete_shouldReturnNotFound_whenProductDoesNotExist() {
        ObjectId id = new ObjectId();

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(null);

        Response response = productUseCase.delete(id.toHexString());

        assertThat(response.getStatus()).isEqualTo(404);
    }
}