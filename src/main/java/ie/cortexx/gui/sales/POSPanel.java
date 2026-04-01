package ie.cortexx.gui.sales;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*
why this panel is built like this

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

    private record ProductRow(String name, String price, int stock) {}
    private record SaleRow(String saleId, String customer, String date, int items, String payment, String total) {}

    public POSPanel() {
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("Point of Sale", buildPOS()),
            UI.tab("Sale History", buildHistory())
        ));
    }

    private JPanel buildPOS() {
        var products = UI.table(
            UI.col("Name", ProductRow::name),
            UI.monoCol("Price", ProductRow::price),
            UI.monoCol("In Stock", ProductRow::stock)
        ).rows(List.of(
            new ProductRow("Paracetamol", "£0.20", 121),
            new ProductRow("Aspirin", "£1.00", 201),
            new ProductRow("Ospen", "£21.00", 78)
        )).onSelect(row -> addToCart(row.name(), row.price()));

        JPanel left = UI.transparentPanel(8);
        left.add(UI.searchField("Search products...", products.table()), BorderLayout.NORTH);
        left.add(products.scroll(), BorderLayout.CENTER);

        JPanel cart = UI.cardPanel();

        JPanel header = UI.paddedPanel(12, 16, 12, 16);
        header.add(UI.heading("Current Sale"), BorderLayout.NORTH);
        header.add(new JComboBox<>(new String[]{
            "Walk-in Customer", "Ms Eva Bauyer (ACC0001)", "Mr Glynne Morrison (ACC0002)"
        }), BorderLayout.SOUTH);
        cart.add(header, BorderLayout.NORTH);

        cartModel = UI.readonlyModel("Item", "Qty", "Total");
        var cartTable = UI.table(cartModel, false);
        cartTable.table().setRowHeight(32);
        cart.add(cartTable.scroll(), BorderLayout.CENTER);

        JPanel bottom = UI.formCard();
        subtotalLabel = UI.mono("Subtotal: £0.00");
        totalLabel = UI.mono("Total: £0.00");
        totalLabel.setFont(UI.FONT_MONO_BIG);
        bottom.add(subtotalLabel);
        bottom.add(UI.gap(4));
        bottom.add(totalLabel);
        bottom.add(UI.gap(12));
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

    private void updateTotals() {
        double sub = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++)
            sub += Double.parseDouble(cartModel.getValueAt(i, 2).toString().replace("£", ""));
        subtotalLabel.setText("Subtotal: £" + String.format("%.2f", sub));
        totalLabel.setText("Total: £" + String.format("%.2f", sub));
        // TODO: apply discount from selected customer's plan via PriceCalculator
    }

    private JPanel buildHistory() {
        var t = UI.table(
            UI.monoCol("Sale #", SaleRow::saleId),
            UI.col("Customer", SaleRow::customer),
            UI.col("Date", SaleRow::date),
            UI.col("Items", SaleRow::items),
            UI.badgeCol("Payment", SaleRow::payment),
            UI.monoCol("Total", SaleRow::total)
        ).rows(List.of(
            new SaleRow("#0001", "Ms Eva Bauyer", "2026-03-01", 4, "ON_CREDIT", "£63.60"),
            new SaleRow("#0002", "Walk-in", "2026-03-03", 2, "CASH", "£4.60")
        ));
        JPanel view = UI.panel();
        view.add(t.scroll(), BorderLayout.CENTER);
        return view;
    }
}
