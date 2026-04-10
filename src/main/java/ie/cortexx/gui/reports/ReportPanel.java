package ie.cortexx.gui.reports;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.ReportDocument;
import ie.cortexx.service.CustomerService;
import ie.cortexx.service.ReportExportService;
import ie.cortexx.service.ReportService;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private final CustomerService customerService = new CustomerService();
    private final ReportExportService reportExportService = new ReportExportService();
    private JPanel contentArea;
    private JComboBox<String> reportType;
    private JTextField fromField;
    private JTextField toField;
    private ReportDocument currentReport;

    private record TurnoverRow(String saleId, String customer, String date, String payment, String total) {}
    private record StockRow(String product, int qty, String cost, String retail, String value, String status) {}
    private record DebtRow(String customer, String account, String status, String balance, String limit) {}

    public ReportPanel() {
        UI.applyPanel(this);

        JPanel toolbar = UI.toolbar();
        JPanel left = UI.flowRow(8);
        reportType = new JComboBox<>(new String[]{
            "Turnover", "Stock", "Debt Summary", "Late Customers"});
        left.add(UI.field("Report Type", reportType));
        fromField = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
        toField = new JTextField(LocalDate.now().toString(), 10);
        left.add(UI.field("From", fromField));
        left.add(UI.field("To", toField));
        toolbar.add(left, BorderLayout.WEST);
        JButton generate = UI.primaryButton("Generate");
        generate.addActionListener(e -> refreshReport());
        JButton export = UI.button("Export");
        export.addActionListener(e -> exportCurrentReport());
        toolbar.add(UI.buttonRow(generate, export), BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        contentArea = UI.transparentPanel(0);
        add(contentArea, BorderLayout.CENTER);
        refreshReport();

        reportType.addActionListener(e -> refreshReport());
    }

    private void refreshReport() {
        try {
            switch ((String) reportType.getSelectedItem()) {
                case "Turnover" -> showTurnover();
                case "Stock" -> showStock();
                case "Debt Summary" -> showDebt();
                case "Late Customers" -> showLateCustomers();
                default -> clearReport("No data");
            }
        } catch (Exception error) {
            clearReport(error.getMessage());
        }
    }

    private void clearReport(String message) {
        currentReport = null;
        UI.swap(contentArea, UI.emptyState(message));
    }

    private void showTurnover() throws Exception {
        Map<Integer, String> customerNames = new HashMap<>();
        for (Customer customer : customerService.findAll()) {
            customerNames.put(customer.getCustomerId(), customer.getName());
        }

        List<TurnoverRow> rows = reportService.getTurnover(LocalDate.parse(fromField.getText().trim()), LocalDate.parse(toField.getText().trim()))
            .stream()
            .map(sale -> new TurnoverRow(
                "#" + sale.getSaleId(),
                sale.getCustomerId() != null ? customerNames.getOrDefault(sale.getCustomerId(), "Unknown") : "Walk-in",
                sale.getSaleDate() != null ? sale.getSaleDate().toLocalDate().toString() : "",
                text(sale.getPaymentMethod()),
                money(sale.getTotalAmount())
            )).toList();

        var table = UI.table(
            UI.monoCol("Sale #", TurnoverRow::saleId),
            UI.col("Customer", TurnoverRow::customer),
            UI.col("Date", TurnoverRow::date),
            UI.badgeCol("Payment", TurnoverRow::payment),
            UI.monoCol("Total", TurnoverRow::total)
        ).rows(rows);
        currentReport = new ReportDocument(
            "Turnover Report",
            fromField.getText().trim() + " to " + toField.getText().trim(),
            List.of("Sale #", "Customer", "Date", "Payment", "Total"),
            rows.stream().map(row -> List.of(row.saleId(), row.customer(), row.date(), row.payment(), row.total())).toList()
        );
        UI.swap(contentArea, rows.isEmpty() ? UI.emptyState("No turnover data") : table.scroll());
    }

    private void showStock() throws Exception {
        List<StockRow> rows = reportService.getStock().stream().map(item -> {
            BigDecimal retail = item.getCostPrice().multiply(BigDecimal.ONE.add(item.getMarkupRate()));
            BigDecimal value = retail.multiply(BigDecimal.valueOf(item.getQuantity()));
            return new StockRow(
                item.getProductName(),
                item.getQuantity(),
                money(item.getCostPrice()),
                money(retail),
                money(value),
                item.isLowStock() ? "LOW_STOCK" : "IN_STOCK"
            );
        }).toList();

        var table = UI.table(
            UI.col("Product", StockRow::product),
            UI.monoCol("Qty", StockRow::qty),
            UI.monoCol("Cost", StockRow::cost),
            UI.monoCol("Retail", StockRow::retail),
            UI.monoCol("Value", StockRow::value),
            UI.badgeCol("Status", StockRow::status)
        ).rows(rows);
        currentReport = new ReportDocument(
            "Stock Report",
            "Generated " + LocalDate.now(),
            List.of("Product", "Qty", "Cost", "Retail", "Value", "Status"),
            rows.stream().map(row -> List.of(row.product(), String.valueOf(row.qty()), row.cost(), row.retail(), row.value(), row.status())).toList()
        );
        UI.swap(contentArea, rows.isEmpty() ? UI.emptyState("No stock data") : table.scroll());
    }

    private void showDebt() throws Exception {
        showDebtReport("Debt Summary Report", "No debt data", reportService.getDebtSummary());
    }

    private void showLateCustomers() throws Exception {
        showDebtReport(
            "Late Customers Report",
            "No late customers",
            reportService.getDebtSummary().stream()
                .filter(customer -> customer.getAccountStatus() == AccountStatus.SUSPENDED || customer.getAccountStatus() == AccountStatus.IN_DEFAULT)
                .toList()
        );
    }

    private void showDebtReport(String title, String emptyMessage, List<Customer> customers) {
        List<DebtRow> rows = customers.stream().map(customer -> new DebtRow(
            customer.getName(),
            text(customer.getAccountNo()),
            customer.getAccountStatus().name(),
            money(customer.getOutstandingBalance()),
            money(customer.getCreditLimit())
        )).toList();

        var table = UI.table(
            UI.col("Customer", DebtRow::customer),
            UI.monoCol("Account", DebtRow::account),
            UI.badgeCol("Status", DebtRow::status),
            UI.monoCol("Balance", DebtRow::balance),
            UI.monoCol("Limit", DebtRow::limit)
        ).rows(rows);
        currentReport = new ReportDocument(
            title,
            "Generated " + LocalDate.now(),
            List.of("Customer", "Account", "Status", "Balance", "Limit"),
            rows.stream().map(row -> List.of(row.customer(), row.account(), row.status(), row.balance(), row.limit())).toList()
        );
        UI.swap(contentArea, rows.isEmpty() ? UI.emptyState(emptyMessage) : table.scroll());
    }

    private void exportCurrentReport() {
        if (currentReport == null || currentReport.rows().isEmpty()) {
            UI.notifyInfo(this, "Generate a report before exporting.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(slug(currentReport.title()) + "-" + LocalDate.now() + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            ReportExportService.ExportResult result = reportExportService.export(currentReport, chooser.getSelectedFile().toPath());
            UI.notifySuccess(this, result.message() + " Saved to " + result.filePath());
        } catch (IOException error) {
            UI.notifyError(this, error.getMessage());
        }
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private String money(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String text(String value) {
        return value != null ? value : "";
    }
}
