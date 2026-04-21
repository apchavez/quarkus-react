package com.products.application.usecase;

import com.products.adapters.out.persistence.MongoProductRepository;
import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.dto.ProductsPagedResponse;
import com.products.application.mapper.ProductMapper;
import com.products.domain.model.ApiResponse;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProductUseCase {

    private static final Logger log = Logger.getLogger(ProductUseCase.class);

    @Inject
    MongoProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    public Response insert(ProductRequest request) {
        try {
            Product product = productMapper.toEntity(request);
            product.create("SYSTEM");

            boolean inserted = productRepository.insert(product);
            if (!inserted) {
                return ApiResponse.conflictKey();
            }

            log.debug("Product inserted successfully");
            return ApiResponse.created(Map.of("id", product.id.toString()));

        } catch (Exception e) {
            log.error("Error inserting product", e);
            return ApiResponse.internalError();
        }
    }

    public Response update(String id, ProductRequest request) {
        try {
            if (!ObjectId.isValid(id)) {
                return ApiResponse.badRequest("id has invalid format");
            }

            ObjectId objectId = new ObjectId(id);

            Product existing = productRepository.findByObjectId(objectId);
            if (existing == null) {
                return ApiResponse.notFound();
            }

            productMapper.updateEntity(request, existing);
            existing.markUpdated("SYSTEM");

            Product updated = productRepository.update(objectId, product -> {
                product.sku = existing.sku;
                product.name = existing.name;
                product.description = existing.description;
                product.category = existing.category;
                product.price = existing.price;
                product.stock = existing.stock;
                product.active = existing.active;
                product.userUpdated = existing.userUpdated;
                product.updated = existing.updated;
            });

            if (updated == null) {
                return ApiResponse.notFound();
            }

            log.debug("Product updated successfully");
            return ApiResponse.updated();

        } catch (Exception e) {
            log.error("Error updating product", e);
            return ApiResponse.internalError();
        }
    }

    public Response delete(String id) {
        try {
            if (!ObjectId.isValid(id)) {
                return ApiResponse.badRequest("id has invalid format");
            }

            ObjectId objectId = new ObjectId(id);

            Product existing = productRepository.findByObjectId(objectId);
            if (existing == null) {
                return ApiResponse.notFound();
            }

            boolean deleted = productRepository.deleteByObjectId(objectId);
            if (!deleted) {
                return ApiResponse.notFound();
            }

            log.debug("Product deleted successfully");
            return ApiResponse.deleted();

        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ApiResponse.internalError();
        }
    }

    public Response findById(String id) {
        try {
            if (!ObjectId.isValid(id)) {
                return ApiResponse.badRequest("id has invalid format");
            }

            ObjectId objectId = new ObjectId(id);
            Product product = productRepository.findByObjectId(objectId);
            if (product == null) {
                return ApiResponse.notFound();
            }

            ProductResponse response = productMapper.toResponse(product);
            return ApiResponse.ok(response);

        } catch (Exception e) {
            log.error("Error finding product by id", e);
            return ApiResponse.internalError();
        }
    }

    public Response findAll(int page, int size) {
        try {
            PagedResponse<Product> paged = productRepository.findAllProducts(page, size);

            List<ProductResponse> responseList = productMapper.toResponseList(paged.data());

            ProductsPagedResponse response = new ProductsPagedResponse(
                    responseList,
                    paged.currentPage(),
                    paged.totalPages(),
                    paged.totalItems()
            );

            return ApiResponse.ok(response);

        } catch (Exception e) {
            log.error("Error finding all products", e);
            return ApiResponse.internalError();
        }
    }

    public Response findBySku(String sku) {
        try {
            Product product = productRepository.findBySku(sku);
            if (product == null) {
                return ApiResponse.notFound();
            }

            ProductResponse response = productMapper.toResponse(product);
            return ApiResponse.ok(response);

        } catch (Exception e) {
            log.error("Error finding product by sku", e);
            return ApiResponse.internalError();
        }
    }

    public Response findByNamePrefix(String prefix) {
        try {
            List<Product> products = productRepository.findByNamePrefix(prefix);
            if (products == null || products.isEmpty()) {
                return ApiResponse.notFound();
            }

            List<ProductResponse> responseList = productMapper.toResponseList(products);
            return ApiResponse.ok(responseList);

        } catch (Exception e) {
            log.error("Error finding products by prefix", e);
            return ApiResponse.internalError();
        }
    }
}