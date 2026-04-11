package ie.cortexx.gui.customer;

import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;
import ie.cortexx.service.CustomerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// customer list on the left with a denser profile card and discount tiers on the right.
public class CustomerListPanel extends JPanel implements RefreshablePage {
    private final JPanel detailPanel = UI.transparentPanel(0);
    private final CustomerService customerService = new CustomerService();
    private final List<CustomerRow> rows = new ArrayList<>();

    private record DiscountTierRow(String tier, String minSpend, String rate) {}

    private record CustomerRow(
        int customerId,
        String account,
        String name,
        String status,
        String balance,
        String limit,
        String discount,
        String contact,
        String phone,
        String address,
        String lastPayment,
        List<DiscountTierRow> tiers
    ) {}

    public CustomerListPanel() {
        UI.applyPanel(this);
        reload();
    }

    @Override
    public void refreshPage() {
        reload();
    }

    private void reload() {
        removeAll();
        rows.clear();
        rows.addAll(loadRows());

        var customers = UI.table(
            UI.col("Account", CustomerRow::account, 96),
            UI.col("Name", CustomerRow::name, 180),
            UI.badgeCol("Status", CustomerRow::status, 86),
            UI.col("Balance", CustomerRow::balance, 84),
            UI.col("Credit Limit", CustomerRow::limit, 96),
            UI.badgeCol("Discount", CustomerRow::discount, 92)
        ).rows(rows).onSelect(this::showDetail);

        JButton newCustomer = UI.primaryButton("+ New Customer");
        newCustomer.addActionListener(e -> createCustomer());
        JPanel left = UI.toolbarAndTable(
            UI.toolbar("Search customers...", customers.table(), newCustomer),
            customers.scroll()
        );

        add(UI.splitPanel(left, detailPanel, 460), BorderLayout.CENTER);

        if (!rows.isEmpty()) {
            customers.table().setRowSelectionInterval(0, 0);
            showDetail(rows.get(0));
        } else {
            UI.swap(detailPanel, UI.emptyState("Select a customer to view details"));
        }
        revalidate();
        repaint();
    }

    private void showDetail(CustomerRow row) {
        UI.swap(detailPanel, buildDetail(row));
    }

    private JComponent buildDetail(CustomerRow row) {
        JPanel card = UI.formCard();

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        header.add(UI.avatarBadge(row.name(), 60), BorderLayout.WEST);

        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        identity.setOpaque(false);
        identity.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel name = UI.label(row.name(), UI.FONT_BOLD.deriveFont(14f), UI.TEXT);

        JPanel chips = UI.flowRow(8);
        chips.add(UI.badge(row.status()));
        chips.add(UI.badge(row.discount()));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        identity.add(name);
        identity.add(Box.createVerticalStrut(6));
        identity.add(chips);
        header.add(identity, BorderLayout.CENTER);

        card.add(UI.fullWidth(header));
        card.add(UI.gap(10));
        card.add(UI.fullWidth(UI.detailLine("ACCOUNT NO", row.account())));
        card.add(UI.fullWidth(UI.detailLine("CONTACT", row.contact())));
        card.add(UI.fullWidth(UI.detailLine("PHONE", row.phone())));
        card.add(UI.fullWidth(UI.detailLine("ADDRESS", row.address(), true)));
        card.add(UI.fullWidth(UI.detailLine("CREDIT LIMIT", row.limit())));
        card.add(UI.fullWidth(UI.detailLine("OUTSTANDING BALANCE", row.balance())));
        card.add(UI.fullWidth(UI.detailLine("DISCOUNT TYPE", row.discount().startsWith("FIXED") ? "FIXED" : "FLEXIBLE")));
        card.add(UI.gap(14));

        JLabel tiersLabel = UI.sectionLabel("DISCOUNT TIERS");
        card.add(UI.fullWidth(tiersLabel));
        card.add(UI.gap(6));
        card.add(UI.fullWidth(discountTierTable(row.tiers())));
        card.add(UI.gap(6));
        card.add(UI.fullWidth(UI.detailLine("LAST PAYMENT", row.lastPayment())));
        card.add(UI.gap(10));

        JButton edit = UI.button("Edit");
        JButton receivePayment = UI.button("Receive Payment");
        JButton delete = UI.dangerButton("Delete");
        edit.addActionListener(e -> editCustomer(row.customerId()));
        receivePayment.addActionListener(e -> receivePayment(row.customerId()));
        delete.addActionListener(e -> deleteCustomer(row.customerId()));
        card.add(UI.fullWidth(UI.buttonRow(edit, receivePayment, delete)));
        return card;
    }

    private JComponent discountTierTable(List<DiscountTierRow> rows) {
        var tiers = UI.table(
            UI.col("Tier", DiscountTierRow::tier, 220),
            UI.monoCol("Min Spend", DiscountTierRow::minSpend, 120),
            UI.monoCol("Rate", DiscountTierRow::rate, 90)
        ).rows(rows);

        tiers.table().setRowSelectionAllowed(false);
        tiers.table().setFocusable(false);
        tiers.table().setRowHeight(28);
        tiers.scroll().setAlignmentX(Component.LEFT_ALIGNMENT);
        int height = 34 + (rows.size() * 28);
        tiers.scroll().setPreferredSize(new Dimension(400, height));
        tiers.scroll().setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return tiers.scroll();
    }

