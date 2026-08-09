package com.products.application.report;

import com.products.application.dto.ProductResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@ApplicationScoped
public class PdfReportGenerator {

    private static final float MARGIN = 50f;
    private static final float ROW_HEIGHT = 16f;
    private static final int ROWS_PER_PAGE = 42;
    private static final float[] COLUMN_X = {MARGIN, MARGIN + 90, MARGIN + 260, MARGIN + 340, MARGIN + 400, MARGIN + 450};

    public byte[] generate(List<ProductResponse> products) {
        try (PDDocument document = new PDDocument()) {
            List<List<ProductResponse>> pages = paginate(products, ROWS_PER_PAGE);
            if (pages.isEmpty()) {
                pages = List.of(List.of());
            }

            double totalValue = products.stream()
                    .mapToDouble(p -> nullToZero(p.price()) * nullToZero(p.stock()))
                    .sum();

            for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    float y = PDRectangle.A4.getHeight() - MARGIN;

                    if (pageIndex == 0) {
                        y = writeTitle(cs, y);
                    }
                    y = writeTableHeader(cs, y);

                    for (ProductResponse product : pages.get(pageIndex)) {
                        writeRow(cs, y, product);
                        y -= ROW_HEIGHT;
                    }

                    if (pageIndex == pages.size() - 1) {
                        writeSummary(cs, y - ROW_HEIGHT, products.size(), totalValue);
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate PDF report", e);
        }
    }

    private float writeTitle(PDPageContentStream cs, float y) throws IOException {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Product Catalogue Report");
        cs.endText();
        return y - 30;
    }

    private float writeTableHeader(PDPageContentStream cs, float y) throws IOException {
        String[] headers = {"SKU", "Name", "Category", "Price", "Stock", "Active"};
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.beginText();
        for (int i = 0; i < headers.length; i++) {
            cs.newLineAtOffset(i == 0 ? COLUMN_X[i] : COLUMN_X[i] - COLUMN_X[i - 1], i == 0 ? y : 0);
            cs.showText(headers[i]);
        }
        cs.endText();
        return y - ROW_HEIGHT;
    }

    private void writeRow(PDPageContentStream cs, float y, ProductResponse product) throws IOException {
        String[] values = {
                nullToDash(product.sku()),
                truncate(nullToDash(product.name()), 28),
                nullToDash(product.category()),
                String.format("%.2f", nullToZero(product.price())),
                String.valueOf(nullToZero(product.stock())),
                Boolean.TRUE.equals(product.active()) ? "yes" : "no"
        };
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        cs.beginText();
        for (int i = 0; i < values.length; i++) {
            cs.newLineAtOffset(i == 0 ? COLUMN_X[i] : COLUMN_X[i] - COLUMN_X[i - 1], i == 0 ? y : 0);
            cs.showText(values[i]);
        }
        cs.endText();
    }

    private void writeSummary(PDPageContentStream cs, float y, int totalCount, double totalValue) throws IOException {
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y - 10);
        cs.showText(String.format("Total products: %d    Total inventory value: %.2f", totalCount, totalValue));
        cs.endText();
    }

    private List<List<ProductResponse>> paginate(List<ProductResponse> products, int pageSize) {
        List<List<ProductResponse>> pages = new java.util.ArrayList<>();
        for (int i = 0; i < products.size(); i += pageSize) {
            pages.add(products.subList(i, Math.min(i + pageSize, products.size())));
        }
        return pages;
    }

    private double nullToZero(Double value) {
        return value == null ? 0d : value;
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
