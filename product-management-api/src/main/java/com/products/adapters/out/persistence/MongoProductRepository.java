package com.products.adapters.out.persistence;

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
public class MongoProductRepository implements PanacheMongoRepository<Product> {

    private static final Logger log = Logger.getLogger(MongoProductRepository.class);

    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public boolean insert(Product product) {
        try {
            persist(product);
            return true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("E11000")) {
                log.debug("Duplicate product avoided");
                return false;
            }
            log.error("Error inserting product", e);
            throw e;
        }
    }

    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public void update(Product product) {
        persistOrUpdate(product);
    }

    @CacheInvalidateAll(cacheName = "product-cache")
    @CacheInvalidateAll(cacheName = "products-search-cache")
    public boolean deleteByObjectId(ObjectId id) {
        return deleteById(id);
    }

    public Product findByObjectId(ObjectId id) {
        return find("_id", id).firstResult();
    }

    public PagedResponse<Product> findAllProducts(int page, int size) {
        PanacheQuery<Product> query = findAll().page(Page.of(page, size));

        List<Product> data = query.list();
        int totalPages = query.pageCount();
        long totalItems = query.count();

        return new PagedResponse<>(data, page, totalPages, totalItems);
    }

    @CacheResult(cacheName = "products-search-cache")
    public List<Product> findByNamePrefix(String namePrefix) {
        // Escape PCRE metacharacters before embedding in the regex anchor
        String escaped = namePrefix.replaceAll("[.+*?^${}()|\\[\\]\\\\]", "\\\\$0");
        return find("{'name': {$regex: ?1, $options: 'i'}}", "^" + escaped).list();
    }

    @CacheResult(cacheName = "product-cache")
    public Product findBySku(String sku) {
        return find("sku", sku).firstResult();
    }
}
