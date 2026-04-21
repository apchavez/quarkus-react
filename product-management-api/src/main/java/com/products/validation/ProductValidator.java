package com.products.validation;

import com.products.application.dto.ProductRequest;
import com.products.domain.model.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class ProductValidator {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,50}$");
    private static final Pattern TEXT_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,:;_\\-()/]{1,255}$");

    public Optional<Response> validateInsert(ProductRequest request) {
        if (request == null) {
            return Optional.of(ApiResponse.badRequest("Request body is required"));
        }

        if (request.sku() == null || request.sku().isBlank()) {
            return Optional.of(ApiResponse.badRequest("sku is required"));
        }

        if (!SKU_PATTERN.matcher(request.sku()).matches()) {
            return Optional.of(ApiResponse.badRequest("sku has invalid format"));
        }

        if (request.name() == null || request.name().isBlank()) {
            return Optional.of(ApiResponse.badRequest("name is required"));
        }

        if (!TEXT_PATTERN.matcher(request.name()).matches()) {
            return Optional.of(ApiResponse.badRequest("name has invalid format"));
        }

        if (request.description() != null
                && !request.description().isBlank()
                && !TEXT_PATTERN.matcher(request.description()).matches()) {
            return Optional.of(ApiResponse.badRequest("description has invalid format"));
        }

        if (request.category() == null || request.category().isBlank()) {
            return Optional.of(ApiResponse.badRequest("category is required"));
        }

        if (!TEXT_PATTERN.matcher(request.category()).matches()) {
            return Optional.of(ApiResponse.badRequest("category has invalid format"));
        }

        if (request.price() == null) {
            return Optional.of(ApiResponse.badRequest("price is required"));
        }

        if (request.price() < 0) {
            return Optional.of(ApiResponse.badRequest("price must be greater than or equal to 0"));
        }

        if (request.stock() == null) {
            return Optional.of(ApiResponse.badRequest("stock is required"));
        }

        if (request.stock() < 0) {
            return Optional.of(ApiResponse.badRequest("stock must be greater than or equal to 0"));
        }

        if (request.active() == null) {
            return Optional.of(ApiResponse.badRequest("active is required"));
        }

        return Optional.empty();
    }

    public Optional<Response> validateUpdate(String id, ProductRequest request) {
        Optional<Response> idValidation = validateObjectId(id);
        if (idValidation.isPresent()) {
            return idValidation;
        }

        return validateInsert(request);
    }

    public Optional<Response> validateDelete(String id) {
        return validateObjectId(id);
    }

    public Optional<Response> validateFind(String id) {
        return validateObjectId(id);
    }

    public Optional<Response> validateFindBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            return Optional.of(ApiResponse.badRequest("sku is required"));
        }

        if (!SKU_PATTERN.matcher(sku).matches()) {
            return Optional.of(ApiResponse.badRequest("sku has invalid format"));
        }

        return Optional.empty();
    }

    public Optional<Response> validateFindByNamePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return Optional.of(ApiResponse.badRequest("prefix is required"));
        }

        if (prefix.length() > 100) {
            return Optional.of(ApiResponse.badRequest("prefix is too long"));
        }

        return Optional.empty();
    }

    public Optional<Response> validateFindAll(Integer page, Integer size) {
        if (page == null) {
            return Optional.of(ApiResponse.badRequest("page is required"));
        }

        if (size == null) {
            return Optional.of(ApiResponse.badRequest("size is required"));
        }

        if (page < 0) {
            return Optional.of(ApiResponse.badRequest("page must be greater than or equal to 0"));
        }

        if (size <= 0) {
            return Optional.of(ApiResponse.badRequest("size must be greater than 0"));
        }

        return Optional.empty();
    }

    private Optional<Response> validateObjectId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.of(ApiResponse.badRequest("id is required"));
        }

        if (!ObjectId.isValid(id)) {
            return Optional.of(ApiResponse.badRequest("id has invalid format"));
        }

        return Optional.empty();
    }
}