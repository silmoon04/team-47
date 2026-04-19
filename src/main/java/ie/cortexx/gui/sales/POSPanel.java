package ie.cortexx.gui.sales;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Payment;
import ie.cortexx.model.Sale;
import ie.cortexx.model.SaleItem;
import ie.cortexx.model.StockItem;
import ie.cortexx.service.CustomerService;
import ie.cortexx.service.DiscountService;
import ie.cortexx.service.OrderService;
import ie.cortexx.service.SaleService;
import ie.cortexx.service.ValidationResult;
import ie.cortexx.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// pos screen with a product list on the left and a custom current-sale cart on the right.
public class POSPanel extends JPanel implements RefreshablePage {
    private static final int CART_WIDTH = 470;
    private static final int CART_CONTROLS_WIDTH = 224;
    private static final int CART_STEPPER_WIDTH = 104;
    private final SaleCart cart = new SaleCart();
    private final SaleService saleService = new SaleService();
    private final CustomerService customerService = new CustomerService();
    private final DiscountService discountService = new DiscountService();
    private final OrderService orderService = new OrderService();
    private final ie.cortexx.dao.SaleDAO saleDAO = new ie.cortexx.dao.SaleDAO();
    private final ie.cortexx.dao.PaymentDAO paymentDAO = new ie.cortexx.dao.PaymentDAO();
    private final JPanel cartItemsPanel = new JPanel();
    private final JPanel historyDetailPanel = UI.transparentPanel(0);
    private final JLabel itemCountLabel = UI.countBadge("0 ITEMS");
    private final JComboBox<CustomerChoice> customerBox = new JComboBox<>();
    private final JLabel subtotalValue = UI.label("£0.00", UI.FONT_BOLD, UI.TEXT);
    private final JLabel discountLabel = UI.label("Discount (0%)", UI.FONT_SMALL, UI.TEXT_DIM);
    private final JLabel discountValue = UI.label("£0.00", UI.FONT_BOLD, UI.TEXT);
    private final JPanel discountRow = new JPanel(new BorderLayout());
    private final JLabel vatValue = UI.label("£0.00", UI.FONT_BOLD, UI.TEXT);
    private final JLabel totalValue = UI.label("£0.00", UI.FONT_BOLD.deriveFont(14f), UI.TEXT);
    private final JButton cashButton = UI.iconButton("Cash", "icons/banknote.svg", false);
    private final JButton cardButton = UI.iconButton("Card", "icons/credit-card.svg", true);
    private final JButton creditButton = UI.iconButton("Credit", "icons/coins.svg", false);
    private final JLabel clearanceStatusLabel = UI.label("Card clearance: ready", UI.FONT_SMALL, UI.TEXT_DIM);
    private final Map<Integer, StockItem> productById = new HashMap<>();
    private final Map<Integer, Customer> customersById = new HashMap<>();
    private final Map<Integer, String> customerNames = new HashMap<>();
    private Integer highlightedProductId;

    private record ProductRow(int productId, String name, BigDecimal price, int stock) {}
    private record SaleRow(int saleId, String saleRef, String customer, String date, String subtotal, String discount, int items, String payment, String total) {}
    private record CustomerChoice(Integer customerId, String label, boolean creditEnabled) {
        @Override public String toString() { return label; }
    }
    private record SalePricing(BigDecimal discountRate, SaleCart.Totals totals) {}
    private record CardPaymentDetails(PaymentType paymentType, String cardType, String cardHolderName,
                                      String cardNumber, String cardExpiry, String cvv) {}

    public POSPanel() {
        UI.applyPanelNoPad(this);
        reloadData();
    }

    @Override
    public void refreshPage() {
        reloadData();
    }

    private void reloadData() {
        removeAll();
        loadCustomers();
        add(UI.innerTabs(
            UI.tab("Point of Sale", buildPOS()),
            UI.tab("Sale History", buildHistory())
        ));
        revalidate();
        repaint();
    }

    private JPanel buildPOS() {
        var products = UI.table(
            UI.col("Name", ProductRow::name, 240),
            UI.col("Price", row -> money(row.price()), 90),
            UI.col("In Stock", ProductRow::stock, 80)
        ).rows(loadProducts()).onSelect(this::addToCart);
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
        view.add(UI.splitPanel(left, cart, CART_WIDTH), BorderLayout.CENTER);
        wirePaymentButtons();
        refreshCart();
        return view;
    }

