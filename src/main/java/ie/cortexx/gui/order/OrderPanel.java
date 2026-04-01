package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*
inner tabs split SA orders from PU online orders.
PU tab is just emptyState for now.

SA tab uses pageWithStats(): stat cards NORTH, toolbar+table CENTER.
empty toolbar for now, can add search/filters later.

placeholder data, swap with OrderDAO.findAll() later.
*/

// order history and status tracking for SA orders
public class OrderPanel extends JPanel {
    private record OrderRow(String orderId, String date, String status, int items, String total, String delivered) {}

    public OrderPanel() {
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("SA Orders", buildSAOrders()),
            UI.tab("PU Online Orders", UI.emptyState("No online orders yet"))
        ));
    }

    private JPanel buildSAOrders() {
        JPanel stats = UI.stats(
            UI.stat("Total Orders", "2", UI.ACCENT, "icons/truck.svg"),
            UI.stat("Delivered", "2", UI.GREEN, "icons/package.svg"),
            UI.stat("Pending", "0", UI.ORANGE, "icons/alert-triangle.svg"),
            UI.stat("Total Spent", "£806.00", UI.PURPLE, "icons/coins.svg")
        );

        var table = UI.table(
            UI.monoCol("SA Order ID", OrderRow::orderId),
            UI.col("Date", OrderRow::date),
            UI.badgeCol("Status", OrderRow::status),
            UI.col("Items", OrderRow::items),
            UI.monoCol("Total", OrderRow::total),
            UI.col("Delivered", OrderRow::delivered)
        ).rows(List.of(
            new OrderRow("ORD-2026-00102", "25/02/2026", "DELIVERED", 5, "£376.00", "26/02/2026"),
            new OrderRow("ORD-2026-00187", "10/03/2026", "DELIVERED", 3, "£430.00", "12/03/2026")
        ));

        return UI.pageWithStats(stats, UI.toolbar(), table.scroll());
    }
}
