package com.products.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseMongoIntegrationTest {

    @Inject
    MongoClient mongoClient;

    @BeforeEach
    void setUpDb() {
        var col = mongoClient.getDatabase("products_test").getCollection("products");
        col.deleteMany(new Document());
        col.createIndex(new Document("sku", 1), new IndexOptions().unique(true));
    }

    @AfterEach
    void tearDownDb() {
        mongoClient.getDatabase("products_test").getCollection("products").deleteMany(new Document());
    }
}
