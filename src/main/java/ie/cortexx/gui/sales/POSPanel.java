package ie.cortexx.gui.sales;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/*

this is the most complex panel bc it has two views (POS + history) in inner tabs.
UI.innerTabs() gives us a JTabbedPane with the underline tab style from UI.init().

POS tab uses UI.splitPanel(left, right, 360) to fix the cart at 360px on the right.
left side fills remaining space with the product table + search.

the cart is a manual JTable (not UI.table()) bc we need to edit rows dynamically
when adding items. DefaultTableModel lets us update qty/total on the fly.

clicking a product row adds it to the cart (or increments qty if already there).
convertRowIndexToModel() is needed bc the table might be filtered/sorted,
so the visual row index wont match the model row index.

history tab is just a simple table with badge on payment method.

placeholder data everywhere, swap with DAO calls later.
*/

// the main POS screen: search products, add to cart, see total
public class POSPanel extends JPanel {
    private DefaultTableModel cartModel;
    private JLabel subtotalLabel, totalLabel;

    public POSPanel() {
        setLayout(new BorderLayout());
        setBackground(UI.BG);
        // inner tabs split POS view from sale history
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

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(UI.searchField("Search products...", products.table()), BorderLayout.NORTH);
        left.add(products.scroll(), BorderLayout.CENTER);

        // click product row to add to cart
        // convertRowIndexToModel() needed bc table might be sorted/filtered
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

        // --- right side: cart panel ---
        JPanel cart = new JPanel(new BorderLayout());
        cart.setBackground(UI.BG_CARD);
        cart.setBorder(BorderFactory.createLineBorder(UI.BORDER));

        // cart header + customer dropdown (determines discount tier)
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.add(UI.heading("Current Sale"), BorderLayout.NORTH);
        // TODO: populate from CustomerDAO.findAll()
        header.add(new JComboBox<>(new String[]{
            "Walk-in Customer", "Ms Eva Bauyer (ACC0001)", "Mr Glynne Morrison (ACC0002)"
        }), BorderLayout.SOUTH);
        cart.add(header, BorderLayout.NORTH);

        // cart items table (manual DefaultTableModel, not UI.table())
        // we need direct control to update qty/total when items are added
        cartModel = new DefaultTableModel(new String[]{"Item", "Qty", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setBackground(UI.BG_CARD);
        cartTable.setForeground(UI.TEXT);
        cartTable.setRowHeight(32);
        cart.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // --- summary + payment buttons at the bottom ---
        JPanel bottom = UI.formCard();
        subtotalLabel = UI.mono("Subtotal: £0.00");
        totalLabel = UI.mono("Total: £0.00");
        totalLabel.setFont(UI.FONT_MONO_BIG);
        bottom.add(subtotalLabel);
        bottom.add(UI.gap(4));
        bottom.add(totalLabel);
        bottom.add(UI.gap(12));
        // 3 payment options in a row
        JPanel payBtns = new JPanel(new GridLayout(1, 3, 8, 0));
        payBtns.setOpaque(false);
        payBtns.add(UI.button("Cash"));
        payBtns.add(UI.primaryButton("Card"));
        payBtns.add(UI.button("Credit"));
        bottom.add(payBtns);
        cart.add(bottom, BorderLayout.SOUTH);

        // splitPanel: products left, cart right at 360px fixed width
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
        // simple table showing past sales
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
