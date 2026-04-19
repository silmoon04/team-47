package ie.cortexx.gui.stock;

import ie.cortexx.dao.ProductDAO;
import ie.cortexx.dao.StockDAO;
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
    private final StockDAO stockDAO = new StockDAO();
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

        JButton newRecordButton = UI.primaryButton("+ New Stock Record");
        newRecordButton.addActionListener(e -> createStockRecord());
        JButton addStockButton = UI.button("Add Stock");
        addStockButton.addActionListener(e -> addStock(table.table()));
        JButton modifyQtyButton = UI.button("Set Quantity");
        modifyQtyButton.addActionListener(e -> modifyQuantity(table.table()));
        JButton removeButton = UI.dangerButton("Remove Item");
        removeButton.addActionListener(e -> removeStockItem(table.table()));
        JButton deliveryButton = UI.button("Record SA Delivery");
        deliveryButton.addActionListener(e -> recordSADelivery(table.table()));
        JPanel toolbar = UI.toolbar("Search stock...", table.table(), newRecordButton, addStockButton, modifyQtyButton, removeButton, deliveryButton);
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
        StockItem item = selectedItem(jTable);
        if (item == null) return;
        String qtyText = JOptionPane.showInputDialog(this, "Add quantity for " + item.getProductName(), "10");
        if (qtyText == null || qtyText.isBlank()) return;

        try {
            int quantity = Integer.parseInt(qtyText.trim());
            if (quantity <= 0) { UI.notifyInfo(this, "Enter a positive quantity."); return; }
            stockService.addStock(item.getProductId(), quantity);
            reload();
            UI.notifySuccess(this, "Stock updated.");
        } catch (Exception error) { UI.notifyError(this, error.getMessage()); }
    }

    private void createStockRecord() {
        JTextField saId = new JTextField();
        JTextField name = new JTextField();
        JTextField packageType = new JTextField("Box");
        JTextField unitType = new JTextField("Pack");
        JTextField unitsPerPack = new JTextField("1");
        JTextField costPrice = new JTextField("0.00");
        JTextField markupRate = new JTextField("1.0000");
        JTextField category = new JTextField("General");
        JTextField openingQty = new JTextField("0");
        JTextField reorderLevel = new JTextField("10");

        JPanel form = new JPanel(new GridLayout(10, 2, 8, 8));
        form.add(new JLabel("SA ID"));
        form.add(saId);
        form.add(new JLabel("Product Name"));
        form.add(name);
        form.add(new JLabel("Package Type"));
        form.add(packageType);
        form.add(new JLabel("Unit Type"));
        form.add(unitType);
        form.add(new JLabel("Units Per Pack"));
        form.add(unitsPerPack);
        form.add(new JLabel("Cost Price"));
        form.add(costPrice);
        form.add(new JLabel("Markup Rate"));
        form.add(markupRate);
        form.add(new JLabel("Category"));
        form.add(category);
        form.add(new JLabel("Opening Quantity"));
        form.add(openingQty);
        form.add(new JLabel("Reorder Level"));
        form.add(reorderLevel);

        if (JOptionPane.showConfirmDialog(this, form, "New Stock Record", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
            != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Product product = new Product();
            product.setSaProductId(saId.getText().trim());
            product.setName(name.getText().trim());
            product.setPackageType(packageType.getText().trim());
            product.setUnitType(unitType.getText().trim());
            product.setUnitsPerPack(Integer.parseInt(unitsPerPack.getText().trim()));
            product.setCostPrice(new BigDecimal(costPrice.getText().trim()));
            product.setMarkupRate(new BigDecimal(markupRate.getText().trim()));
            product.setVatRate(BigDecimal.ZERO);
            product.setCategory(category.getText().trim());
            product.setActive(true);

            if (product.getSaProductId().isBlank() || product.getName().isBlank()) {
                UI.notifyInfo(this, "SA ID and product name are required.");
                return;
            }

            productDAO.save(product);

            StockItem item = new StockItem();
            item.setProductId(product.getProductId());
            item.setQuantity(Integer.parseInt(openingQty.getText().trim()));
            item.setReorderLevel(Integer.parseInt(reorderLevel.getText().trim()));
            stockDAO.save(item);

            reload();
            UI.notifySuccess(this, "New stock record created.");
        } catch (Exception error) {
            UI.notifyError(this, error.getMessage());
        }
    }

    private void modifyQuantity(JTable jTable) {
        StockItem item = selectedItem(jTable);
        if (item == null) return;
        String qtyText = JOptionPane.showInputDialog(this, "Set new quantity for " + item.getProductName() + " (current: " + item.getQuantity() + ")", String.valueOf(item.getQuantity()));
        if (qtyText == null || qtyText.isBlank()) return;

        try {
            int newQty = Integer.parseInt(qtyText.trim());
            if (newQty < 0) { UI.notifyInfo(this, "Quantity cannot be negative."); return; }
            int delta = newQty - item.getQuantity();
            new ie.cortexx.dao.StockDAO().updateQuantity(item.getProductId(), delta);
            reload();
            UI.notifySuccess(this, "Quantity set to " + newQty + ".");
        } catch (Exception error) { UI.notifyError(this, error.getMessage()); }
    }

    private void removeStockItem(JTable jTable) {
        StockItem item = selectedItem(jTable);
        if (item == null) return;
        if (item.getQuantity() > 0) {
            if (!UI.confirm(this, "This item has " + item.getQuantity() + " units in stock. Set to zero and deactivate?", "Remove Stock Item")) return;
        } else {
            if (!UI.confirm(this, "Remove " + item.getProductName() + " from active stock?", "Confirm Remove")) return;
        }
        try {
            // set qty to 0 and deactivate product
            new ie.cortexx.dao.StockDAO().updateQuantity(item.getProductId(), -item.getQuantity());
            new ie.cortexx.dao.ProductDAO().deactivate(item.getProductId());
            reload();
            UI.notifySuccess(this, item.getProductName() + " removed from stock.");
        } catch (Exception error) { UI.notifyError(this, error.getMessage()); }
    }

    private void recordSADelivery(JTable jTable) {
        StockItem item = selectedItem(jTable);
        if (item == null) return;
        String qtyText = JOptionPane.showInputDialog(this, "Record delivery from SA for " + item.getProductName() + "\nQuantity received:", "10");
        if (qtyText == null || qtyText.isBlank()) return;

        try {
            int quantity = Integer.parseInt(qtyText.trim());
            if (quantity <= 0) { UI.notifyInfo(this, "Enter a positive quantity."); return; }
            stockService.addStock(item.getProductId(), quantity);
            reload();
            UI.notifySuccess(this, "SA delivery recorded: +" + quantity + " " + item.getProductName() + ".");
        } catch (Exception error) { UI.notifyError(this, error.getMessage()); }
    }

    private StockItem selectedItem(JTable jTable) {
        int viewRow = jTable.getSelectedRow();
        if (viewRow < 0) { UI.notifyInfo(this, "Select a stock row first."); return null; }
        return items.get(jTable.convertRowIndexToModel(viewRow));
    }
}
