package ie.cortexx.gui.order;

import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class OrderPanel extends JPanel implements RefreshablePage {
    private final OrderService orderService = new OrderService();

    private record OrderRow(String orderId, String date, String status, int items, String total, String delivered) {}
    private record OnlineOrderRow(String ref, String member, String status, String payment, String total, String address) {}

    public OrderPanel() {
        UI.applyPanelNoPad(this);
        reload();
    }

    @Override
    public void refreshPage() {
        reload();
    }

    private void reload() {
        removeAll();
        add(UI.innerTabs(
            UI.tab("SA Orders", buildSAOrders()),
            UI.tab("PU Online Orders", buildOnlineOrders())
        ));
        revalidate();
        repaint();
    }

    private JPanel buildSAOrders() {
        OrderService.RemoteView<Order> state = loadOrders();
        List<Order> orders = state.rows();
        long delivered = orders.stream().filter(order -> "DELIVERED".equals(order.getOrderStatus().name())).count();
        long pending = orders.stream().filter(order -> !"DELIVERED".equals(order.getOrderStatus().name())).count();
        BigDecimal totalSpent = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        JButton syncButton = UI.button("Refresh Orders");
        syncButton.addActionListener(e -> reload());
        JPanel toolbar = UI.toolbar();
        toolbar.add(UI.buttonRow(syncButton), BorderLayout.EAST);

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

        JComponent content = orders.isEmpty() ? UI.emptyState(emptyOrdersMessage(state)) : table.scroll();
        JPanel header = toolbar;
        if (!state.message().isBlank()) {
            JPanel top = UI.panel();
            top.add(buildStatusBanner(state), BorderLayout.NORTH);
            top.add(toolbar, BorderLayout.SOUTH);
            header = top;
        }

        return UI.pageWithStats(stats, header, content);
    }

    private JComponent buildOnlineOrders() {
        JButton syncButton = UI.button("Refresh Online Orders");
        syncButton.addActionListener(e -> reload());
        JPanel toolbar = UI.toolbar();
        toolbar.add(UI.buttonRow(syncButton), BorderLayout.EAST);

        try {
            List<OnlineOrder> onlineOrders = orderService.findOnlineOrders();
            if (onlineOrders.isEmpty()) {
                return UI.toolbarAndTable(toolbar, UI.emptyState("No online orders yet"));
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
            return UI.toolbarAndTable(toolbar, table.scroll());
        } catch (Exception error) {
            return UI.toolbarAndTable(toolbar, UI.emptyState(error.getMessage()));
        }
    }

    private OrderService.RemoteView<Order> loadOrders() {
        try {
            return orderService.loadSaOrdersView();
        } catch (Exception error) {
            return new OrderService.RemoteView<>(List.of(), OrderService.RemoteSource.NONE, OrderService.RemoteIssue.UNREACHABLE, error.getMessage());
        }
    }

    private JComponent buildStatusBanner(OrderService.RemoteView<?> view) {
        JPanel banner = UI.paddedPanel(0, 0, 8, 0);
        banner.setOpaque(false);
        String badge = switch (view.source()) {
            case LIVE_SA -> "LIVE SA";
            case LOCAL_CACHE -> "LOCAL CACHE";
            case NONE -> "SA ISSUE";
        };
        banner.add(UI.badge(badge), BorderLayout.WEST);
        banner.add(UI.monoLabel(view.message(), 11f, view.isLive() ? UI.TEXT_DIM : UI.ORANGE), BorderLayout.CENTER);
        return banner;
    }

    private String emptyOrdersMessage(OrderService.RemoteView<Order> view) {
        if (!view.message().isBlank()) {
            return view.message();
        }
        return "No SA orders yet";
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
