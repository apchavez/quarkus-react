package com.products.application.report;

import com.products.application.dto.ProductResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelReportGeneratorTest {

    private final ExcelReportGenerator generator = new ExcelReportGenerator();

    @Test
    void generate_withProducts_producesReadableWorkbookWithCorrectRows() throws IOException {
        List<ProductResponse> products = List.of(
                new ProductResponse("1", "SKU-1", "Product One", "Desc", "Tech", 10.0, 5, true, "SYSTEM", Instant.now(), Instant.now()),
                new ProductResponse("2", "SKU-2", "Product Two", "Desc", "Tech", 20.0, 3, false, "SYSTEM", Instant.now(), Instant.now())
        );

        byte[] xlsx = generator.generate(products);
        assertThat(xlsx).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = workbook.getSheet("Products");
            assertThat(sheet).isNotNull();

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("SKU");

            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("SKU-1");
            assertThat(row1.getCell(4).getNumericCellValue()).isEqualTo(10.0);

            Row row2 = sheet.getRow(2);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("SKU-2");
        }
    }

    @Test
    void generate_withEmptyList_producesWorkbookWithOnlyHeaderAndSummary() throws IOException {
        byte[] xlsx = generator.generate(List.of());
        assertThat(xlsx).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = workbook.getSheet("Products");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("SKU");
        }
    }
}
