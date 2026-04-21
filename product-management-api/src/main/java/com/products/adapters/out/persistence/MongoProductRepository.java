package com.products.adapters.out.persistence;

import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import io.quarkus.cache.CacheResult;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class MongoProductRepository implements PanacheMongoRepository<Product> {

    private static final Logger log = Logger.getLogger(MongoProductRepository.class);

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

    public Product update(ObjectId id, Consumer<Product> updater) {
        try {
            Product existing = find("_id", id).firstResult();
            if (existing == null) {
                return null;
            }

            updater.accept(existing);
            persistOrUpdate(existing);
            return existing;
        } catch (Exception e) {
            log.errorf(e, "Error updating product with id %s", id);
            throw e;
        }
    }

    public boolean deleteByObjectId(ObjectId id) {
        try {
            return deleteById(id);
        } catch (Exception e) {
            log.errorf(e, "Error deleting product with id %s", id);
            throw e;
        }
    }

    public Product findByObjectId(ObjectId id) {
        try {
            return find("_id", id).firstResult();
        } catch (Exception e) {
            log.errorf(e, "Error finding product by id %s", id);
            return null;
        }
    }

    public PagedResponse<Product> findAllProducts(int page, int size) {
        try {
            PanacheQuery<Product> query = findAll().page(Page.of(page, size));

            List<Product> data = query.list();
            int totalPages = query.pageCount();
            long totalItems = query.count();

            return new PagedResponse<>(data, page, totalPages, totalItems);
        } catch (Exception e) {
            log.error("Error finding all products", e);
            return new PagedResponse<>(Collections.emptyList(), page, 0, 0);
        }
    }

    @CacheResult(cacheName = "products-search-cache")
    public List<Product> findByNamePrefix(String namePrefix) {
        try {
            return find("name like ?1", namePrefix + "%").list();
        } catch (Exception e) {
            log.errorf(e, "Error finding products by name prefix %s", namePrefix);
            return Collections.emptyList();
        }
    }

    @CacheResult(cacheName = "product-cache")
    public Product findBySku(String sku) {
        try {
            return find("sku", sku).firstResult();
        } catch (Exception e) {
            log.errorf(e, "Error finding product by SKU %s", sku);
            return null;
        }
    }
}