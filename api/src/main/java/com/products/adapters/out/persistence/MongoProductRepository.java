package com.products.adapters.out.persistence;

import com.mongodb.MongoWriteException;
import com.products.application.port.out.ProductRepositoryPort;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class MongoProductRepository implements ProductRepositoryPort, PanacheMongoRepository<Product> {

    private static final Logger log = Logger.getLogger(MongoProductRepository.class);

    @Override
    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public boolean insert(Product product) {
        try {
            persist(product);
            return true;
        } catch (MongoWriteException e) {
            if (e.getCode() == 11000) {
                log.debug("Duplicate product SKU prevented");
                return false;
            }
            throw e;
        }
    }

    @Override
    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public boolean replace(Product product) {
        try {
            persistOrUpdate(product);
            return true;
        } catch (MongoWriteException e) {
            if (e.getCode() == 11000) {
                log.debug("Duplicate SKU on update prevented");
                return false;
            }
            throw e;
        }
    }

    @Override
    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public boolean deleteByObjectId(ObjectId id) {
        return deleteById(id);
    }

    @Override
    public Product findByObjectId(ObjectId id) {
        return find("_id", id).firstResult();
    }

    @Override
    public PagedResponse<Product> findAllProducts(int page, int size) {
        PanacheQuery<Product> query = findAll().page(Page.of(page, size));

        List<Product> data = query.list();
        int totalPages = query.pageCount();
        long totalItems = query.count();

        return new PagedResponse<>(data, page, totalPages, totalItems);
    }

    @Override
    @CacheResult(cacheName = "products-search-cache")
    public List<Product> findByNamePrefix(String namePrefix) {
        // Escape PCRE metacharacters before embedding in the regex anchor
        String escaped = namePrefix.replaceAll("[.+*?^${}()|\\[\\]\\\\]", "\\\\$0");
        return find("{'name': {$regex: ?1, $options: 'i'}}", "^" + escaped).list();
    }

    @Override
    @CacheResult(cacheName = "product-cache")
    public Product findBySku(String sku) {
        return find("sku", sku).firstResult();
    }
}
