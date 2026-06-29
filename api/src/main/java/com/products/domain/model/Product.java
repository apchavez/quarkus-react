package com.products.domain.model;

import com.products.domain.model.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "products")
public class Product extends BaseEntity {

    public String sku;
    public String name;
    public String description;
    public String category;
    public Double price;
    public Integer stock;
    public Boolean active = true;

    public void create(String user) {
        this.markCreated(user);
    }

    public boolean hasStock() {
        return stock != null && stock > 0;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(active) && hasStock();
    }
}