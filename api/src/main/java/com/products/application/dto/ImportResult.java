package com.products.application.dto;

import java.util.List;

public record ImportResult(int imported, int failed, List<ImportRowError> errors) {
}