    private JComponent buildCartHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = UI.label("Current Sale", UI.FONT_TITLE.deriveFont(17f), UI.TEXT);
        top.add(title, BorderLayout.WEST);
        top.add(itemCountLabel, BorderLayout.EAST);

        customerBox.setFont(UI.FONT);
        customerBox.setBackground(UI.BG_CARD);
        customerBox.setForeground(UI.TEXT);
        customerBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        for (ActionListener listener : customerBox.getActionListeners()) {
            customerBox.removeActionListener(listener);
        }
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

        summary.add(UI.summaryRow(UI.label("Subtotal", UI.FONT_SMALL, UI.TEXT_DIM), subtotalValue));
        discountRow.setOpaque(false);
        discountRow.add(discountLabel, BorderLayout.WEST);
        discountRow.add(discountValue, BorderLayout.EAST);
        summary.add(Box.createVerticalStrut(6));
        summary.add(discountRow);
        summary.add(Box.createVerticalStrut(6));
        summary.add(UI.summaryRow(UI.label("VAT (0%)", UI.FONT_SMALL, UI.TEXT_DIM), vatValue));
        summary.add(Box.createVerticalStrut(10));
        summary.add(new JSeparator());
        summary.add(Box.createVerticalStrut(10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLabel = UI.label("Total", UI.FONT_BOLD.deriveFont(14f), UI.TEXT);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);
        summary.add(totalRow);
        summary.add(Box.createVerticalStrut(8));
        summary.add(clearanceStatusLabel);

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

    private void addToCart(ProductRow row) {
        if (!canAddQuantity(row.productId(), row.name(), row.stock())) {
            return;
        }

        cart.addItem(row.productId(), row.name(), row.price());
        highlightedProductId = row.productId();
        refreshCart();
    }

    private void updateQty(int productId, int delta) {
        if (delta > 0) {
            StockItem stockItem = productById.get(productId);
            String name = stockItem != null ? stockItem.getProductName() : "item";
            int available = stockItem != null ? stockItem.getQuantity() : 0;
            if (!canAddQuantity(productId, name, available)) {
                return;
            }
        }

        cart.updateQuantity(productId, delta);
        highlightedProductId = productId;
        refreshCart();
    }

    private boolean canAddQuantity(int productId, String name, int available) {
        if (available <= 0) {
            UI.notifyInfo(this, name + " is out of stock.");
            return false;
        }
        if (cart.quantityOf(productId) >= available) {
            UI.notifyInfo(this, "Only " + available + " available for " + name + ".");
            return false;
        }
        return true;
    }

    private void removeItem(int productId) {
        cart.removeItem(productId);
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
        highlightedProductId = null;

        itemCountLabel.setText(cart.itemCount() + " ITEMS");
        updateTotals();
        updatePaymentState();
        clearanceStatusLabel.setText("Card clearance: ready");
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private JPanel cartRow(SaleCart.Item item, boolean divider) {
        CartRowPanel row = new CartRowPanel();
        row.setLayout(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            divider ? BorderFactory.createMatteBorder(0, 0, 1, 0, UI.BORDER) : BorderFactory.createEmptyBorder(),
            new EmptyBorder(10, 16, 10, 16)
        ));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel copy = new JPanel();
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.setOpaque(false);
        copy.setMinimumSize(new Dimension(0, 44));

        JLabel name = UI.label(item.name(), UI.FONT_BOLD, UI.TEXT);
        JLabel each = UI.label("£" + String.format("%.2f", item.unitPrice()) + " each", UI.FONT_SMALL, UI.TEXT_DIM);
        name.setToolTipText(item.name());
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        each.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        copy.add(name);
        copy.add(Box.createVerticalStrut(2));
        copy.add(each);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.setOpaque(false);
        controls.setAlignmentY(Component.CENTER_ALIGNMENT);
        controls.setPreferredSize(new Dimension(CART_CONTROLS_WIDTH, 34));
        controls.setMinimumSize(new Dimension(CART_CONTROLS_WIDTH, 34));
        controls.setMaximumSize(new Dimension(CART_CONTROLS_WIDTH, 34));
        controls.add(qtyStepper(item, availableQuantity(item.productId())));
        controls.add(Box.createHorizontalStrut(10));

        JLabel total = UI.label("£" + String.format("%.2f", item.lineTotal()), UI.FONT_BOLD, UI.TEXT);
        total.setHorizontalAlignment(SwingConstants.RIGHT);
        total.setPreferredSize(new Dimension(64, 28));
        total.setMinimumSize(new Dimension(64, 28));
        controls.add(total);
        controls.add(Box.createHorizontalStrut(8));

        JButton remove = UI.iconActionButton("icons/trash-2.svg", "Remove item", true);
        remove.setAlignmentY(Component.CENTER_ALIGNMENT);
        remove.addActionListener(e -> removeItem(item.productId()));
        controls.add(remove);
        row.add(copy, BorderLayout.CENTER);
        row.add(controls, BorderLayout.EAST);
        if (Integer.valueOf(item.productId()).equals(highlightedProductId)) {
            row.pulse();
        }
        return row;
    }

    private JComponent qtyStepper(SaleCart.Item item, int available) {
        JPanel stepper = new JPanel();
        stepper.setLayout(new BoxLayout(stepper, BoxLayout.X_AXIS));
        stepper.setOpaque(false);
        stepper.setAlignmentY(Component.CENTER_ALIGNMENT);
        stepper.setPreferredSize(new Dimension(CART_STEPPER_WIDTH, 32));
        stepper.setMinimumSize(new Dimension(CART_STEPPER_WIDTH, 32));
        stepper.setMaximumSize(new Dimension(CART_STEPPER_WIDTH, 32));

        JButton minus = UI.stepperButton("-");
        minus.setAlignmentY(Component.CENTER_ALIGNMENT);
        minus.addActionListener(e -> updateQty(item.productId(), -1));
        JButton plus = UI.stepperButton("+");
        plus.setAlignmentY(Component.CENTER_ALIGNMENT);
        plus.setEnabled(available > 0 && item.quantity() < available);
        plus.setToolTipText(plus.isEnabled() ? "Increase quantity" : "No more stock available");
        plus.addActionListener(e -> updateQty(item.productId(), 1));

        JLabel qty = UI.label(String.valueOf(item.quantity()), UI.FONT_BOLD, UI.TEXT);
        qty.setHorizontalAlignment(SwingConstants.CENTER);
        qty.setPreferredSize(new Dimension(18, 28));
        qty.setMinimumSize(new Dimension(18, 28));
        qty.setMaximumSize(new Dimension(18, 28));
        qty.setAlignmentY(Component.CENTER_ALIGNMENT);

        stepper.add(minus);
        stepper.add(Box.createHorizontalStrut(5));
        stepper.add(qty);
        stepper.add(Box.createHorizontalStrut(5));
        stepper.add(plus);
        return stepper;
    }

    private int availableQuantity(int productId) {
        StockItem stockItem = productById.get(productId);
        return stockItem == null ? 0 : stockItem.getQuantity();
    }

    private static final class CartRowPanel extends JPanel {
        private float pulse = 0f;

        private void pulse() {
            pulse = 1f;
            Timer timer = new Timer(16, null);
            timer.addActionListener(e -> {
                pulse = Math.max(0f, pulse - 0.08f);
                repaint();
                if (pulse <= 0f) {
                    timer.stop();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (pulse <= 0f) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(UI.ACCENT.getRed(), UI.ACCENT.getGreen(), UI.ACCENT.getBlue(), Math.round(30 * pulse)));
            g2.fillRoundRect(8, 5, getWidth() - 16, getHeight() - 10, UI.FIELD_ARC, UI.FIELD_ARC);
            g2.dispose();
        }
    }

    private void updateTotals() {
        applyPricing(currentPricing());
    }

    private void applyPricing(SalePricing pricing) {
        SaleCart.Totals totals = pricing.totals();
        BigDecimal discountRate = pricing.discountRate();

        subtotalValue.setText("£" + String.format("%.2f", totals.subtotal()));
        discountLabel.setText("Discount (" + discountRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%)");
        discountValue.setText("-£" + String.format("%.2f", totals.discount()));
        discountRow.setVisible(discountRate.compareTo(BigDecimal.ZERO) > 0);
        vatValue.setText("£0.00");
        totalValue.setText("£" + String.format("%.2f", totals.total()));
    }

    private SalePricing currentPricing() {
        BigDecimal discountRate = resolveDiscountRate((CustomerChoice) customerBox.getSelectedItem());
        return new SalePricing(discountRate, cart.totals(discountRate));
    }

    private BigDecimal resolveDiscountRate(CustomerChoice choice) {
        if (choice == null || choice.customerId() == null || cart.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Customer customer = customersById.get(choice.customerId());
        if (customer == null || customer.getDiscountType() == null) {
            return BigDecimal.ZERO;
        }

        try {
            return discountService.resolveRate(customer, cart.totals(BigDecimal.ZERO).subtotal(), LocalDate.now());
        } catch (Exception error) {
            return BigDecimal.ZERO;
        }
    }

    private void updatePaymentState() {
        boolean hasItems = !cart.isEmpty();
        CustomerChoice customer = (CustomerChoice) customerBox.getSelectedItem();
        boolean isAccountCustomer = customer != null && customer.customerId() != null;
        cashButton.setEnabled(hasItems);
        cardButton.setEnabled(hasItems);
        creditButton.setEnabled(hasItems && customer != null && customer.creditEnabled());
    }

    private JPanel buildHistory() {
        List<SaleRow> historyRows = loadHistory();
        if (historyRows.isEmpty()) {
            JPanel view = UI.panel();
            view.add(UI.emptyState("No recent sales yet"), BorderLayout.CENTER);
            return view;
        }

        var t = UI.table(
            UI.col("Sale #", SaleRow::saleRef),
            UI.col("Customer", SaleRow::customer),
            UI.col("Date", SaleRow::date),
            UI.col("Items", SaleRow::items),
            UI.badgeCol("Payment", SaleRow::payment),
            UI.col("Total", SaleRow::total)
        ).rows(historyRows).onSelect(this::showSaleDetail);
        t.table().setDefaultRenderer(Object.class, UI.plainTableRenderer());
        t.table().setFont(UI.FONT);
        t.table().setRowHeight(34);

        JPanel left = UI.toolbarAndTable(UI.toolbar("Search sale history...", t.table()), t.scroll());
        JPanel view = UI.panel();
        view.add(UI.splitPanel(left, historyDetailPanel, 430), BorderLayout.CENTER);

        showSaleDetail(historyRows.get(0));
        t.table().setRowSelectionInterval(0, 0);
        return view;
    }

    private void showSaleDetail(SaleRow row) {
        try {
            List<SaleItem> items = saleDAO.findItemsBySaleId(row.saleId());
            List<Payment> payments = paymentDAO.findBySale(row.saleId());
            Payment payment = payments.isEmpty() ? null : payments.get(0);

            JPanel card = UI.formCard();
            card.add(UI.fullWidth(UI.sectionLabel("SALE RECEIPT")));
            card.add(UI.gap(10));
            card.add(UI.fullWidth(UI.detailLine("SALE", row.saleRef())));
            card.add(UI.fullWidth(UI.detailLine("CUSTOMER", row.customer())));
            card.add(UI.fullWidth(UI.detailLine("DATE", row.date())));
            card.add(UI.fullWidth(UI.detailLine("PAYMENT", row.payment())));
            card.add(UI.fullWidth(UI.detailLine("SUBTOTAL", row.subtotal())));
            card.add(UI.fullWidth(UI.detailLine("DISCOUNT", row.discount())));
            card.add(UI.fullWidth(UI.detailLine("TOTAL", row.total())));
            if (payment != null && payment.getCardLast4() != null) {
                card.add(UI.fullWidth(UI.detailLine("CARD", payment.getCardType() + " ****" + payment.getCardLast4())));
            }
            card.add(UI.gap(12));
            card.add(UI.fullWidth(UI.sectionLabel("ITEMS")));
            card.add(UI.gap(8));
            for (SaleItem item : items) {
                String value = item.getQuantity() + " x " + money(item.getUnitPrice()) + " = " + money(item.getLineTotal());
                card.add(UI.fullWidth(UI.detailLine(item.getProductName(), value, 180, false)));
            }
            UI.swap(historyDetailPanel, card);
        } catch (Exception error) {
            UI.swap(historyDetailPanel, UI.emptyState(error.getMessage()));
        }
    }

    private List<ProductRow> loadProducts() {
        productById.clear();
        try {
            return orderService.getCatalogue().stream().map(item -> {
                productById.put(item.getProductId(), item);
                BigDecimal retail = item.getCostPrice().multiply(BigDecimal.ONE.add(item.getMarkupRate()));
                return new ProductRow(item.getProductId(), item.getProductName(), retail, item.getQuantity());
            }).toList();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private void loadCustomers() {
        customerBox.removeAllItems();
        customerNames.clear();
        customersById.clear();
        customerBox.addItem(new CustomerChoice(null, "Walk-in Customer", false));

        try {
            for (Customer customer : customerService.findAll()) {
                customerNames.put(customer.getCustomerId(), customer.getName());
                customersById.put(customer.getCustomerId(), customer);
                boolean creditEnabled = customer.getAccountStatus() == AccountStatus.NORMAL;
                customerBox.addItem(new CustomerChoice(
                    customer.getCustomerId(),
                    customer.getName() + " (" + text(customer.getAccountNo()) + ")",
                    creditEnabled
                ));
            }
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void wirePaymentButtons() {
        resetButton(cashButton, e -> checkout(PaymentType.CASH));
        resetButton(cardButton, e -> checkout(PaymentType.CREDIT_CARD));
        resetButton(creditButton, e -> checkout(PaymentType.ON_CREDIT));
    }

    private void resetButton(AbstractButton button, ActionListener listener) {
        for (ActionListener existing : button.getActionListeners()) {
            button.removeActionListener(existing);
        }
        button.addActionListener(listener);
    }

    private void checkout(PaymentType paymentType) {
        if (cart.isEmpty()) {
            return;
        }

        if (!UI.confirm(this, "Complete this sale?", "Confirm Checkout")) {
            return;
        }

        SalePricing pricing = currentPricing();
        applyPricing(pricing);
        clearanceStatusLabel.setText("Card clearance: pending");

        CustomerChoice customer = (CustomerChoice) customerBox.getSelectedItem();
        SaleCart.Totals totals = pricing.totals();
        BigDecimal discountRate = pricing.discountRate();

        CardPaymentDetails cardDetails = null;
        if (paymentType == PaymentType.CREDIT_CARD || paymentType == PaymentType.DEBIT_CARD) {
            cardDetails = promptCardPaymentDetails(paymentType);
            if (cardDetails == null) {
                clearanceStatusLabel.setText("Card clearance: cancelled");
                return;
            }
            paymentType = cardDetails.paymentType();
        }

        Sale sale = new Sale();
        sale.setCustomerId(customer != null ? customer.customerId() : null);
        sale.setSoldBy(currentUserId());
        sale.setSubtotal(totals.subtotal());
        sale.setDiscountAmount(totals.discount());
        sale.setVatAmount(BigDecimal.ZERO);
        sale.setTotalAmount(totals.total());
        sale.setPaymentMethod(paymentType.name());
        sale.setWalkIn(customer == null || customer.customerId() == null);

        for (SaleCart.Item item : cart.items()) {
            StockItem stockItem = productById.get(item.productId());
            if (stockItem == null) {
                continue;
            }
            BigDecimal unitPrice = item.unitPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity())).multiply(BigDecimal.ONE.subtract(discountRate));
            SaleItem saleItem = new SaleItem(stockItem.getProductId(), stockItem.getProductName(), item.quantity(), unitPrice, lineTotal);
            saleItem.setDiscountRate(discountRate);
            sale.getItems().add(saleItem);
        }

        Payment payment = new Payment();
        if (customer != null && customer.customerId() != null) {
            payment.setCustomerId(customer.customerId());
        }
        payment.setPaymentType(paymentType);
        payment.setAmount(totals.total());
        payment.setChangeGiven(BigDecimal.ZERO);
        if (paymentType == PaymentType.CREDIT_CARD || paymentType == PaymentType.DEBIT_CARD) {
            payment.setCardType(cardDetails.cardType());
            payment.setCardFirst4(cardDetails.cardNumber().substring(0, 4));
            payment.setCardLast4(cardDetails.cardNumber().substring(12));
            payment.setCardExpiry(cardDetails.cardExpiry());
            // clearance via PU subsystem (Team C)
            String puResult = clearCardViaPU(totals.total(), cardDetails);
            if (puResult != null && puResult.startsWith("FAIL")) {
                clearanceStatusLabel.setText("Card clearance: declined");
                UI.notifyError(this, "Card declined by PU: " + puResult);
                return;
            }
            clearanceStatusLabel.setText(puResult == null
                ? "Card clearance: recorded locally"
                : "Card clearance: " + puResult);
        }

        ValidationResult result = saleService.processSale(sale, payment);
        if (!result.isValid()) {
            if (paymentType == PaymentType.CREDIT_CARD || paymentType == PaymentType.DEBIT_CARD) {
                clearanceStatusLabel.setText("Card clearance: failed");
            }
            UI.notifyError(this, result.getMessage());
            return;
        }

        cart.clear();
        UI.notifySuccess(this, "Sale completed.");
        reloadData();
    }

    private CardPaymentDetails promptCardPaymentDetails(PaymentType requestedType) {
        JTextField cardHolderName = new JTextField();
        JTextField cardNumber = new JTextField();
        JTextField cardExpiry = new JTextField("12/2028");
        JTextField cardType = new JTextField("VISA");
        JPasswordField cvv = new JPasswordField();
        JComboBox<PaymentType> typeBox = new JComboBox<>(new PaymentType[]{PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD});
        typeBox.setSelectedItem(requestedType == PaymentType.DEBIT_CARD ? PaymentType.DEBIT_CARD : PaymentType.CREDIT_CARD);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Cardholder"));
        form.add(cardHolderName);
        form.add(new JLabel("Card number"));
        form.add(cardNumber);
        form.add(new JLabel("Card expiry"));
        form.add(cardExpiry);
        form.add(new JLabel("Card type"));
        form.add(cardType);
        form.add(new JLabel("Security code"));
        form.add(cvv);
        form.add(new JLabel("Payment type"));
        form.add(typeBox);

        int choice = JOptionPane.showConfirmDialog(this, form, "Card Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        String holder = cardHolderName.getText() == null ? "" : cardHolderName.getText().trim();
        String number = cardNumber.getText() == null ? "" : cardNumber.getText().replaceAll("\\s+", "");
        String expiry = cardExpiry.getText() == null ? "" : cardExpiry.getText().trim();
        String type = cardType.getText() == null ? "" : cardType.getText().trim();
        String securityCode = new String(cvv.getPassword()).trim();

        if (holder.isEmpty()) {
            UI.notifyError(this, "Cardholder name is required");
            return null;
        }
        if (!number.matches("\\d{16}")) {
            UI.notifyError(this, "Card number must contain exactly 16 digits");
            return null;
        }
        if (type.isEmpty()) {
            UI.notifyError(this, "Card type is required");
            return null;
        }
        if (!expiry.matches("(0[1-9]|1[0-2])/(\\d{2}|\\d{4})")) {
            UI.notifyError(this, "Card expiry must be MM/YY or MM/YYYY");
            return null;
        }
        if (!securityCode.matches("\\d{3,4}")) {
            UI.notifyError(this, "Security code must contain 3 or 4 digits");
            return null;
        }

        return new CardPaymentDetails(
            (PaymentType) typeBox.getSelectedItem(),
            type,
            holder,
            number,
            expiry,
            securityCode
        );
    }

    private List<SaleRow> loadHistory() {
        try {
            return saleDAO.findByDateRange(LocalDate.now().minusDays(60), LocalDate.now()).stream().map(sale -> new SaleRow(
                sale.getSaleId(),
                "#" + sale.getSaleId(),
                sale.getCustomerId() != null ? customerNames.getOrDefault(sale.getCustomerId(), "Unknown") : "Walk-in",
                sale.getSaleDate() != null ? sale.getSaleDate().toLocalDate().toString() : "",
                money(sale.getSubtotal()),
                money(sale.getDiscountAmount()),
                itemCount(sale.getSaleId()),
                text(sale.getPaymentMethod()),
                money(sale.getTotalAmount())
            )).toList();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private int itemCount(int saleId) {
        try {
            return saleDAO.countItems(saleId);
        } catch (Exception error) {
            return 0;
        }
    }

    private int currentUserId() {
        return SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getUserId()
            : 1;
    }

    private String money(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String text(String value) {
        return value != null ? value : "";
    }

    /** Call PU subsystem to clear card payment. Returns txn ID on success, null if PU unreachable. */
    private String clearCardViaPU(BigDecimal amount, CardPaymentDetails details) {
        try {
            java.net.URL url = new java.net.URI("http://localhost:8080/process-card-payment").toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            String body = "amount=" + encode(amount.toPlainString())
                + "&paymentType=" + encode(details.paymentType().name())
                + "&cardType=" + encode(details.cardType())
                + "&cardHolderName=" + encode(details.cardHolderName())
                + "&cardNumber=" + encode(details.cardNumber())
                + "&cardExpiry=" + encode(details.cardExpiry())
                + "&cardSecurityCode=" + encode(details.cvv());
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            int code = conn.getResponseCode();
            java.io.InputStream responseStream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String response = responseStream != null
                ? new String(responseStream.readAllBytes(), StandardCharsets.UTF_8)
                : "";
            conn.disconnect();
            if (code == 200) {
                return response.trim().isEmpty() ? "APPROVED" : response.trim();
            }
            return "FAIL: HTTP " + code + (response.isBlank() ? "" : " - " + response.trim());
        } catch (Exception e) {
            // PU not running — fall through and record locally
            return null;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
