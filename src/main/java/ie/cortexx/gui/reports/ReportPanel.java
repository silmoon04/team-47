package ie.cortexx.gui.reports;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

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

        // content area: swaps tables based on selected report
        contentArea = UI.transparentPanel(0);
        add(contentArea, BorderLayout.CENTER);
        showTurnover();

        reportType.addActionListener(e -> {
            contentArea.removeAll();
            switch ((String) reportType.getSelectedItem()) {
                case "Turnover" -> showTurnover();
                case "Stock" -> showStock();
                case "Debt Summary" -> showDebt();
                default -> contentArea.add(UI.emptyState("No data"));
            }
            contentArea.revalidate();
            contentArea.repaint();
        });
    }

    private void showTurnover() {
        var t = UI.table("Sale #", "Customer", "Date", "Payment", "Total");
        t.monoColumn(0).badgeColumn(3).monoColumn(4);
        // TODO: swap with SaleDAO.findByDateRange(from, to)
        t.model().addRow(new Object[]{"#0001", "Ms Eva Bauyer", "2026-03-01", "ON_CREDIT", "£63.60"});
        t.model().addRow(new Object[]{"#0002", "Walk-in", "2026-03-03", "CASH", "£4.60"});
        contentArea.add(t.scroll());
    }

    private void showStock() {
        var t = UI.table("Product", "Qty", "Cost", "Retail", "Value", "Status");
        t.monoColumn(1).monoColumn(2).monoColumn(3).monoColumn(4).badgeColumn(5);
        // TODO: swap with StockDAO.findAll()
        t.model().addRow(new Object[]{"Paracetamol", 121, "£0.10", "£0.20", "£24.20", "IN_STOCK"});
        contentArea.add(t.scroll());
    }

    private void showDebt() {
        var t = UI.table("Customer", "Account", "Status", "Balance", "Limit");
        t.monoColumn(1).badgeColumn(2).monoColumn(3).monoColumn(4);
        // TODO: swap with CustomerDAO.findDebtors()
        t.model().addRow(new Object[]{"Ms Eva Bauyer", "ACC0001", "NORMAL", "£0.00", "£500.00"});
        contentArea.add(t.scroll());
    }
}
