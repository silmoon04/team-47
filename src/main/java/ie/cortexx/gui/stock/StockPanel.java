package ie.cortexx.gui.stock;

import ie.cortexx.dao.ProductDAO;
import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Product;
import ie.cortexx.model.StockItem;
import ie.cortexx.service.StockService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

*/

// shows all products and their quantities in a JTable
public class StockPanel extends JPanel implements RefreshablePage {
    private final StockService stockService = new StockService();
    private final ProductDAO productDAO = new ProductDAO();
    private final List<StockItem> items = new ArrayList<>();

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
        reload();
    }

    @Override
    public void refreshPage() {
        reload();
    }

    private void reload() {
        removeAll();
        items.clear();
        items.addAll(loadItems());
        Map<Integer, String> packageTypes = loadPackageTypes();

        JPanel stats = buildStats(items);

        var table = UI.table(
            UI.monoCol("SA ID", StockRow::saId, 110),
            UI.col("Name", StockRow::name, 220),
            UI.col("Type", StockRow::type, 80),
            UI.monoCol("Cost", StockRow::cost, 70),
            UI.monoCol("Retail", StockRow::retail, 70),
            UI.monoCol("Qty", StockRow::qty, 55),
            UI.monoCol("Reorder", StockRow::reorder, 70),
            UI.badgeCol("Status", StockRow::status, 105),
            UI.monoCol("Value", StockRow::value, 90)
        ).rows(items.stream().map(item -> toRow(item, packageTypes.get(item.getProductId()))).toList());

        JButton addStockButton = UI.primaryButton("+ Add Stock");
        addStockButton.addActionListener(e -> addStock(table.table()));
        JPanel toolbar = UI.toolbar("Search stock...", table.table(), addStockButton);
        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private List<StockItem> loadItems() {
        try {
            return stockService.findAll();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private Map<Integer, String> loadPackageTypes() {
        Map<Integer, String> packageTypes = new HashMap<>();
        try {
            for (Product product : productDAO.findAll()) {
                packageTypes.put(product.getProductId(), product.getPackageType());
            }
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        return packageTypes;
    }

    private JPanel buildStats(List<StockItem> loadedItems) {
        int totalUnits = loadedItems.stream().mapToInt(StockItem::getQuantity).sum();
        long lowStock = loadedItems.stream().filter(StockItem::isLowStock).count();
        BigDecimal totalValue = loadedItems.stream()
            .map(this::retailValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UI.stats(
            UI.stat("Total Products", String.valueOf(loadedItems.size()), UI.ACCENT, "icons/package.svg"),
            UI.stat("Total Units", String.valueOf(totalUnits), UI.GREEN, "icons/boxes.svg"),
            UI.stat("Stock Value", money(totalValue), UI.PURPLE, "icons/coins.svg"),
            UI.stat("Low Stock", String.valueOf(lowStock), UI.RED, "icons/alert-triangle.svg")
        );
    }

    private StockRow toRow(StockItem item, String packageType) {
        BigDecimal retailPrice = retailPrice(item);
        return new StockRow(
            item.getSaProductId(),
            item.getProductName(),
            packageType != null ? packageType : "",
            money(item.getCostPrice()),
            money(retailPrice),
            item.getQuantity(),
            item.getReorderLevel(),
            item.isLowStock() ? "LOW_STOCK" : "IN_STOCK",
            money(retailValue(item))
        );
    }

    private BigDecimal retailPrice(StockItem item) {
        return item.getCostPrice().multiply(BigDecimal.ONE.add(item.getMarkupRate()));
    }

    private BigDecimal retailValue(StockItem item) {
        return retailPrice(item).multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private String money(BigDecimal amount) {
        return "£" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void addStock(JTable jTable) {
        int viewRow = jTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a stock row first.");
            return;
        }

        int modelRow = jTable.convertRowIndexToModel(viewRow);
        StockItem item = items.get(modelRow);
        String qtyText = JOptionPane.showInputDialog(this, "Add quantity for " + item.getProductName(), "10");
        if (qtyText == null || qtyText.isBlank()) {
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyText.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive quantity.");
                return;
            }
            stockService.addStock(item.getProductId(), quantity);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
