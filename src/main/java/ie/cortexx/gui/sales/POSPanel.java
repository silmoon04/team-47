package ie.cortexx.gui.sales;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/*
=== why this panel is built like this ===

two views (POS + history) in inner tabs.
POS tab uses UI.splitPanel(left, right, 360) to fix the cart at 360px.

the cart uses a manual DefaultTableModel (not UI.table()) bc we need
to update qty/total dynamically when items are added.

convertRowIndexToModel() is needed bc the table might be filtered/sorted,
so the visual row index wont match the model row index.

placeholder data everywhere, swap with DAO calls later.
*/

// the main POS screen: search products, add to cart, see total
public class POSPanel extends JPanel {
    private DefaultTableModel cartModel;
    private JLabel subtotalLabel, totalLabel;

    public POSPanel() {
        UI.applyPanelNoPad(this);
        var tabs = UI.innerTabs();
        tabs.addTab("Point of Sale", buildPOS());
        tabs.addTab("Sale History", buildHistory());
        add(tabs);
    }

    private JPanel buildPOS() {
        // --- left side: product search + table ---
        var products = UI.table("Name", "Price", "In Stock");
        products.monoColumn(1).monoColumn(2);
        // TODO: swap with StockDAO.findAll() loop
        products.model().addRow(new Object[]{"Paracetamol", "£0.20", 121});
        products.model().addRow(new Object[]{"Aspirin", "£1.00", 201});
        products.model().addRow(new Object[]{"Ospen", "£21.00", 78});

        JPanel left = UI.transparentPanel(8);
        left.add(UI.searchField("Search products...", products.table()), BorderLayout.NORTH);
        left.add(products.scroll(), BorderLayout.CENTER);

        // click product row to add to cart
        products.table().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = products.table().getSelectedRow();
                if (row < 0) return;
                row = products.table().convertRowIndexToModel(row);
                addToCart(
                    (String) products.model().getValueAt(row, 0),
                    (String) products.model().getValueAt(row, 1));
            }
        });

        // --- right side: cart ---
        // cardPanel() gives us card bg + border in one call
        JPanel cart = UI.cardPanel();

        // cart header + customer dropdown
        JPanel header = UI.paddedPanel(12, 16, 12, 16);
        header.add(UI.heading("Current Sale"), BorderLayout.NORTH);
        // TODO: populate from CustomerDAO.findAll()
        header.add(new JComboBox<>(new String[]{
            "Walk-in Customer", "Ms Eva Bauyer (ACC0001)", "Mr Glynne Morrison (ACC0002)"
        }), BorderLayout.SOUTH);
        cart.add(header, BorderLayout.NORTH);

        // cart items table (manual, not UI.table(), bc we update rows dynamically)
        cartModel = new DefaultTableModel(new String[]{"Item", "Qty", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setBackground(UI.BG_CARD);
        cartTable.setForeground(UI.TEXT);
        cartTable.setRowHeight(32);
        cart.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // --- summary + payment buttons ---
        JPanel bottom = UI.formCard();
        subtotalLabel = UI.mono("Subtotal: £0.00");
        totalLabel = UI.mono("Total: £0.00");
        totalLabel.setFont(UI.FONT_MONO_BIG);
        bottom.add(subtotalLabel);
        bottom.add(UI.gap(4));
        bottom.add(totalLabel);
        bottom.add(UI.gap(12));
        // gridRow(3, 8) = 3 cols, 8px gap, transparent
        JPanel payBtns = UI.gridRow(3, 8);
        payBtns.add(UI.button("Cash"));
        payBtns.add(UI.primaryButton("Card"));
        payBtns.add(UI.button("Credit"));
        bottom.add(payBtns);
        cart.add(bottom, BorderLayout.SOUTH);

        JPanel view = UI.panel();
        view.add(UI.splitPanel(left, cart, 360), BorderLayout.CENTER);
        return view;
    }

    // adds item to cart, or increments qty if already there
    private void addToCart(String name, String price) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(name)) {
                int qty = (int) cartModel.getValueAt(i, 1) + 1;
                double unit = Double.parseDouble(price.replace("£", ""));
                cartModel.setValueAt(qty, i, 1);
                cartModel.setValueAt("£" + String.format("%.2f", unit * qty), i, 2);
                updateTotals();
                return;
            }
        }
        cartModel.addRow(new Object[]{name, 1, price});
        updateTotals();
    }

    // recalculates subtotal/total from all cart rows
    private void updateTotals() {
        double sub = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++)
            sub += Double.parseDouble(cartModel.getValueAt(i, 2).toString().replace("£", ""));
        subtotalLabel.setText("Subtotal: £" + String.format("%.2f", sub));
        totalLabel.setText("Total: £" + String.format("%.2f", sub));
        // TODO: apply discount from selected customer's plan via PriceCalculator
    }

    private JPanel buildHistory() {
        var t = UI.table("Sale #", "Customer", "Date", "Items", "Payment", "Total");
        t.monoColumn(0).monoColumn(5).badgeColumn(4);
        // TODO: swap with SaleDAO.findByDateRange(from, to)
        t.model().addRow(new Object[]{"#0001", "Ms Eva Bauyer", "2026-03-01", 4, "ON_CREDIT", "£63.60"});
        t.model().addRow(new Object[]{"#0002", "Walk-in", "2026-03-03", 2, "CASH", "£4.60"});
        JPanel view = UI.panel();
        view.add(t.scroll(), BorderLayout.CENTER);
        return view;
    }
}
