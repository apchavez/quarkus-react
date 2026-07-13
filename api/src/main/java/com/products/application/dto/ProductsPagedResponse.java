package com.products.application.dto;

import java.util.List;

public record ProductsPagedResponse(
        List<ProductResponse> products,
        int currentPage,
        int totalPages,
        long totalItems) {
}