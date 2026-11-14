package com.products.application.usecase;

import com.products.application.dto.ImportResult;
import com.products.application.dto.ImportRowError;
import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.dto.ProductsPagedResponse;
import com.products.application.mapper.ProductMapper;
import com.products.application.port.in.ProductServicePort;
import com.products.application.port.out.ProductEventPublisherPort;
import com.products.application.port.out.ProductRepositoryPort;
import com.products.domain.event.ProductEvent;
import com.products.domain.event.ProductEventType;
import com.products.domain.model.PagedResponse;
import com.products.domain.model.Product;
import com.products.exception.DuplicateSkuException;
import com.products.exception.ProductNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductUseCase implements ProductServicePort {

    private static final Logger log = Logger.getLogger(ProductUseCase.class);

    @Inject
    ProductRepositoryPort productRepository;

    @Inject
    ProductMapper productMapper;

    @Inject
    ProductEventPublisherPort eventPublisher;

    @Inject
    Validator validator;

    @Override
    public ProductResponse insert(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.create("SYSTEM");

        boolean inserted = productRepository.insert(product);
        if (!inserted) {
            throw new DuplicateSkuException();
        }

        eventPublisher.publish(ProductEvent.of(ProductEventType.PRODUCT_CREATED, product));
        log.debug("Product inserted successfully");
        return productMapper.toResponse(product);
    }

    @Override
    public void update(String id, ProductRequest request) {
        ObjectId objectId = new ObjectId(id);

        Product existing = productRepository.findByObjectId(objectId);
        if (existing == null) {
            throw new ProductNotFoundException();
        }

        productMapper.updateEntity(request, existing);
        existing.markUpdated("SYSTEM");

        boolean updated = productRepository.replace(existing);
        if (!updated) {
            throw new DuplicateSkuException();
        }

        eventPublisher.publish(ProductEvent.of(ProductEventType.PRODUCT_UPDATED, existing));
        log.debug("Product updated successfully");
    }

    @Override
    public void delete(String id) {
        ObjectId objectId = new ObjectId(id);

        Product existing = productRepository.findByObjectId(objectId);
        if (existing == null) {
            throw new ProductNotFoundException();
        }

        boolean deleted = productRepository.deleteByObjectId(objectId);
        if (!deleted) {
            throw new ProductNotFoundException();
        }

        eventPublisher.publish(ProductEvent.of(ProductEventType.PRODUCT_DELETED, existing));
        log.debug("Product deleted successfully");
    }

    @Override
    public ProductResponse findById(String id) {
        ObjectId objectId = new ObjectId(id);
        Product product = productRepository.findByObjectId(objectId);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        return productMapper.toResponse(product);
    }

    @Override
    public ProductsPagedResponse findAllActive(int page, int size) {
        PagedResponse<Product> paged = productRepository.findAllActiveProducts(page, size);

        List<ProductResponse> responseList = productMapper.toResponseList(paged.data());

        return new ProductsPagedResponse(
                responseList, paged.page(), paged.size(), paged.totalItems(), paged.totalPages(), paged.last());
    }

    @Override
    public ProductsPagedResponse findAllInactive(int page, int size) {
        PagedResponse<Product> paged = productRepository.findAllInactiveProducts(page, size);

        List<ProductResponse> responseList = productMapper.toResponseList(paged.data());

        return new ProductsPagedResponse(
                responseList, paged.page(), paged.size(), paged.totalItems(), paged.totalPages(), paged.last());
    }

    @Override
    public ProductResponse findBySku(String sku) {
        Product product = productRepository.findBySku(sku);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> findByNamePrefix(String prefix) {
        List<Product> products = productRepository.findByNamePrefix(prefix);
        return productMapper.toResponseList(products);
    }

    @Override
    public ImportResult importProducts(InputStream csvInput) {
        List<ImportRowError> errors = new ArrayList<>();
        int imported = 0;
        int failed = 0;
        int rowNum = 1; // row 1 is the header

        try (Reader reader = new InputStreamReader(csvInput, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                rowNum++;
                try {
                    ProductRequest request = toProductRequest(record);
                    Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
                    if (!violations.isEmpty()) {
                        String message = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining("; "));
                        errors.add(new ImportRowError(rowNum, message));
                        failed++;
                        continue;
                    }
                    insert(request);
                    imported++;
                } catch (DuplicateSkuException e) {
                    errors.add(new ImportRowError(rowNum, "a product with this sku already exists"));
                    failed++;
                } catch (Exception e) {
                    errors.add(new ImportRowError(rowNum, "could not parse row: " + e.getMessage()));
                    failed++;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read CSV file: " + e.getMessage(), e);
        }

        log.debug("CSV import finished: " + imported + " imported, " + failed + " failed");
        return new ImportResult(imported, failed, errors);
    }

    @Override
    public List<ProductResponse> findAllForReport() {
        List<Product> products = productRepository.findAllProducts();
        return productMapper.toResponseList(products);
    }

    private ProductRequest toProductRequest(CSVRecord record) {
        String description = record.isMapped("description") && record.get("description") != null
                ? record.get("description")
                : "";
        return new ProductRequest(
                record.get("sku"),
                record.get("name"),
                description,
                record.get("category"),
                Double.parseDouble(record.get("price")),
                Integer.parseInt(record.get("stock")),
                Boolean.parseBoolean(record.get("active")));
    }
}
