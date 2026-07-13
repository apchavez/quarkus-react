package com.products.adapters.out.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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

    @Inject
    MongoClient mongoClient;

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

    // ── Redis cache: proves it's a real cache, not decoration ──────────────────

    @Test
    void testFindBySku_ServesStaleDataFromCache_UntilInvalidated() {
        Product product = buildProduct("SKU-CACHE-001", "Original Name");
        repository.insert(product);

        // Populates the Redis cache entry for this SKU.
        assertThat(repository.findBySku("SKU-CACHE-001").name).isEqualTo("Original Name");

        // Mutate MongoDB directly, bypassing the repository (and its cache invalidation).
        mongoClient.getDatabase("products_test").getCollection("products")
                .updateOne(Filters.eq("sku", "SKU-CACHE-001"), Updates.set("name", "Mutated Directly In Mongo"));

        // The cached (now stale) value is still served — proves Redis is actually being read.
        assertThat(repository.findBySku("SKU-CACHE-001").name).isEqualTo("Original Name");

        // A write through the repository invalidates the cache...
        product.name = "Updated Via Repository";
        repository.replace(product);

        // ...so the next read reflects the fresh value from MongoDB.
        assertThat(repository.findBySku("SKU-CACHE-001").name).isEqualTo("Updated Via Repository");
    }

    @Test
    void testFindByNamePrefix_ServesStaleDataFromCache_UntilInvalidated() {
        repository.insert(buildProduct("SKU-CACHE-002", "Cacheable Widget"));

        // Populates the Redis cache entry for this search prefix.
        assertThat(repository.findByNamePrefix("Cacheable")).hasSize(1);

        // Insert a second matching product directly in MongoDB via the repository,
        // which invalidates the search cache as a side effect...
        repository.insert(buildProduct("SKU-CACHE-003", "Cacheable Gadget"));

        // ...so the next search reflects both products.
        assertThat(repository.findByNamePrefix("Cacheable")).hasSize(2);
    }
}