    private List<CustomerRow> loadRows() {
        try {
            return customerService.findAll().stream().map(this::toRow).toList();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private CustomerRow toRow(Customer customer) {
        return new CustomerRow(
            customer.getCustomerId(),
            text(customer.getAccountNo()),
            text(customer.getName()),
            customer.getAccountStatus().name(),
            money(customer.getOutstandingBalance()),
            money(customer.getCreditLimit()),
            discountLabel(customer),
            text(customer.getContactName()),
            text(customer.getPhone()),
            text(customer.getAddress()),
            customer.getLastPaymentDate() != null ? customer.getLastPaymentDate().toString() : "-",
            loadTiers(customer)
        );
    }

    private List<DiscountTierRow> loadTiers(Customer customer) {
        if (customer.getDiscountType() == DiscountType.FIXED) {
            BigDecimal rate = customer.getFixedDiscountRate() != null ? customer.getFixedDiscountRate() : BigDecimal.ZERO;
            return List.of(new DiscountTierRow("Standard", "£0.00", percent(rate)));
        }

        try {
            List<DiscountTier> tiers = customerService.findTiers(customer.getCustomerId());
            if (tiers.isEmpty()) {
                return List.of(new DiscountTierRow("Default", "£0.00", "0%"));
            }
            return tiers.stream()
                .map(tier -> new DiscountTierRow(tier.getTierName(), money(tier.getMinMonthlySpend()), percent(tier.getDiscountRate())))
                .toList();
        } catch (Exception error) {
            return List.of();
        }
    }

    private String discountLabel(Customer customer) {
        if (customer.getDiscountType() == DiscountType.FIXED) {
            BigDecimal rate = customer.getFixedDiscountRate() != null ? customer.getFixedDiscountRate() : BigDecimal.ZERO;
            return "FIXED " + percent(rate);
        }
        return "FLEXIBLE";
    }

    private void createCustomer() {
        JTextField name = new JTextField();
        JTextField contact = new JTextField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        JComboBox<String> discountType = new JComboBox<>(new String[]{"FIXED", "FLEXIBLE"});

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Contact"));
        form.add(contact);
        form.add(new JLabel("Phone"));
        form.add(phone);
        form.add(new JLabel("Address"));
        form.add(address);
        form.add(new JLabel("Discount Type"));
        form.add(discountType);

        if (JOptionPane.showConfirmDialog(this, form, "New Customer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Customer customer = new Customer(name.getText().trim(), address.getText().trim(), DiscountType.valueOf(discountType.getSelectedItem().toString()));
            customer.setAccountNo("ACC" + System.currentTimeMillis());
            customer.setContactName(contact.getText().trim());
            customer.setPhone(phone.getText().trim());
            if (customer.getDiscountType() == DiscountType.FIXED) {
                customer.setFixedDiscountRate(new BigDecimal("0.0300"));
            }
            customerService.save(customer);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCustomer(int customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return;
            }

            JTextField name = new JTextField(text(customer.getName()));
            JTextField contact = new JTextField(text(customer.getContactName()));
            JTextField phone = new JTextField(text(customer.getPhone()));
            JTextField address = new JTextField(text(customer.getAddress()));
            JTextField limit = new JTextField(customer.getCreditLimit() != null ? customer.getCreditLimit().toPlainString() : "500.00");

            JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
            form.add(new JLabel("Name"));
            form.add(name);
            form.add(new JLabel("Contact"));
            form.add(contact);
            form.add(new JLabel("Phone"));
            form.add(phone);
            form.add(new JLabel("Address"));
            form.add(address);
            form.add(new JLabel("Credit Limit"));
            form.add(limit);

            if (JOptionPane.showConfirmDialog(this, form, "Edit Customer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            customer.setName(name.getText().trim());
            customer.setContactName(contact.getText().trim());
            customer.setPhone(phone.getText().trim());
            customer.setAddress(address.getText().trim());
            customer.setCreditLimit(new BigDecimal(limit.getText().trim()));
            customerService.update(customer);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void receivePayment(int customerId) {
        String amountText = JOptionPane.showInputDialog(this, "Payment amount", "10.00");
        if (amountText == null || amountText.isBlank()) {
            return;
        }

        try {
            customerService.receivePayment(customerId, new BigDecimal(amountText.trim()), PaymentType.ACCOUNT_PAYMENT);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Payment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer(int customerId) {
        if (JOptionPane.showConfirmDialog(this, "Delete this customer?", "Confirm Delete", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            customerService.delete(customerId);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String money(BigDecimal value) {
        BigDecimal safe = value != null ? value : BigDecimal.ZERO;
        return "£" + safe.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String percent(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }

    private String text(String value) {
        return value != null ? value : "";
    }

}
