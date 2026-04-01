package ie.cortexx.gui.reports;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*

custom toolbar since we need combo + date fields + buttons.
flowRow() keeps controls in a horizontal row.

content area swaps tables based on selected report type.
each show*() builds a fresh table (simpler than reusing one and swapping cols).

placeholder data, swap with DAO calls later.
*/

// date range picker + report type, generates different report tables
public class ReportPanel extends JPanel {
    private JPanel contentArea;

    private record TurnoverRow(String saleId, String customer, String date, String payment, String total) {}
    private record StockRow(String product, int qty, String cost, String retail, String value, String status) {}
    private record DebtRow(String customer, String account, String status, String balance, String limit) {}

    public ReportPanel() {
        UI.applyPanel(this);

        // --- toolbar: report picker + date range + buttons ---
        JPanel toolbar = UI.toolbar();
        // flowRow(8) = FlowLayout LEFT, 8px gap, transparent
        JPanel left = UI.flowRow(8);
        var reportType = new JComboBox<>(new String[]{
            "Turnover", "Stock", "Debt Summary", "Late Customers"});
        left.add(UI.field("Report Type", reportType));
        left.add(UI.field("From", new JTextField("2026-03-01", 10)));
        left.add(UI.field("To", new JTextField("2026-03-31", 10)));
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(UI.buttonRow(UI.primaryButton("Generate"), UI.button("Export")), BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        contentArea = UI.transparentPanel(0);
        add(contentArea, BorderLayout.CENTER);
        showTurnover();

        reportType.addActionListener(e -> {
            switch ((String) reportType.getSelectedItem()) {
                case "Turnover" -> showTurnover();
                case "Stock" -> showStock();
                case "Debt Summary" -> showDebt();
                default -> UI.swap(contentArea, UI.emptyState("No data"));
            }
        });
    }

    private void showTurnover() {
        var table = UI.table(
            UI.monoCol("Sale #", TurnoverRow::saleId),
            UI.col("Customer", TurnoverRow::customer),
            UI.col("Date", TurnoverRow::date),
            UI.badgeCol("Payment", TurnoverRow::payment),
            UI.monoCol("Total", TurnoverRow::total)
        ).rows(List.of(
            new TurnoverRow("#0001", "Ms Eva Bauyer", "2026-03-01", "ON_CREDIT", "£63.60"),
            new TurnoverRow("#0002", "Walk-in", "2026-03-03", "CASH", "£4.60")
        ));
        UI.swap(contentArea, table.scroll());
    }

    private void showStock() {
        var table = UI.table(
            UI.col("Product", StockRow::product),
            UI.monoCol("Qty", StockRow::qty),
            UI.monoCol("Cost", StockRow::cost),
            UI.monoCol("Retail", StockRow::retail),
            UI.monoCol("Value", StockRow::value),
            UI.badgeCol("Status", StockRow::status)
        ).rows(List.of(
            new StockRow("Paracetamol", 121, "£0.10", "£0.20", "£24.20", "IN_STOCK")
        ));
        UI.swap(contentArea, table.scroll());
    }

    private void showDebt() {
        var table = UI.table(
            UI.col("Customer", DebtRow::customer),
            UI.monoCol("Account", DebtRow::account),
            UI.badgeCol("Status", DebtRow::status),
            UI.monoCol("Balance", DebtRow::balance),
            UI.monoCol("Limit", DebtRow::limit)
        ).rows(List.of(
            new DebtRow("Ms Eva Bauyer", "ACC0001", "NORMAL", "£0.00", "£500.00")
        ));
        UI.swap(contentArea, table.scroll());
    }
}
