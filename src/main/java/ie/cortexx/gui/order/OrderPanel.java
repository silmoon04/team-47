package ie.cortexx.gui.order;

import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Reminder;
import ie.cortexx.service.CustomerService;
import ie.cortexx.service.OrderService;
import ie.cortexx.service.ReminderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderPanel extends JPanel implements RefreshablePage {
    private final OrderService orderService = new OrderService();
    private final ReminderService reminderService = new ReminderService();
    private final CustomerService customerService = new CustomerService();
    private final ie.cortexx.dao.OnlineOrderDAO onlineOrderDAO = new ie.cortexx.dao.OnlineOrderDAO();
    private final JPanel orderDetailPanel = UI.transparentPanel(0);
    private final JPanel reminderDetailPanel = UI.transparentPanel(0);

    private record OrderRow(String orderId, String date, String status, int items, String total, String delivered) {}
    private record OnlineOrderRow(String ref, String member, String status, String payment, String total, String address) {}
    private record ReminderRow(int reminderId, String customer, String type, String amount, String due, String sent, String content) {}
    private record BalanceState(String value, String message) {}

    private static final String[] ONLINE_STATUSES = {"CONFIRMED", "RECEIVED", "PROCESSING", "READY", "DISPATCHED", "DELIVERED", "CANCELLED"};

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
            UI.tab("PU Online Orders", buildOnlineOrders()),
            UI.tab("Reminders", buildReminders())
        ));
        revalidate();
        repaint();
    }

    private JPanel buildSAOrders() {
        OrderService.RemoteView<Order> state = loadOrders();
        List<Order> orders = state.rows();
        BalanceState balance = loadBalance();

        long delivered = orders.stream().filter(order -> "DELIVERED".equals(text(order.getOrderStatus()))).count();
        long pending = orders.stream().filter(order -> !"DELIVERED".equals(text(order.getOrderStatus()))).count();
        BigDecimal totalSpent = orders.stream().map(order -> safe(order.getTotalAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);

        JButton refreshButton = UI.button("Refresh Orders");
        refreshButton.addActionListener(e -> reload());
        JButton balanceButton = UI.button("Refresh Balance");
        balanceButton.addActionListener(e -> reload());

        var table = UI.table(
            UI.monoCol("SA Order ID", OrderRow::orderId),
            UI.col("Date", OrderRow::date),
            UI.badgeCol("Status", OrderRow::status),
            UI.col("Items", OrderRow::items),
            UI.monoCol("Total", OrderRow::total),
            UI.col("Delivered", OrderRow::delivered)
        ).rows(orders.stream().map(order -> new OrderRow(
            order.getSaOrderId() != null ? order.getSaOrderId() : "#" + order.getOrderId(),
            order.getOrderedAt() != null ? order.getOrderedAt().toLocalDate().toString() : "",
            text(order.getOrderStatus()),
            order.getItems() != null ? order.getItems().size() : 0,
            money(order.getTotalAmount()),
            order.getDeliveredAt() != null ? order.getDeliveredAt().toLocalDate().toString() : "-"
        )).toList());
        table.onSelect(selected -> {
            int viewRow = table.table().getSelectedRow();
            if (viewRow < 0) {
                return;
            }
            int modelRow = table.table().convertRowIndexToModel(viewRow);
            showOrderDetail(orders.get(modelRow), null);
        });

        JPanel toolbar = UI.toolbar("Search SA orders...", table.table(), refreshButton, balanceButton);

        JPanel stats = UI.stats(
            UI.stat("Total Orders", String.valueOf(orders.size()), UI.ACCENT, "icons/truck.svg"),
            UI.stat("Delivered", String.valueOf(delivered), UI.GREEN, "icons/package.svg"),
            UI.stat("Pending", String.valueOf(pending), UI.ORANGE, "icons/alert-triangle.svg"),
            UI.stat("Total Spent", money(totalSpent), UI.PURPLE, "icons/coins.svg"),
            UI.stat("SA Balance", balance.value(), balance.message().isBlank() ? UI.ACCENT : UI.ORANGE, "icons/coins.svg")
        );

        if (orders.isEmpty()) {
            UI.swap(orderDetailPanel, UI.emptyState("Select an order to view invoice details"));
        } else {
            showOrderDetail(orders.get(0), null);
            table.table().setRowSelectionInterval(0, 0);
        }

        JPanel split = UI.splitPanel(orders.isEmpty() ? UI.emptyState(emptyOrdersMessage()) : table.scroll(), orderDetailPanel, 430);
        return UI.pageWithStats(stats, toolbar, UI.withFooter(split, buildFooter(state, balance)));
    }

    private JPanel buildOnlineOrders() {
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
                text(order.getStatus()),
                text(order.getPaymentMethod()),
                money(order.getTotalPrice()),
                text(order.getDeliveryAddress())
            )).toList());

            JComboBox<String> statusBox = new JComboBox<>(ONLINE_STATUSES);
            JButton updateBtn = UI.button("Update Status");
            updateBtn.addActionListener(e -> {
                int row = table.table().getSelectedRow();
                if (row < 0) {
                    UI.notifyError(this, "Select an order first");
                    return;
                }

                OnlineOrder selected = onlineOrders.get(row);
                String newStatus = (String) statusBox.getSelectedItem();
                try {
                    onlineOrderDAO.updateStatus(selected.getOnlineOrderId(), newStatus);
                    UI.notifySuccess(this, "Status -> " + newStatus);
                    reload();
                } catch (Exception ex) {
                    UI.notifyError(this, ex.getMessage());
                }
            });

            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            statusPanel.setOpaque(false);
            statusPanel.add(statusBox);
            statusPanel.add(updateBtn);
            toolbar.add(statusPanel, BorderLayout.WEST);

            return UI.toolbarAndTable(toolbar, table.scroll());
        } catch (Exception error) {
            return UI.toolbarAndTable(toolbar, UI.emptyState(error.getMessage()));
        }
    }

    private JPanel buildReminders() {
        List<Reminder> reminders;
        Map<Integer, String> customerNames = new HashMap<>();
        try {
            reminders = reminderService.findDebtorReminders();
            for (Customer customer : customerService.findAll()) {
                customerNames.put(customer.getCustomerId(), customer.getName());
            }
        } catch (Exception error) {
            JPanel panel = UI.panel();
            panel.add(UI.emptyState(error.getMessage()));
            return panel;
        }

        JButton generateButton = UI.primaryButton("Generate Reminders");
        generateButton.addActionListener(e -> {
            try {
                reminderService.generateReminders();
                reload();
                UI.notifySuccess(this, "Reminders generated.");
            } catch (Exception error) {
                UI.notifyError(this, error.getMessage());
            }
        });

        JButton refreshButton = UI.button("Refresh");
        refreshButton.addActionListener(e -> reload());

        JPanel toolbar = UI.toolbar();
        toolbar.add(UI.buttonRow(refreshButton, generateButton), BorderLayout.EAST);

        var table = UI.table(
            UI.col("Customer", ReminderRow::customer),
            UI.badgeCol("Type", ReminderRow::type),
            UI.monoCol("Amount", ReminderRow::amount),
            UI.col("Due", ReminderRow::due),
            UI.col("Sent", ReminderRow::sent),
            UI.col("Content", ReminderRow::content, 300)
        ).rows(reminders.stream().map(reminder -> new ReminderRow(
            reminder.getReminderId(),
            customerNames.getOrDefault(reminder.getCustomerId(), "Customer #" + reminder.getCustomerId()),
            text(reminder.getReminderType()),
            money(reminder.getAmountOwed()),
            reminder.getDueDate() != null ? reminder.getDueDate().toString() : "-",
            reminder.getSentAt() != null ? reminder.getSentAt().toString() : "-",
            reminder.getContent() != null ? reminder.getContent() : "-"
        )).toList()).onSelect(row -> showReminderDetail(row, reminders));

        if (reminders.isEmpty()) {
            UI.swap(reminderDetailPanel, UI.emptyState("No reminders generated yet"));
        } else {
            showReminderDetail(table.rowAtView(0), reminders);
            table.table().setRowSelectionInterval(0, 0);
        }

        JPanel split = UI.splitPanel(reminders.isEmpty() ? UI.emptyState("No reminders generated yet") : table.scroll(), reminderDetailPanel, 430);
        JPanel stats = UI.stats(
            UI.stat("Reminder Count", String.valueOf(reminders.size()), UI.ACCENT, "icons/alert-triangle.svg"),
            UI.stat("Debtor Accounts", String.valueOf(customerNames.size()), UI.ORANGE, "icons/users.svg")
        );
        return UI.pageWithStats(stats, toolbar, split);
    }

    private JPanel buildFooter(OrderService.RemoteView<Order> state, BalanceState balance) {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.add(UI.detailLine("SA balance", balance.value()));
        if (!balance.message().isBlank()) {
            footer.add(Box.createVerticalStrut(6));
            footer.add(UI.statusBanner("SA BALANCE", balance.message(), UI.ORANGE));
        }
        JComponent banner = statusBanner(state);
        if (banner != null) {
            footer.add(Box.createVerticalStrut(6));
            footer.add(banner);
        }
        return footer;
    }

    private OrderService.RemoteView<Order> loadOrders() {
        try {
            return orderService.loadSaOrdersView();
        } catch (Exception error) {
            return new OrderService.RemoteView<>(List.of(), OrderService.RemoteSource.NONE, OrderService.RemoteIssue.UNREACHABLE, error.getMessage());
        }
    }

    private BalanceState loadBalance() {
        try {
            return new BalanceState(money(orderService.getSaOutstandingBalance()), "");
        } catch (Exception error) {
            return new BalanceState("Unavailable", error.getMessage());
        }
    }

    private void showOrderDetail(Order order, String liveStatusOverride) {
        if (order == null) {
            UI.swap(orderDetailPanel, UI.emptyState("Select an order to view invoice details"));
            return;
        }

        String liveStatus = liveStatusOverride != null ? liveStatusOverride : text(order.getOrderStatus());
        JButton refreshStatus = UI.button("Refresh Progress");
        refreshStatus.setEnabled(order.getSaOrderId() != null && !order.getSaOrderId().isBlank());
        refreshStatus.addActionListener(e -> {
            try {
                var status = orderService.getSaOrderStatus(order.getSaOrderId());
                showOrderDetail(order, status != null ? status.name() : "UNKNOWN");
            } catch (Exception error) {
                UI.notifyError(this, error.getMessage());
            }
        });

        JPanel card = UI.formCard();
        card.add(UI.fullWidth(UI.sectionLabel("SA ORDER INVOICE")));
        card.add(UI.gap(10));
        card.add(UI.fullWidth(UI.detailLine("ORDER", order.getSaOrderId() != null ? order.getSaOrderId() : "#" + order.getOrderId())));
        card.add(UI.fullWidth(UI.detailLine("LOCAL STATUS", text(order.getOrderStatus()))));
        card.add(UI.fullWidth(UI.detailLine("LIVE PROGRESS", liveStatus)));
        card.add(UI.fullWidth(UI.detailLine("ORDERED", order.getOrderedAt() != null ? order.getOrderedAt().toLocalDate().toString() : "-")));
        card.add(UI.fullWidth(UI.detailLine("DELIVERED", order.getDeliveredAt() != null ? order.getDeliveredAt().toLocalDate().toString() : "-")));
        card.add(UI.fullWidth(UI.detailLine("TOTAL", money(order.getTotalAmount()))));
        card.add(UI.gap(8));
        card.add(UI.fullWidth(refreshStatus));
        card.add(UI.gap(12));
        card.add(UI.fullWidth(UI.sectionLabel("ITEMS")));
        card.add(UI.gap(8));

        List<OrderItem> items = order.getItems() != null ? order.getItems() : List.of();
        if (items.isEmpty()) {
            card.add(UI.fullWidth(UI.detailLine("Items", "No item rows were loaded")));
        } else {
            for (OrderItem item : items) {
                BigDecimal unitPrice = safe(item.getUnitPrice());
                String value = item.getQuantity() + " x " + money(unitPrice) + " = "
                    + money(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                card.add(UI.fullWidth(UI.detailLine("Product " + item.getProductId(), value, 180, false)));
            }
        }

        UI.swap(orderDetailPanel, card);
    }

    private void showReminderDetail(ReminderRow row, List<Reminder> reminders) {
        if (row == null) {
            UI.swap(reminderDetailPanel, UI.emptyState("Select a reminder to inspect it"));
            return;
        }

        JButton markSent = UI.button("Mark Sent");
        markSent.setEnabled("-".equals(row.sent()));
        markSent.addActionListener(e -> {
            try {
                reminderService.markSent(row.reminderId());
                reload();
                UI.notifySuccess(this, "Reminder marked as sent.");
            } catch (Exception error) {
                UI.notifyError(this, error.getMessage());
            }
        });

        JPanel card = UI.formCard();
        card.add(UI.fullWidth(UI.sectionLabel("REMINDER")));
        card.add(UI.gap(10));
        card.add(UI.fullWidth(UI.detailLine("CUSTOMER", row.customer())));
        card.add(UI.fullWidth(UI.detailLine("TYPE", row.type())));
        card.add(UI.fullWidth(UI.detailLine("AMOUNT OWED", row.amount())));
        card.add(UI.fullWidth(UI.detailLine("DUE DATE", row.due())));
        card.add(UI.fullWidth(UI.detailLine("SENT AT", row.sent())));
        card.add(UI.gap(12));
        card.add(UI.fullWidth(UI.detailLine("CONTENT", row.content(), true)));
        card.add(UI.gap(12));
        card.add(UI.fullWidth(markSent));
        card.add(UI.gap(8));
        card.add(UI.fullWidth(UI.detailLine("Total reminders", String.valueOf(reminders.size()))));
        UI.swap(reminderDetailPanel, card);
    }

    private JComponent statusBanner(OrderService.RemoteView<?> view) {
        return view.message().isBlank() ? null : buildStatusBanner(view);
    }

    private JComponent buildStatusBanner(OrderService.RemoteView<?> view) {
        Color tone = switch (view.source()) {
            case LIVE_SA -> UI.ACCENT;
            case LOCAL_CACHE -> UI.ORANGE;
            case NONE -> UI.RED;
        };
        return UI.fullWidth(UI.statusBanner(view.source().label(), view.message(), tone));
    }

    private String emptyOrdersMessage() {
        return "No SA orders yet";
    }

    private String money(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String memberLabel(String value) {
        return value != null && !value.isBlank() ? value : "Guest checkout";
    }

    private String text(Object value) {
        return value != null ? value.toString() : "";
    }
}
