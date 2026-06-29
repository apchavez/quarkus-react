package com.products.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProductRequest(

        @NotBlank(message = "sku is required")
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "sku has invalid format")
        String sku,

        @NotBlank(message = "name is required")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s.,:;_\\-()/]{1,255}$", message = "name has invalid format")
        String name,

        @Pattern(regexp = "^$|^[\\p{L}\\p{N}\\s.,:;_\\-()/]{1,255}$", message = "description has invalid format")
        String description,

        @NotBlank(message = "category is required")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s.,:;_\\-()/]{1,255}$", message = "category has invalid format")
        String category,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", message = "price must be greater than or equal to 0")
        Double price,

        @NotNull(message = "stock is required")
        @Min(value = 0, message = "stock must be greater than or equal to 0")
        Integer stock,

        @NotNull(message = "active is required")
        Boolean active
) {
}
