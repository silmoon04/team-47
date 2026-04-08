package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.Product;
import ie.cortexx.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
simple toolbar + table layout using UI.toolbarAndTable().
this stacks the toolbar on top and the table below, which is the
standard layout for panels that dont need stat cards.

toolbar has search on the left and two buttons on the right.
UI.toolbar(hint, table, buttons...) accepts varargs JButtons,
so we pass UI.button() and UI.primaryButton() directly.

the catalogue data comes from SAProxyService.getCatalogue() which
calls the SA API.

mono on ID, cost, and availability cols bc theyre numbers/prices.
*/

public class CataloguePanel extends JPanel {
    private final OrderService orderService = new OrderService();
    private final List<CatalogueRow> rows = new ArrayList<>();

    private record CatalogueRow(String itemId, String productName, String packageCost, String availability) {}

    public CataloguePanel() {
        UI.applyPanel(this);
        reload();
    }

    private void reload() {
        removeAll();
        rows.clear();
        rows.addAll(loadRows());

        var table = UI.table(
            UI.monoCol("Item ID", CatalogueRow::itemId),
            UI.col("Product Name", CatalogueRow::productName),
            UI.monoCol("Package Cost", CatalogueRow::packageCost),
            UI.monoCol("SA Availability", CatalogueRow::availability)
        ).rows(rows);

        JButton syncButton = UI.button("Sync Catalogue");
        syncButton.addActionListener(e -> syncCatalogue());
        JButton placeOrder = UI.primaryButton("Place Order");
        placeOrder.addActionListener(e -> placeOrder(table.table()));

        var toolbar = UI.toolbar("Search catalogue...", table.table(), syncButton, placeOrder);

        add(UI.toolbarAndTable(toolbar, table.scroll()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private List<CatalogueRow> loadRows() {
        try {
            return orderService.getSaCatalogue().stream().map(product -> new CatalogueRow(
                product.getSaProductId(),
                product.getName(),
                money(product.getCostPrice()),
                product.getAvailability() + " packs"
            )).toList();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
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

    private void placeOrder(JTable jTable) {
        int viewRow = jTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int modelRow = jTable.convertRowIndexToModel(viewRow);
        CatalogueRow row = rows.get(modelRow);
        String qtyText = JOptionPane.showInputDialog(this, "Order quantity", "1");
        if (qtyText == null || qtyText.isBlank()) {
            return;
        }

        try {
            OrderConfirmation confirmation = orderService.placeSaOrder(row.itemId(), Integer.parseInt(qtyText.trim()));
            JOptionPane.showMessageDialog(this, "Order placed: " + confirmation.getSaOrderId());
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Order Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String money(BigDecimal amount) {
        return "£" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
