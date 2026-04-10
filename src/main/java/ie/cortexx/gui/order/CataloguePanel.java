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

        var table = UI.table(
            UI.monoCol("Item ID", Product::getSaProductId),
            UI.col("Product Name", Product::getName),
            UI.monoCol("Package Cost", product -> money(product.getCostPrice())),
            UI.monoCol("SA Availability", this::availabilityLabel)
        ).rows(currentView.rows());

        JButton syncButton = UI.button("Sync Catalogue");
        syncButton.addActionListener(e -> syncCatalogue());
        JButton placeOrder = UI.primaryButton("Place Order");
        placeOrder.addActionListener(e -> placeOrder(table));
        placeOrder.setEnabled(currentView.isLive() && !currentView.rows().isEmpty());

        var toolbar = UI.toolbar("Search catalogue...", table.table(), syncButton, placeOrder);

        JComponent body = currentView.rows().isEmpty() ? UI.emptyState(emptyMessage()) : table.scroll();
        add(UI.toolbarAndTable(buildHeader(toolbar), body), BorderLayout.CENTER);
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
            JOptionPane.showMessageDialog(this, "Catalogue synced from SA.");
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Sync Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void placeOrder(UI.DataTable<Product> table) {
        int viewRow = table.table().getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        Product product = table.rowAtView(viewRow);
        if (product == null || product.getAvailability() <= 0) {
            JOptionPane.showMessageDialog(this, "No stock is currently available in SA for this item.");
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
            JOptionPane.showMessageDialog(this, "Order placed: " + confirmation.getSaOrderId());
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Order Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildHeader(JPanel toolbar) {
        if (currentView.message().isBlank()) {
            return toolbar;
        }

        JPanel header = UI.panel();
        header.add(buildStatusBanner(currentView), BorderLayout.NORTH);
        header.add(toolbar, BorderLayout.SOUTH);
        return header;
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

    private String emptyMessage() {
        if (!currentView.message().isBlank()) {
            return currentView.message();
        }
        return "No catalogue rows available";
    }

    private String availabilityLabel(Product product) {
        return currentView.isLive() ? product.getAvailability() + " packs" : "cached only";
    }

    private String money(BigDecimal amount) {
        return "£" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
