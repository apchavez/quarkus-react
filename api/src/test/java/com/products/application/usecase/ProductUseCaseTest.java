package com.products.application.usecase;

import com.products.application.dto.ImportResult;
import com.products.application.dto.ImportRowError;
import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.dto.ProductsPagedResponse;
import com.products.application.mapper.ProductMapper;
import com.products.application.port.out.ProductEventPublisherPort;
import com.products.application.port.out.ProductRepositoryPort;
import com.products.domain.event.ProductEvent;
import com.products.domain.event.ProductEventType;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import com.products.exception.DuplicateSkuException;
import com.products.exception.ProductNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProductUseCaseTest {

    @InjectMock
    ProductRepositoryPort productRepository;

    @InjectMock
    ProductMapper productMapper;

    @InjectMock
    ProductEventPublisherPort eventPublisher;

    @Inject
    ProductUseCase productUseCase;

    @Test
    void insert_shouldReturnProductResponse_whenProductIsInserted() {
        ProductRequest request = mock(ProductRequest.class);
        Product product = new Product();
        product.id = new ObjectId();
        ProductResponse expectedResponse = mock(ProductResponse.class);

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.insert(product)).thenReturn(true);
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        assertThat(productUseCase.insert(request)).isEqualTo(expectedResponse);

        ArgumentCaptor<ProductEvent> captor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(ProductEventType.PRODUCT_CREATED);
        assertThat(captor.getValue().product()).isEqualTo(product);
    }

    @Test
    void insert_shouldThrowDuplicateSkuException_whenProductAlreadyExists() {
        ProductRequest request = mock(ProductRequest.class);
        Product product = new Product();
        product.id = new ObjectId();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.insert(product)).thenReturn(false);

        assertThatThrownBy(() -> productUseCase.insert(request))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void findById_shouldReturnProductResponse_whenProductExists() {
        ObjectId id = new ObjectId();
        Product product = new Product();
        ProductResponse productResponse = mock(ProductResponse.class);

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        assertThat(productUseCase.findById(id.toHexString())).isEqualTo(productResponse);
    }

    @Test
    void findById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(null);

        assertThatThrownBy(() -> productUseCase.findById(new ObjectId().toHexString()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findAllActive_shouldReturnPagedResponse() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);
        PagedResponse<Product> pagedResponse = new PagedResponse<>(List.of(product), 0, 10, 1, 1, true);

        when(productRepository.findAllActiveProducts(0, 10)).thenReturn(pagedResponse);
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        ProductsPagedResponse result = productUseCase.findAllActive(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void findAllInactive_shouldReturnPagedResponse() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);
        PagedResponse<Product> pagedResponse = new PagedResponse<>(List.of(product), 0, 10, 1, 1, true);

        when(productRepository.findAllInactiveProducts(0, 10)).thenReturn(pagedResponse);
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        ProductsPagedResponse result = productUseCase.findAllInactive(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void findBySku_shouldReturnProductResponse_whenProductExists() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);

        when(productRepository.findBySku("SKU-001")).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(responseDto);

        assertThat(productUseCase.findBySku("SKU-001")).isEqualTo(responseDto);
    }

    @Test
    void findBySku_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findBySku("SKU-404")).thenReturn(null);

        assertThatThrownBy(() -> productUseCase.findBySku("SKU-404"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findByNamePrefix_shouldReturnList_whenProductsExist() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);

        when(productRepository.findByNamePrefix("Lap")).thenReturn(List.of(product));
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        assertThat(productUseCase.findByNamePrefix("Lap")).hasSize(1);
    }

    @Test
    void findByNamePrefix_shouldReturnEmptyList_whenNoProductsExist() {
        when(productRepository.findByNamePrefix("XYZ")).thenReturn(List.of());
        when(productMapper.toResponseList(anyList())).thenReturn(List.of());

        assertThat(productUseCase.findByNamePrefix("XYZ")).isEmpty();
    }

    @Test
    void update_shouldComplete_whenProductExists() {
        ObjectId id = new ObjectId();
        ProductRequest request = mock(ProductRequest.class);
        Product existing = new Product();

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(existing);
        doNothing().when(productMapper).updateEntity(request, existing);
        when(productRepository.replace(existing)).thenReturn(true);

        productUseCase.update(id.toHexString(), request);

        ArgumentCaptor<ProductEvent> captor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(ProductEventType.PRODUCT_UPDATED);
        assertThat(captor.getValue().product()).isEqualTo(existing);
    }

    @Test
    void update_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(null);

        assertThatThrownBy(() -> productUseCase.update(new ObjectId().toHexString(), mock(ProductRequest.class)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void update_shouldThrowDuplicateSkuException_whenSkuConflictsWithAnotherProduct() {
        ObjectId id = new ObjectId();
        ProductRequest request = mock(ProductRequest.class);
        Product existing = new Product();

        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(existing);
        doNothing().when(productMapper).updateEntity(request, existing);
        when(productRepository.replace(existing)).thenReturn(false);

        assertThatThrownBy(() -> productUseCase.update(id.toHexString(), request))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void delete_shouldComplete_whenProductExists() {
        Product existing = new Product();
        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(existing);
        when(productRepository.deleteByObjectId(any(ObjectId.class))).thenReturn(true);

        productUseCase.delete(new ObjectId().toHexString());

        ArgumentCaptor<ProductEvent> captor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(ProductEventType.PRODUCT_DELETED);
        assertThat(captor.getValue().product()).isEqualTo(existing);
    }

    @Test
    void delete_shouldThrowProductNotFoundException_whenProductNotFoundBeforeDeleting() {
        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(null);

        assertThatThrownBy(() -> productUseCase.delete(new ObjectId().toHexString()))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void delete_shouldThrowProductNotFoundException_whenDeleteRaces() {
        // Product existed at lookup time but was gone by delete time (e.g. concurrent delete) —
        // deleteByObjectId returning false in that race must still surface as 404, not publish an event.
        when(productRepository.findByObjectId(any(ObjectId.class))).thenReturn(new Product());
        when(productRepository.deleteByObjectId(any(ObjectId.class))).thenReturn(false);

        assertThatThrownBy(() -> productUseCase.delete(new ObjectId().toHexString()))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(eventPublisher);
    }

    // ─── importProducts ───────────────────────────────────────────────────────

    private InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importProducts_validCsv_importsAllRows() {
        String csv = """
                sku,name,description,category,price,stock,active
                SKU-IMP-1,Product One,Desc one,Tech,10.50,5,true
                SKU-IMP-2,Product Two,Desc two,Tech,20.00,3,true
                """;

        when(productMapper.toEntity(any(ProductRequest.class))).thenAnswer(inv -> {
            Product p = new Product();
            p.id = new ObjectId();
            return p;
        });
        when(productRepository.insert(any(Product.class))).thenReturn(true);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mock(ProductResponse.class));

        ImportResult result = productUseCase.importProducts(csv(csv));

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void importProducts_invalidRows_reportsErrorsAndContinuesProcessing() {
        String csv = """
                sku,name,description,category,price,stock,active
                SKU-IMP-OK,Valid Product,Desc,Tech,10.50,5,true
                ,Missing Sku,Desc,Tech,10.00,1,true
                SKU-IMP-NEG,Negative Price,Desc,Tech,-5.00,1,true
                """;

        when(productMapper.toEntity(any(ProductRequest.class))).thenAnswer(inv -> {
            Product p = new Product();
            p.id = new ObjectId();
            return p;
        });
        when(productRepository.insert(any(Product.class))).thenReturn(true);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mock(ProductResponse.class));

        ImportResult result = productUseCase.importProducts(csv(csv));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(2);
        assertThat(result.errors()).extracting(ImportRowError::row).containsExactly(3, 4);
    }

    @Test
    void importProducts_malformedNumericField_reportsRowAsFailed() {
        String csv = """
                sku,name,description,category,price,stock,active
                SKU-IMP-BAD,Bad Price,Desc,Tech,not-a-number,5,true
                """;

        ImportResult result = productUseCase.importProducts(csv(csv));

        assertThat(result.imported()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        verifyNoInteractions(productRepository);
    }

    @Test
    void importProducts_duplicateSku_reportsAsFailed() {
        String csv = """
                sku,name,description,category,price,stock,active
                SKU-IMP-DUP,Product,Desc,Tech,10.00,1,true
                """;

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(new Product());
        when(productRepository.insert(any(Product.class))).thenReturn(false);

        ImportResult result = productUseCase.importProducts(csv(csv));

        assertThat(result.imported()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.errors().get(0).message()).contains("already exists");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void importProducts_emptyFile_returnsZeroCounts() {
        String csv = "sku,name,description,category,price,stock,active\n";

        ImportResult result = productUseCase.importProducts(csv(csv));

        assertThat(result.imported()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.errors()).isEmpty();
    }

    // ─── findAllForReport ─────────────────────────────────────────────────────

    @Test
    void findAllForReport_returnsAllMappedProducts() {
        Product product = new Product();
        ProductResponse responseDto = mock(ProductResponse.class);

        when(productRepository.findAllProducts()).thenReturn(List.of(product));
        when(productMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        List<ProductResponse> result = productUseCase.findAllForReport();

        assertThat(result).hasSize(1);
    }
}
