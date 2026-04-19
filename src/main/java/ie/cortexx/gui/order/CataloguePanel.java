package ie.cortexx.gui.order;

import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.Product;
import ie.cortexx.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class CataloguePanel extends JPanel implements RefreshablePage {
    private final OrderService orderService = new OrderService();
    private OrderService.RemoteView<Product> currentView = new OrderService.RemoteView<>(List.of(), OrderService.RemoteSource.NONE, OrderService.RemoteIssue.NONE, "");
    private BigDecimal saBalance = BigDecimal.ZERO;
    private String balanceMessage = "";

    public CataloguePanel() {
        UI.applyPanel(this);
        reload();
    }

    @Override
    public void refreshPage() {
        reload();
    }

    private void reload() {
        removeAll();
        currentView = loadView();
        loadBalance();

        var table = UI.table(
            UI.monoCol("Item ID", Product::getSaProductId),
            UI.col("Product Name", Product::getName),
            UI.monoCol("Package Cost", product -> money(product.getCostPrice())),
            UI.monoCol("SA Availability", this::availabilityLabel)
        ).rows(currentView.rows());

        JButton syncButton = UI.button("Sync Catalogue");
        syncButton.addActionListener(e -> syncCatalogue());
        JButton balanceButton = UI.button("Refresh Balance");
        balanceButton.addActionListener(e -> reload());
        JButton placeOrder = UI.primaryButton("Place Order");
        placeOrder.addActionListener(e -> placeOrder(table));
        placeOrder.setEnabled(currentView.isLive() && !currentView.rows().isEmpty());

        var toolbar = UI.toolbar("Search catalogue...", table.table(), syncButton, balanceButton, placeOrder);

        JComponent body = currentView.rows().isEmpty() ? UI.emptyState(emptyMessage()) : table.scroll();
        add(UI.pageWithStats(
            UI.stats(
                UI.stat("SA Balance", balanceMessage.isBlank() ? money(saBalance) : "Unavailable", balanceMessage.isBlank() ? UI.ACCENT : UI.ORANGE, "icons/coins.svg"),
                UI.stat("Catalogue Rows", String.valueOf(currentView.rows().size()), UI.GREEN, "icons/book-open.svg")
            ),
            toolbar,
            UI.withFooter(body, buildFooter())
        ), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private OrderService.RemoteView<Product> loadView() {
        try {
            return orderService.loadSaCatalogueView();
        } catch (Exception error) {
            return new OrderService.RemoteView<>(List.of(), OrderService.RemoteSource.NONE, OrderService.RemoteIssue.UNREACHABLE, error.getMessage());
        }
    }

    private void syncCatalogue() {
        try {
            orderService.syncCatalogue();
            reload();
            UI.notifySuccess(this, "Catalogue synced from SA.");
        } catch (Exception error) {
            UI.notifyError(this, error.getMessage());
        }
    }

    private void placeOrder(UI.DataTable<Product> table) {
        int viewRow = table.table().getSelectedRow();
        if (viewRow < 0) {
            UI.notifyInfo(this, "Select a product first.");
            return;
        }

        Product product = table.rowAtView(viewRow);
        if (product == null || product.getAvailability() <= 0) {
            UI.notifyInfo(this, "No stock is currently available in SA for this item.");
            return;
        }

        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, product.getAvailability(), 1));
        JPanel form = new JPanel(new GridLayout(2, 1, 0, 8));
        form.add(new JLabel("Select packs to order"));
        form.add(quantity);

        if (JOptionPane.showConfirmDialog(this, form, "Place SA Order", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            OrderConfirmation confirmation = orderService.placeSaOrder(product.getSaProductId(), ((Number) quantity.getValue()).intValue());
            reload();
            UI.notifySuccess(this, "Order placed: " + confirmation.getSaOrderId());
        } catch (Exception error) {
            UI.notifyError(this, error.getMessage());
        }
    }

    private JComponent statusBanner() {
        return currentView.message().isBlank() ? null : buildStatusBanner(currentView);
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.add(UI.detailLine("SA balance", balanceMessage.isBlank() ? money(saBalance) : "Unavailable"));
        if (!balanceMessage.isBlank()) {
            footer.add(Box.createVerticalStrut(6));
            footer.add(UI.statusBanner("SA BALANCE", balanceMessage, UI.ORANGE));
        }
        JComponent banner = statusBanner();
        if (banner != null) {
            footer.add(Box.createVerticalStrut(6));
            footer.add(banner);
        }
        return footer;
    }

    private void loadBalance() {
        try {
            saBalance = orderService.getSaOutstandingBalance();
            balanceMessage = "";
        } catch (Exception error) {
            saBalance = BigDecimal.ZERO;
            balanceMessage = error.getMessage();
        }
    }

    private JComponent buildStatusBanner(OrderService.RemoteView<?> view) {
        Color tone = switch (view.source()) {
            case LIVE_SA -> UI.ACCENT;
            case LOCAL_CACHE -> UI.ORANGE;
            case NONE -> UI.RED;
        };
        return UI.fullWidth(UI.statusBanner(view.source().label(), view.message(), tone));
    }

    private String emptyMessage() {
        return "No catalogue rows available";
    }

    private String availabilityLabel(Product product) {
        return currentView.isLive() ? product.getAvailability() + " packs" : "cached only";
    }

    private String money(BigDecimal amount) {
        return "£" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
