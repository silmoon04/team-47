package ie.cortexx.gui.sales;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// POS screen with a product list on the left and a custom current-sale cart on the right.
public class POSPanel extends JPanel {
    private final SaleCart cart = new SaleCart();
    private final JPanel cartItemsPanel = new JPanel();
    private final JLabel itemCountLabel = UI.countBadge("0 ITEMS");
    private final JComboBox<CustomerChoice> customerBox = new JComboBox<>(new CustomerChoice[]{
        new CustomerChoice("Walk-in Customer", 0.00, false),
        new CustomerChoice("Ms Eva Bauyer (ACC0001)", 0.03, true),
        new CustomerChoice("Mr Glynne Morrison (ACC0002)", 0.00, true)
    });
    private final JLabel subtotalValue = UI.monoLabel("£0.00", 12f, UI.TEXT);
    private final JLabel discountLabel = UI.monoLabel("Discount (0%)", 12f, UI.TEXT_DIM);
    private final JLabel discountValue = UI.monoLabel("£0.00", 12f, UI.TEXT);
    private final JPanel discountRow = new JPanel(new BorderLayout());
    private final JLabel vatValue = UI.monoLabel("£0.00", 12f, UI.TEXT);
    private final JLabel totalValue = UI.monoLabelBold("£0.00", 14f, UI.TEXT);
    private final JButton cashButton = UI.iconButton("Cash", "icons/banknote.svg", false);
    private final JButton cardButton = UI.iconButton("Card", "icons/credit-card.svg", true);
    private final JButton creditButton = UI.iconButton("Credit", "icons/coins.svg", false);

    private record ProductRow(String name, String price, int stock) {}
    private record SaleRow(String saleId, String customer, String date, int items, String payment, String total) {}
    private record CustomerChoice(String label, double discountRate, boolean creditEnabled) {
        @Override public String toString() { return label; }
    }

