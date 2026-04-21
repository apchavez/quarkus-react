package com.products.adapters.out.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import com.products.domain.model.Product;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MongoProductRepositoryTest {

    @Inject
    MongoProductRepository repository;

    @Inject
    MongoClient mongoClient;

    @BeforeEach
    void setUp() {
        mongoClient.getDatabase("products_test")
                .getCollection("products")
                .deleteMany(new Document());

        mongoClient.getDatabase("products_test")
                .getCollection("products")
                .createIndex(
                        new Document("sku", 1),
                        new IndexOptions().unique(true));
    }

    @AfterEach
    void cleanUp() {
        mongoClient.getDatabase("products_test")
                .getCollection("products")
                .deleteMany(new Document());
    }

    @Test
    void testInsert_Success() {
        Product product = new Product();
        product.id = new ObjectId();
        product.sku = "SKU-IT-001";
        product.name = "Laptop Test";
        product.description = "Integration test";
        product.category = "Technology";
        product.price = 100.0;
        product.stock = 10;
        product.active = true;
        product.create("TEST");

        boolean inserted = repository.insert(product);

        assertThat(inserted).isTrue();

        Product found = repository.findBySku("SKU-IT-001");
        assertThat(found).isNotNull();
        assertThat(found.name).isEqualTo("Laptop Test");
    }

    @Test
    void testInsert_DuplicateSku_ReturnsFalse() {
        Product p1 = new Product();
        p1.id = new ObjectId();
        p1.sku = "SKU-DUP-001";
        p1.name = "Product 1";
        p1.description = "First";
        p1.category = "Technology";
        p1.price = 10.0;
        p1.stock = 1;
        p1.active = true;
        p1.create("TEST");

        Product p2 = new Product();
        p2.id = new ObjectId();
        p2.sku = "SKU-DUP-001";
        p2.name = "Product 2";
        p2.description = "Second";
        p2.category = "Technology";
        p2.price = 20.0;
        p2.stock = 2;
        p2.active = true;
        p2.create("TEST");

        boolean inserted1 = repository.insert(p1);
        boolean inserted2 = repository.insert(p2);

        assertThat(inserted1).isTrue();
        assertThat(inserted2).isFalse();
    }

    @Test
    void testFindByObjectId_Success() {
        Product product = new Product();
        product.id = new ObjectId();
        product.sku = "SKU-FIND-001";
        product.name = "Find Test";
        product.description = "Find by id";
        product.category = "Technology";
        product.price = 50.0;
        product.stock = 5;
        product.active = true;
        product.create("TEST");

        repository.insert(product);

        Product found = repository.findByObjectId(product.id);

        assertThat(found).isNotNull();
        assertThat(found.sku).isEqualTo("SKU-FIND-001");
    }

    @Test
    void testDeleteByObjectId_Success() {
        Product product = new Product();
        product.id = new ObjectId();
        product.sku = "SKU-DEL-001";
        product.name = "Delete Test";
        product.description = "Delete by id";
        product.category = "Technology";
        product.price = 30.0;
        product.stock = 3;
        product.active = true;
        product.create("TEST");

        repository.insert(product);

        boolean deleted = repository.deleteByObjectId(product.id);

        assertThat(deleted).isTrue();
        assertThat(repository.findByObjectId(product.id)).isNull();
    }

    @Test
    void testFindAllProducts_Success() {
        Product product = new Product();
        product.id = new ObjectId();
        product.sku = "SKU-PAGE-001";
        product.name = "Paged Test";
        product.description = "Paged query";
        product.category = "Technology";
        product.price = 70.0;
        product.stock = 7;
        product.active = true;
        product.create("TEST");

        repository.insert(product);

        var paged = repository.findAllProducts(0, 10);

        assertThat(paged).isNotNull();
        assertThat(paged.data()).isNotEmpty();
        assertThat(paged.totalItems()).isGreaterThan(0);
    }
}