package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

/*
inner tabs split SA orders from PU online orders.
PU tab is just emptyState for now.

SA tab uses pageWithStats(): stat cards NORTH, toolbar+table CENTER.
empty toolbar for now, can add search/filters later.

placeholder data, swap with OrderDAO.findAll() later.
*/

// order history and status tracking for SA orders
public class OrderPanel extends JPanel {
    public OrderPanel() {
        UI.applyPanelNoPad(this);
        var tabs = UI.innerTabs();
        tabs.addTab("SA Orders", buildSAOrders());
        tabs.addTab("PU Online Orders", UI.emptyState("No online orders yet"));
        add(tabs);
    }

    private JPanel buildSAOrders() {
        // TODO: replace with real counts from OrderDAO
        JPanel stats = UI.statsRow(4);
        stats.add(UI.statCard("Total Orders", "2", UI.ACCENT));
        stats.add(UI.statCard("Delivered", "2", UI.GREEN));
        stats.add(UI.statCard("Pending", "0", UI.ORANGE));
        stats.add(UI.statCard("Total Spent", "£806.00", UI.PURPLE));

        var t = UI.table("SA Order ID", "Date", "Status", "Items", "Total", "Delivered");
        t.monoColumn(0).badgeColumn(2).monoColumn(4);
        // TODO: swap with OrderDAO.findAll() loop
        t.model().addRow(new Object[]{
            "ORD-2026-00102", "25/02/2026", "DELIVERED", 5, "£376.00", "26/02/2026"});
        t.model().addRow(new Object[]{
            "ORD-2026-00187", "10/03/2026", "DELIVERED", 3, "£430.00", "12/03/2026"});

        return UI.pageWithStats(stats, UI.toolbar(), t.scroll());
    }
}
