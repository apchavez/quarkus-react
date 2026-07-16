package com.products.application.port.in;

import com.products.application.dto.ImportResult;
import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.dto.ProductsPagedResponse;

import java.io.InputStream;
import java.util.List;

public interface ProductServicePort {

    ProductResponse insert(ProductRequest request);

    ImportResult importProducts(InputStream csvInput);

    List<ProductResponse> findAllForReport();

    void update(String id, ProductRequest request);

    void delete(String id);

    ProductResponse findById(String id);

    ProductsPagedResponse findAllActive(int page, int size);

    ProductsPagedResponse findAllInactive(int page, int size);

    ProductResponse findBySku(String sku);

    List<ProductResponse> findByNamePrefix(String prefix);
}
