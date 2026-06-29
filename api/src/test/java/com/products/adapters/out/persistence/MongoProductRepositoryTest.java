package com.products.adapters.out.persistence;

import com.products.domain.model.Product;
import com.products.support.BaseMongoIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MongoProductRepositoryTest extends BaseMongoIntegrationTest {

    @Inject
    MongoProductRepository repository;

    private Product buildProduct(String sku, String name) {
        Product p = new Product();
        p.id = new ObjectId();
        p.sku = sku;
        p.name = name;
        p.description = "Integration test";
        p.category = "Technology";
        p.price = 100.0;
        p.stock = 10;
        p.active = true;
        p.create("TEST");
        return p;
    }

    @Test
    void testInsert_Success() {
        Product product = buildProduct("SKU-IT-001", "Laptop Test");

        boolean inserted = repository.insert(product);

        assertThat(inserted).isTrue();
        assertThat(repository.findBySku("SKU-IT-001")).isNotNull()
                .extracting(p -> p.name).isEqualTo("Laptop Test");
    }

    @Test
    void testInsert_DuplicateSku_ReturnsFalse() {
        Product p1 = buildProduct("SKU-DUP-001", "Product 1");
        Product p2 = buildProduct("SKU-DUP-001", "Product 2");

        assertThat(repository.insert(p1)).isTrue();
        assertThat(repository.insert(p2)).isFalse();
    }

    @Test
    void testFindByObjectId_Success() {
        Product product = buildProduct("SKU-FIND-001", "Find Test");
        repository.insert(product);

        Product found = repository.findByObjectId(product.id);

        assertThat(found).isNotNull();
        assertThat(found.sku).isEqualTo("SKU-FIND-001");
    }

    @Test
    void testDeleteByObjectId_Success() {
        Product product = buildProduct("SKU-DEL-001", "Delete Test");
        repository.insert(product);

        assertThat(repository.deleteByObjectId(product.id)).isTrue();
        assertThat(repository.findByObjectId(product.id)).isNull();
    }

    @Test
    void testFindAllProducts_Success() {
        repository.insert(buildProduct("SKU-PAGE-001", "Paged Test"));

        var paged = repository.findAllProducts(0, 10);

        assertThat(paged).isNotNull();
        assertThat(paged.data()).isNotEmpty();
        assertThat(paged.totalItems()).isGreaterThan(0);
    }

    @Test
    void testFindByNamePrefix_ReturnsMatchingProducts() {
        repository.insert(buildProduct("SKU-PREFIX-001", "Laptop Pro Max"));

        var results = repository.findByNamePrefix("Laptop");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).name).startsWith("Laptop");
    }

    @Test
    void testFindByNamePrefix_NoMatch_ReturnsEmpty() {
        assertThat(repository.findByNamePrefix("ZZZNotExists")).isEmpty();
    }
}
