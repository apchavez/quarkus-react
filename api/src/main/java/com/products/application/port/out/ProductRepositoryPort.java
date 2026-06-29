package com.products.application.port.out;

import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import org.bson.types.ObjectId;

import java.util.List;

public interface ProductRepositoryPort {

    boolean insert(Product product);

    boolean replace(Product product);

    boolean deleteByObjectId(ObjectId id);

    Product findByObjectId(ObjectId id);

    PagedResponse<Product> findAllProducts(int page, int size);

    Product findBySku(String sku);

    List<Product> findByNamePrefix(String namePrefix);
}
