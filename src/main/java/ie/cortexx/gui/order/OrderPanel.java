package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/*
inner tabs split SA orders from PU online orders.
PU tab is just emptyState for now.

SA tab uses pageWithStats(): stat cards NORTH, toolbar+table CENTER.
empty toolbar for now, can add search/filters later.

*/

public class OrderPanel extends JPanel {
    private final OrderService orderService = new OrderService();

    private record OrderRow(String orderId, String date, String status, int items, String total, String delivered) {}
    private record OnlineOrderRow(String ref, String member, String status, String payment, String total, String address) {}

    public OrderPanel() {
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("SA Orders", buildSAOrders()),
            UI.tab("PU Online Orders", buildOnlineOrders())
        ));
    }

    private JPanel buildSAOrders() {
        List<Order> orders = loadOrders();
        long delivered = orders.stream().filter(order -> "DELIVERED".equals(order.getOrderStatus().name())).count();
        long pending = orders.stream().filter(order -> !"DELIVERED".equals(order.getOrderStatus().name())).count();
        BigDecimal totalSpent = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        JPanel stats = UI.stats(
            UI.stat("Total Orders", String.valueOf(orders.size()), UI.ACCENT, "icons/truck.svg"),
            UI.stat("Delivered", String.valueOf(delivered), UI.GREEN, "icons/package.svg"),
            UI.stat("Pending", String.valueOf(pending), UI.ORANGE, "icons/alert-triangle.svg"),
            UI.stat("Total Spent", money(totalSpent), UI.PURPLE, "icons/coins.svg")
        );

        var table = UI.table(
            UI.monoCol("SA Order ID", OrderRow::orderId),
            UI.col("Date", OrderRow::date),
            UI.badgeCol("Status", OrderRow::status),
            UI.col("Items", OrderRow::items),
            UI.monoCol("Total", OrderRow::total),
            UI.col("Delivered", OrderRow::delivered)
        ).rows(orders.stream().map(order -> new OrderRow(
            order.getSaOrderId() != null ? order.getSaOrderId() : String.valueOf(order.getOrderId()),
            order.getOrderedAt() != null ? order.getOrderedAt().toLocalDate().toString() : "",
            order.getOrderStatus().name(),
            order.getItems() != null ? order.getItems().size() : 0,
            money(order.getTotalAmount()),
            order.getDeliveredAt() != null ? order.getDeliveredAt().toLocalDate().toString() : "-"
        )).toList());

        return UI.pageWithStats(stats, UI.toolbar(), orders.isEmpty() ? UI.emptyState("No orders yet") : table.scroll());
    }

    private JComponent buildOnlineOrders() {
        try {
            List<OnlineOrder> onlineOrders = orderService.findOnlineOrders();
            if (onlineOrders.isEmpty()) {
                return UI.emptyState("No online orders yet");
            }

            var table = UI.table(
                UI.monoCol("PU Ref", OnlineOrderRow::ref),
                UI.col("Member", OnlineOrderRow::member),
                UI.badgeCol("Status", OnlineOrderRow::status),
                UI.col("Payment", OnlineOrderRow::payment),
                UI.monoCol("Total", OnlineOrderRow::total),
                UI.col("Delivery", OnlineOrderRow::address)
            ).rows(onlineOrders.stream().map(order -> new OnlineOrderRow(
                order.getOrderReference(),
                memberLabel(order.getMemberId()),
                order.getStatus(),
                text(order.getPaymentMethod()),
                money(order.getTotalPrice()),
                text(order.getDeliveryAddress())
            )).toList());
            return table.scroll();
        } catch (Exception error) {
            return UI.emptyState("No online orders yet");
        }
    }

    private List<Order> loadOrders() {
        try {
            return orderService.findSaOrders();
        } catch (Exception error) {
            return List.of();
        }
    }

    private String money(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String memberLabel(String value) {
        return value != null && !value.isBlank() ? value : "Guest checkout";
    }

    private String text(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }
}
