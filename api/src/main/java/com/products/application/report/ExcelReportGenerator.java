package com.products.application.report;

import com.products.application.dto.ProductResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@ApplicationScoped
public class ExcelReportGenerator {

    private static final String[] HEADERS = {"SKU", "Name", "Description", "Category", "Price", "Stock", "Active"};

    public byte[] generate(List<ProductResponse> products) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            SXSSFSheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            double totalValue = 0d;
            int rowIndex = 1;
            for (ProductResponse product : products) {
                Row row = sheet.createRow(rowIndex++);
                double price = product.price() == null ? 0d : product.price();
                int stock = product.stock() == null ? 0 : product.stock();

                row.createCell(0).setCellValue(nullToEmpty(product.sku()));
                row.createCell(1).setCellValue(nullToEmpty(product.name()));
                row.createCell(2).setCellValue(nullToEmpty(product.description()));
                row.createCell(3).setCellValue(nullToEmpty(product.category()));
                row.createCell(4).setCellValue(price);
                row.createCell(5).setCellValue(stock);
                row.createCell(6).setCellValue(Boolean.TRUE.equals(product.active()));

                totalValue += price * stock;
            }

            Row summaryRow = sheet.createRow(rowIndex + 1);
            Cell summaryLabelCell = summaryRow.createCell(0);
            summaryLabelCell.setCellValue("Total products: " + products.size());
            summaryLabelCell.setCellStyle(headerStyle);
            Cell summaryValueCell = summaryRow.createCell(4);
            summaryValueCell.setCellValue("Total inventory value: " + String.format("%.2f", totalValue));
            summaryValueCell.setCellStyle(headerStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel report", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
