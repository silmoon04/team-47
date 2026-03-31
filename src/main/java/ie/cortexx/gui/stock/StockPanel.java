package ie.cortexx.gui.stock;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

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
    public StockPanel() {
        // dark bg, 20px padding, BorderLayout (standard for every panel)
        UI.applyPanel(this);

        // stat cards across the top
        // statsRow(4) creates a horizontal grid with 4 columns
        // statCard() makes a card with coloured icon, label, and big number
        // TODO: replace hardcoded values with real counts from DAO
        JPanel stats = UI.statsRow(4);
        stats.add(UI.statCard("Total Products", "14", UI.ACCENT));
        stats.add(UI.statCard("Total Units", "786", UI.GREEN));
        stats.add(UI.statCard("Stock Value", "£1,547.40", UI.PURPLE));
        stats.add(UI.statCard("Low Stock", "2", UI.RED));

        // table
        // 9 cols: mono on numbers/prices, badge on status
        var t = UI.table("SA ID", "Name", "Type", "Cost",
            "Retail", "Qty", "Reorder", "Status", "Value");
        t.monoColumn(0).monoColumn(3).monoColumn(4)
            .monoColumn(5).monoColumn(6).badgeColumn(7).monoColumn(8);

        // toolbar
        // search left, "+ Add Stock" btn right
        // search filters the table as you type (RowFilter under the hood)
        JPanel toolbar = UI.toolbar("Search stock...", t.table(), "+ Add Stock");

        // pageWithStats assembles: stats NORTH, toolbar+table CENTER
        add(UI.pageWithStats(stats, toolbar, t.scroll()), BorderLayout.CENTER);

        // placeholder data
        // TODO: swap with StockDAO.findAll() loop
        // retail = costPrice * 2 (100% markup), status based on qty vs reorder lvl
        t.model().addRow(new Object[]{
            "100 00001", "Paracetamol", "Box", "£0.10", "£0.20", 121, 10, "IN_STOCK", "£24.20"});
        t.model().addRow(new Object[]{
            "100 00007", "Lipitor TB 20mg", "Box", "£15.50", "£31.00", 10, 10, "LOW_STOCK", "£310.00"});
        t.model().addRow(new Object[]{
            "200 00005", "Rhynol", "Bottle", "£2.50", "£5.00", 14, 15, "LOW_STOCK", "£70.00"});
    }
}
