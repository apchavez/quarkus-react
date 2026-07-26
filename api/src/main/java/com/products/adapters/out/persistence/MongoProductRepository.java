package com.products.adapters.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import com.products.application.port.out.ProductRepositoryPort;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MongoProductRepository implements ProductRepositoryPort, PanacheMongoRepository<Product> {

    private static final Logger log = Logger.getLogger(MongoProductRepository.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String PRODUCT_CACHE_PREFIX = "product-cache:";
    private static final String SEARCH_CACHE_PREFIX = "products-search-cache:";
    private static final String ACTIVE_CACHE_PREFIX = "products-active-cache:";

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ObjectMapper objectMapper;

    private ValueCommands<String, String> cache;
    private KeyCommands<String> cacheKeys;

    void init(@Observes StartupEvent event) {
        cache = redisDataSource.value(String.class);
        cacheKeys = redisDataSource.key();
    }

    @Override
    public boolean insert(Product product) {
        try {
            persist(product);
            invalidateCaches();
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
    public boolean replace(Product product) {
        try {
            persistOrUpdate(product);
            invalidateCaches();
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
    public boolean deleteByObjectId(ObjectId id) {
        boolean deleted = deleteById(id);
        if (deleted) {
            invalidateCaches();
        }
        return deleted;
    }

    @Override
    public Product findByObjectId(ObjectId id) {
        return find("_id", id).firstResult();
    }

    @Override
    public PagedResponse<Product> findAllActiveProducts(int page, int size) {
        String key = ACTIVE_CACHE_PREFIX + page + ":" + size;

        PagedResponse<Product> cached = readPagedResponse(key);
        if (cached != null) {
            return cached;
        }

        PanacheQuery<Product> query = find("active", true).page(Page.of(page, size));
        List<Product> data = query.list();
        int totalPages = query.pageCount();
        long totalItems = query.count();
        boolean last = page >= totalPages - 1;

        PagedResponse<Product> result = new PagedResponse<>(data, page, size, totalPages, totalItems, last);
        writeCache(key, result);
        return result;
    }

    @Override
    public PagedResponse<Product> findAllInactiveProducts(int page, int size) {
        // Low-traffic admin-only view — not cached, avoids the extra invalidation
        // surface for a query path that's rarely hit.
        PanacheQuery<Product> query = find("active", false).page(Page.of(page, size));

        List<Product> data = query.list();
        int totalPages = query.pageCount();
        long totalItems = query.count();
        boolean last = page >= totalPages - 1;

        return new PagedResponse<>(data, page, size, totalPages, totalItems, last);
    }

    @Override
    public List<Product> findByNamePrefix(String namePrefix) {
        String key = SEARCH_CACHE_PREFIX + namePrefix.toLowerCase();

        List<Product> cached = readList(key);
        if (cached != null) {
            return cached;
        }

        // Escape PCRE metacharacters before embedding in the regex anchor
        String escaped = namePrefix.replaceAll("[.+*?^${}()|\\[\\]\\\\]", "\\\\$0");
        List<Product> result = mongoCollection()
            .find(Filters.regex("name", "^" + escaped, "i"))
            .into(new ArrayList<>());

        writeCache(key, result);
        return result;
    }

    @Override
    public List<Product> findAllProducts() {
        return listAll();
    }

    @Override
    public Product findBySku(String sku) {
        String key = PRODUCT_CACHE_PREFIX + sku;

        Product cached = readValue(key, Product.class);
        if (cached != null) {
            return cached;
        }

        Product result = find("sku", sku).firstResult();
        if (result != null) {
            writeCache(key, result);
        }
        return result;
    }

    // ---- cache helpers — fail-open: any Redis error is logged and treated as a
    // cache miss/no-op so the product API keeps serving from MongoDB uninterrupted. ----

    private void invalidateCaches() {
        deleteByPattern(PRODUCT_CACHE_PREFIX + "*");
        deleteByPattern(SEARCH_CACHE_PREFIX + "*");
        deleteByPattern(ACTIVE_CACHE_PREFIX + "*");
    }

    private void deleteByPattern(String pattern) {
        try {
            List<String> keys = cacheKeys.keys(pattern);
            if (!keys.isEmpty()) {
                cacheKeys.del(keys.toArray(new String[0]));
            }
        } catch (Exception e) {
            log.warn("[CACHE] Redis no disponible al invalidar (fail-open) — patrón '" + pattern + "': "
                    + e.getMessage());
        }
    }

    private <T> T readValue(String key, Class<T> type) {
        try {
            String json = cache.get(key);
            return json == null ? null : objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("[CACHE] Redis no disponible en lectura (fail-open) — key '" + key + "': " + e.getMessage());
            return null;
        }
    }

    private PagedResponse<Product> readPagedResponse(String key) {
        try {
            String json = cache.get(key);
            return json == null ? null : objectMapper.readValue(json, new TypeReference<PagedResponse<Product>>() {});
        } catch (Exception e) {
            log.warn("[CACHE] Redis no disponible en lectura (fail-open) — key '" + key + "': " + e.getMessage());
            return null;
        }
    }

    private List<Product> readList(String key) {
        try {
            String json = cache.get(key);
            return json == null ? null : objectMapper.readValue(json, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            log.warn("[CACHE] Redis no disponible en lectura (fail-open) — key '" + key + "': " + e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, Object value) {
        try {
            cache.setex(key, CACHE_TTL.toSeconds(), objectMapper.writeValueAsString(value));
        } catch (Exception e) {
            log.warn("[CACHE] No se pudo escribir en Redis (fail-open) — key '" + key + "': " + e.getMessage());
        }
    }
}
