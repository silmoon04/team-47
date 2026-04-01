package ie.cortexx.gui.stock;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*
basically
using UI.applyPanel() to set dark bg + padding + BorderLayout on every panel.
this is the standard setup from UI.java so all panels look consistent.

UI.table() returns a StyledTable record with .table(), .model(), .scroll().
- .monoColumn(i) sets monospace font on col i (good for ids, prices, numbers)
- .badgeColumn(i) renders coloured status pills using the BADGES map in UI.java
- .scroll() gives us the JScrollPane to drop into the layout

UI.toolbar() creates a bar with search on the left and buttons on the right.
the search field uses RowFilter internally, filters all columns case insensitively.

UI.pageWithStats() stacks stat cards in NORTH and toolbar+table in CENTER.
saves us from manually nesting panels for this common layout.

placeholder data is hardcoded for now, swap with StockDAO.findAll() loop later.
the table, search, badges all stay the same, only the data loading changes.
*/

// shows all products and their quantities in a JTable
public class StockPanel extends JPanel {
    private record StockRow(
        String saId,
        String name,
        String type,
        String cost,
        String retail,
        int qty,
        int reorder,
        String status,
        String value
    ) {}

    public StockPanel() {
        UI.applyPanel(this);

        JPanel stats = UI.stats(
            UI.stat("Total Products", "14", UI.ACCENT, "icons/package.svg"),
            UI.stat("Total Units", "786", UI.GREEN, "icons/boxes.svg"),
            UI.stat("Stock Value", "£1,547.40", UI.PURPLE, "icons/coins.svg"),
            UI.stat("Low Stock", "2", UI.RED, "icons/alert-triangle.svg")
        );

        var table = UI.table(
            UI.monoCol("SA ID", StockRow::saId),
            UI.col("Name", StockRow::name),
            UI.col("Type", StockRow::type),
            UI.monoCol("Cost", StockRow::cost),
            UI.monoCol("Retail", StockRow::retail),
            UI.monoCol("Qty", StockRow::qty),
            UI.monoCol("Reorder", StockRow::reorder),
            UI.badgeCol("Status", StockRow::status),
            UI.monoCol("Value", StockRow::value)
        ).rows(List.of(
            new StockRow("100 00001", "Paracetamol", "Box", "£0.10", "£0.20", 121, 10, "IN_STOCK", "£24.20"),
            new StockRow("100 00007", "Lipitor TB 20mg", "Box", "£15.50", "£31.00", 10, 10, "LOW_STOCK", "£310.00"),
            new StockRow("200 00005", "Rhynol", "Bottle", "£2.50", "£5.00", 14, 15, "LOW_STOCK", "£70.00")
        ));

        JPanel toolbar = UI.toolbar("Search stock...", table.table(), "+ Add Stock");
        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
    }
}
