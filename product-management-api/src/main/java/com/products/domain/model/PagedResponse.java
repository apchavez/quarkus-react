package com.products.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(List<T> data, int currentPage, int totalPages, long totalItems) {
}
