package ie.cortexx.gui.reports;

import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Sale;
import ie.cortexx.model.StockItem;
import ie.cortexx.service.CustomerService;
import ie.cortexx.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

custom toolbar since we need combo + date fields + buttons.
flowRow() keeps controls in a horizontal row.

content area swaps tables based on selected report type.
each show*() builds a fresh table (simpler than reusing one and swapping cols).

*/

// date range picker + report type, generates different report tables
public class ReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private final CustomerService customerService = new CustomerService();
    private JPanel contentArea;
    private JComboBox<String> reportType;
    private JTextField fromField;
    private JTextField toField;

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
        toolbar.add(UI.buttonRow(generate, UI.button("Export")), BorderLayout.EAST);
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
                default -> UI.swap(contentArea, UI.emptyState("No data"));
            }
        } catch (Exception error) {
            UI.swap(contentArea, UI.emptyState(error.getMessage()));
        }
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
        UI.swap(contentArea, rows.isEmpty() ? UI.emptyState("No stock data") : table.scroll());
    }

    private void showDebt() throws Exception {
        List<DebtRow> rows = reportService.getDebtSummary().stream().map(customer -> new DebtRow(
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
        UI.swap(contentArea, rows.isEmpty() ? UI.emptyState("No debt data") : table.scroll());
    }

    private String money(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String text(String value) {
        return value != null ? value : "";
    }
}
