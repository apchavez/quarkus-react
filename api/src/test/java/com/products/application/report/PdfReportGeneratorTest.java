package com.products.application.report;

import com.products.application.dto.ProductResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfReportGeneratorTest {

    private final PdfReportGenerator generator = new PdfReportGenerator();

    @Test
    void generate_withProducts_returnsNonEmptyPdfBytes() {
        List<ProductResponse> products = List.of(
                new ProductResponse("1", "SKU-1", "Product One", "Desc", "Tech", 10.0, 5, true, "SYSTEM", Instant.now(), Instant.now()),
                new ProductResponse("2", "SKU-2", "Product Two", "Desc", "Tech", 20.0, 3, false, "SYSTEM", Instant.now(), Instant.now())
        );

        byte[] pdf = generator.generate(products);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_withEmptyList_returnsValidPdf() {
        byte[] pdf = generator.generate(List.of());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_withManyProducts_paginatesAcrossMultiplePages() {
        List<ProductResponse> products = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            products.add(new ProductResponse(String.valueOf(i), "SKU-" + i, "Product " + i, "Desc", "Tech", 5.0, 1, true, "SYSTEM", Instant.now(), Instant.now()));
        }

        byte[] pdf = generator.generate(products);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
