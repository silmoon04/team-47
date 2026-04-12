package ie.cortexx.service;

import ie.cortexx.model.ReportDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ReportExportService {
    private static final float PAGE_MARGIN = 42f;
    private static final float TITLE_SIZE = 22f;
    private static final float SUBTITLE_SIZE = 11f;
    private static final float HEADER_SIZE = 10f;
    private static final float BODY_SIZE = 10f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float ROW_HEIGHT = 22f;
    private static final float CELL_PADDING = 7f;
    private static final Color TITLE_COLOR = new Color(0x111827);
    private static final Color SUBTITLE_COLOR = new Color(0x6b7280);
    private static final Color HEADER_FILL = new Color(0x1f2937);
    private static final Color HEADER_TEXT = Color.WHITE;
    private static final Color BORDER = new Color(0xd7dee7);
    private static final Color ZEBRA = new Color(0xf8fafc);

    public record ExportResult(Path filePath, String message) {
    }

    public ExportResult export(ReportDocument document, Path targetPath) throws IOException {
        Path pdfPath = ensurePdfPath(targetPath);
        if (pdfPath.getParent() != null) {
            java.nio.file.Files.createDirectories(pdfPath.getParent());
        }

        try (PDDocument pdf = new PDDocument()) {
            render(pdf, document);
            pdf.save(pdfPath.toFile());
        }

        return new ExportResult(pdfPath, "Report exported.");
    }

    private Path ensurePdfPath(Path path) {
        String name = path.getFileName().toString();
        return name.toLowerCase().endsWith(".pdf") ? path : path.resolveSibling(name + ".pdf");
    }

    private void render(PDDocument pdf, ReportDocument document) throws IOException {
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font subtitleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float availableWidth = PDRectangle.A4.getWidth() - (PAGE_MARGIN * 2);
        float[] widths = columnWidths(document, bodyFont, BODY_SIZE, availableWidth);

        PageContext context = newPage(pdf);
        float y = drawDocumentHeader(context.stream(), context.page(), document, titleFont, subtitleFont);
        y = drawTableHeader(context.stream(), y, widths, document.headers(), headerFont);

        for (int rowIndex = 0; rowIndex < document.rows().size(); rowIndex++) {
            if (y - ROW_HEIGHT < PAGE_MARGIN) {
                context.close();
                context = newPage(pdf);
                y = context.page().getMediaBox().getHeight() - PAGE_MARGIN;
                y = drawTableHeader(context.stream(), y, widths, document.headers(), headerFont);
            }
            y = drawTableRow(context.stream(), y, widths, document.headers(), document.rows().get(rowIndex), bodyFont, rowIndex % 2 == 0);
        }

        context.close();
    }

    private PageContext newPage(PDDocument pdf) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);
        return new PageContext(page, new PDPageContentStream(pdf, page));
    }

    private float drawDocumentHeader(PDPageContentStream stream, PDPage page, ReportDocument document,
                                     PDType1Font titleFont, PDType1Font subtitleFont) throws IOException {
        float y = page.getMediaBox().getHeight() - PAGE_MARGIN;
        drawText(stream, titleFont, TITLE_SIZE, TITLE_COLOR, PAGE_MARGIN, y, document.title());
        y -= 28f;
        if (!document.subtitle().isBlank()) {
            drawText(stream, subtitleFont, SUBTITLE_SIZE, SUBTITLE_COLOR, PAGE_MARGIN, y, document.subtitle());
            y -= 24f;
        }

        stream.setStrokingColor(BORDER);
        stream.moveTo(PAGE_MARGIN, y);
        stream.lineTo(page.getMediaBox().getWidth() - PAGE_MARGIN, y);
        stream.stroke();
        return y - 18f;
    }

    private float drawTableHeader(PDPageContentStream stream, float topY, float[] widths, List<String> headers,
                                  PDType1Font headerFont) throws IOException {
        float x = PAGE_MARGIN;
        for (int column = 0; column < headers.size(); column++) {
            float width = widths[column];
            drawCell(stream, x, topY, width, HEADER_HEIGHT, HEADER_FILL, BORDER);
            drawCellText(stream, headerFont, HEADER_SIZE, HEADER_TEXT, x, topY, width, HEADER_HEIGHT, headers.get(column), false);
            x += width;
        }
        return topY - HEADER_HEIGHT;
    }

    private float drawTableRow(PDPageContentStream stream, float topY, float[] widths, List<String> headers,
                               List<String> row, PDType1Font bodyFont, boolean zebra) throws IOException {
        float x = PAGE_MARGIN;
        Color fill = zebra ? ZEBRA : Color.WHITE;
        for (int column = 0; column < widths.length; column++) {
            float width = widths[column];
            String value = column < row.size() ? row.get(column) : "";
            drawCell(stream, x, topY, width, ROW_HEIGHT, fill, BORDER);
            drawCellText(stream, bodyFont, BODY_SIZE, TITLE_COLOR, x, topY, width, ROW_HEIGHT, value, isNumericColumn(headers.get(column)));
            x += width;
        }
        return topY - ROW_HEIGHT;
    }

    private void drawCell(PDPageContentStream stream, float x, float topY, float width, float height,
                          Color fill, Color border) throws IOException {
        stream.setNonStrokingColor(fill);
        stream.addRect(x, topY - height, width, height);
        stream.fill();

        stream.setStrokingColor(border);
        stream.addRect(x, topY - height, width, height);
        stream.stroke();
    }

    private void drawCellText(PDPageContentStream stream, PDType1Font font, float fontSize, Color color,
                              float x, float topY, float width, float height, String value, boolean rightAlign) throws IOException {
        String fitted = fitText(font, fontSize, sanitize(value), width - (CELL_PADDING * 2));
        float textWidth = font.getStringWidth(fitted) / 1000f * fontSize;
        float textX = rightAlign ? x + width - CELL_PADDING - textWidth : x + CELL_PADDING;
        float textY = topY - ((height - fontSize) / 2f) - 3f;
        drawText(stream, font, fontSize, color, textX, textY, fitted);
    }

    private void drawText(PDPageContentStream stream, PDType1Font font, float fontSize, Color color,
                          float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(sanitize(text));
        stream.endText();
    }

    private float[] columnWidths(ReportDocument document, PDType1Font font, float fontSize, float totalWidth) throws IOException {
        int columns = document.headers().size();
        float[] weights = new float[columns];
        float weightSum = 0f;

        for (int column = 0; column < columns; column++) {
            float maxWidth = stringWidth(font, fontSize, document.headers().get(column));
            for (List<String> row : document.rows()) {
                if (column < row.size()) {
                    maxWidth = Math.max(maxWidth, stringWidth(font, fontSize, row.get(column)));
                }
            }
            weights[column] = Math.max(72f, Math.min(170f, maxWidth + (CELL_PADDING * 2)));
            weightSum += weights[column];
        }

        float[] widths = new float[columns];
        float used = 0f;
        for (int column = 0; column < columns; column++) {
            widths[column] = totalWidth * (weights[column] / weightSum);
            used += widths[column];
        }
        widths[columns - 1] += totalWidth - used;
        return widths;
    }

    private float stringWidth(PDType1Font font, float fontSize, String value) throws IOException {
        return font.getStringWidth(sanitize(value)) / 1000f * fontSize;
    }

    private String fitText(PDType1Font font, float fontSize, String value, float maxWidth) throws IOException {
        if (stringWidth(font, fontSize, value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        String trimmed = value;
        while (!trimmed.isEmpty() && stringWidth(font, fontSize, trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? ellipsis : trimmed + ellipsis;
    }

    private boolean isNumericColumn(String header) {
        String normalized = header == null ? "" : header.toLowerCase();
        return normalized.contains("qty")
            || normalized.contains("items")
            || normalized.contains("cost")
            || normalized.contains("retail")
            || normalized.contains("value")
            || normalized.contains("total")
            || normalized.contains("balance")
            || normalized.contains("limit");
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private record PageContext(PDPage page, PDPageContentStream stream) {
        private void close() throws IOException {
            stream.close();
        }
    }
}