package ie.cortexx.model;

import java.util.List;

public record ReportDocument(String title, String subtitle, List<String> headers, List<List<String>> rows) {
    public ReportDocument {
        title = title == null ? "Report" : title;
        subtitle = subtitle == null ? "" : subtitle;
        headers = headers == null ? List.of() : List.copyOf(headers);
        rows = rows == null ? List.of() : rows.stream().map(List::copyOf).toList();
    }
}