    public POSPanel() {
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("Point of Sale", buildPOS()),
            UI.tab("Sale History", buildHistory())
        ));
    }

    private JPanel buildPOS() {
        var products = UI.table(
            UI.col("Name", ProductRow::name, 240),
            UI.col("Price", ProductRow::price, 90),
            UI.col("In Stock", ProductRow::stock, 80)
        ).rows(List.of(
            new ProductRow("Paracetamol", "£0.20", 121),
            new ProductRow("Aspirin", "£1.00", 201),
            new ProductRow("Iodine tincture", "£0.60", 35),
            new ProductRow("Ospen", "£21.00", 78),
            new ProductRow("Amopen", "£30.00", 90),
            new ProductRow("Claritin CR, 60g", "£39.00", 21)
        )).onSelect(row -> addToCart(row.name(), row.price()));
        products.table().setDefaultRenderer(Object.class, UI.plainTableRenderer());
        products.table().setFont(UI.FONT);
        products.table().setRowHeight(34);

        JPanel left = UI.transparentPanel(8);
        left.add(UI.searchField("Search products...", products.table()), BorderLayout.NORTH);
        left.add(products.scroll(), BorderLayout.CENTER);

        JPanel cart = UI.cardPanel();
        cart.add(buildCartHeader(), BorderLayout.NORTH);
        cart.add(buildCartBody(), BorderLayout.CENTER);
        cart.add(buildCartFooter(), BorderLayout.SOUTH);

        JPanel view = UI.panel();
        view.add(UI.splitPanel(left, cart, 392), BorderLayout.CENTER);
        refreshCart();
        return view;
    }

    private JComponent buildCartHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = UI.monoLabelBold("Current Sale", 17f, UI.TEXT);
        top.add(title, BorderLayout.WEST);
        top.add(itemCountLabel, BorderLayout.EAST);

        customerBox.setFont(UI.FONT);
        customerBox.setBackground(UI.BG_CARD);
        customerBox.setForeground(UI.TEXT);
        customerBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        customerBox.addActionListener(e -> {
            updateTotals();
            updatePaymentState();
        });

        header.add(top, BorderLayout.NORTH);
        header.add(customerBox, BorderLayout.SOUTH);
        return header;
    }

    private JComponent buildCartBody() {
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(UI.BG_CARD);

        JScrollPane scroll = new JScrollPane(cartItemsPanel);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UI.BORDER));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getViewport().setBackground(UI.BG_CARD);
        return scroll;
    }

    private JComponent buildCartFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JPanel summary = new JPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setOpaque(false);
        summary.setBorder(new EmptyBorder(12, 18, 12, 18));

        summary.add(UI.summaryRow(UI.monoLabel("Subtotal", 12f, UI.TEXT_DIM), subtotalValue));
        discountRow.setOpaque(false);
        discountRow.add(discountLabel, BorderLayout.WEST);
        discountRow.add(discountValue, BorderLayout.EAST);
        summary.add(Box.createVerticalStrut(6));
        summary.add(discountRow);
        summary.add(Box.createVerticalStrut(6));
        summary.add(UI.summaryRow(UI.monoLabel("VAT (0%)", 12f, UI.TEXT_DIM), vatValue));
        summary.add(Box.createVerticalStrut(10));
        summary.add(new JSeparator());
        summary.add(Box.createVerticalStrut(10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLabel = UI.monoLabelBold("Total", 14f, UI.TEXT);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);
        summary.add(totalRow);

        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 18, 16, 18));
        actions.add(cashButton);
        actions.add(cardButton);
        actions.add(creditButton);

        footer.add(summary, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }

    private void addToCart(String name, String price) {
        double unitPrice = Double.parseDouble(price.replace("£", ""));
        cart.addItem(name, unitPrice);
        refreshCart();
    }

    private void updateQty(String name, int delta) {
        cart.updateQuantity(name, delta);
        refreshCart();
    }

    private void removeItem(String name) {
        cart.removeItem(name);
        refreshCart();
    }

    private void refreshCart() {
        cartItemsPanel.removeAll();
        List<SaleCart.Item> items = cart.items();

        if (cart.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(UI.BG_CARD);
            empty.setBorder(new EmptyBorder(24, 20, 24, 20));
            JLabel text = new JLabel("Select a product to start the sale");
            text.setFont(UI.FONT);
            text.setForeground(UI.TEXT_DIM);
            empty.add(text);
            cartItemsPanel.add(empty);
        } else {
            for (int i = 0; i < items.size(); i++) {
                cartItemsPanel.add(cartRow(items.get(i), i < items.size() - 1));
            }
        }

        itemCountLabel.setText(cart.itemCount() + " ITEMS");
        updateTotals();
        updatePaymentState();
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private JPanel cartRow(SaleCart.Item item, boolean divider) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            divider ? BorderFactory.createMatteBorder(0, 0, 1, 0, UI.BORDER) : BorderFactory.createEmptyBorder(),
            new EmptyBorder(10, 16, 10, 16)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel copy = new JPanel();
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.setOpaque(false);

        JLabel name = UI.monoLabelBold(item.name(), 13f, UI.TEXT);
        JLabel each = UI.monoLabel("£" + String.format("%.2f", item.unitPrice()) + " each", 11f, UI.TEXT_DIM);

        copy.add(name);
        copy.add(Box.createVerticalStrut(2));
        copy.add(each);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controls.setOpaque(false);
        controls.add(qtyStepper(item));

        JLabel total = UI.monoLabelBold("£" + String.format("%.2f", item.lineTotal()), 13f, UI.TEXT);
        total.setHorizontalAlignment(SwingConstants.LEFT);
        total.setPreferredSize(new Dimension(74, 28));
        total.setMinimumSize(new Dimension(74, 28));
        controls.add(total);

        JButton remove = UI.squareButton("\u00D7");
        remove.addActionListener(e -> removeItem(item.name()));
        controls.add(remove);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 1;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(0, 0, 0, 10);
        row.add(copy, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.anchor = GridBagConstraints.WEST;
        right.fill = GridBagConstraints.NONE;
        row.add(controls, right);
        return row;
    }

    private JComponent qtyStepper(SaleCart.Item item) {
        JPanel stepper = new JPanel(new GridLayout(1, 3, 0, 0));
        stepper.setBackground(UI.BG_CARD);
        stepper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI.BORDER, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        stepper.setPreferredSize(new Dimension(98, 30));
        stepper.setMaximumSize(new Dimension(98, 30));

        JButton minus = UI.stepperButton("-");
        minus.addActionListener(e -> updateQty(item.name(), -1));
        JButton plus = UI.stepperButton("+");
        plus.addActionListener(e -> updateQty(item.name(), 1));

        JLabel qty = UI.monoLabelBold(String.valueOf(item.quantity()), 12f, UI.TEXT);
        qty.setHorizontalAlignment(SwingConstants.CENTER);

        stepper.add(minus);
        stepper.add(qty);
        stepper.add(plus);
        return stepper;
    }

    private void updateTotals() {
        CustomerChoice customer = (CustomerChoice) customerBox.getSelectedItem();
        double discountRate = customer != null ? customer.discountRate() : 0.0;
        SaleCart.Totals totals = cart.totals(discountRate);

        subtotalValue.setText("£" + String.format("%.2f", totals.subtotal()));
        discountLabel.setText("Discount (" + Math.round(discountRate * 100) + "%)");
        discountValue.setText("-£" + String.format("%.2f", totals.discount()));
        discountRow.setVisible(discountRate > 0);
        vatValue.setText("£0.00");
        totalValue.setText("£" + String.format("%.2f", totals.total()));
    }

    private void updatePaymentState() {
        boolean hasItems = !cart.isEmpty();
        CustomerChoice customer = (CustomerChoice) customerBox.getSelectedItem();
        cashButton.setEnabled(hasItems);
        cardButton.setEnabled(hasItems);
        creditButton.setEnabled(hasItems && customer != null && customer.creditEnabled());
    }

    private JPanel buildHistory() {
        var t = UI.table(
            UI.col("Sale #", SaleRow::saleId),
            UI.col("Customer", SaleRow::customer),
            UI.col("Date", SaleRow::date),
            UI.col("Items", SaleRow::items),
            UI.badgeCol("Payment", SaleRow::payment),
            UI.col("Total", SaleRow::total)
        ).rows(List.of(
            new SaleRow("#0001", "Ms Eva Bauyer", "2026-03-01", 4, "ON_CREDIT", "£63.60"),
            new SaleRow("#0002", "Walk-in", "2026-03-03", 2, "CASH", "£4.60")
        ));
        t.table().setDefaultRenderer(Object.class, UI.plainTableRenderer());
        t.table().setFont(UI.FONT);
        t.table().setRowHeight(34);
        JPanel view = UI.panel();
        view.add(t.scroll(), BorderLayout.CENTER);
        return view;
    }
}
