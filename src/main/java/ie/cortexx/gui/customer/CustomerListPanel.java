package ie.cortexx.gui.customer;

import ie.cortexx.dao.ReminderDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.enums.UserRole;
import ie.cortexx.gui.RefreshablePage;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;
import ie.cortexx.model.Reminder;
import ie.cortexx.service.CustomerService;
import ie.cortexx.service.DebtCycleService;
import ie.cortexx.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// customer list on the left with a denser profile card and discount tiers on the right.
public class CustomerListPanel extends JPanel implements RefreshablePage {
    private final JPanel detailPanel = UI.transparentPanel(0);
    private final CustomerService customerService = new CustomerService();
    private final ReminderDAO reminderDAO = new ReminderDAO();
    private final List<CustomerRow> rows = new ArrayList<>();

    private record DiscountTierRow(int tierId, String tier, BigDecimal minSpend, BigDecimal rate) {}
    private record ReminderRow(String type, String amount, String dueDate, String sentDate, String content) {}

    private record CustomerRow(
        int customerId,
        String account,
        String name,
        AccountStatus status,
        String balance,
        String limit,
        DiscountType discountType,
        BigDecimal fixedDiscountRate,
        String contact,
        String phone,
        String address,
        String lastPayment,
        List<DiscountTierRow> tiers,
        List<ReminderRow> reminders
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
        // auto-run debt cycle checks (suspend/escalate) on every refresh
        runDebtCycleChecks();
        rows.addAll(loadRows());

        var customers = UI.table(
            UI.col("Account", CustomerRow::account, 96),
            UI.col("Name", CustomerRow::name, 180),
            UI.badgeCol("Status", row -> row.status().name(), 86),
            UI.col("Balance", CustomerRow::balance, 84),
            UI.col("Credit Limit", CustomerRow::limit, 96),
            UI.badgeCol("Discount", this::discountLabel, 92)
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

    private void runDebtCycleChecks() {
        try {
            DebtCycleService debtCycleService = new DebtCycleService(new ie.cortexx.dao.CustomerDAO());
            for (Customer customer : customerService.findAll()) {
                debtCycleService.checkAndSuspend(customer.getCustomerId());
                debtCycleService.escalateToDefault(customer.getCustomerId());
            }
        } catch (Exception ignored) {
            // keep the customer screen usable even if the background status sync fails
        }
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
        chips.add(UI.badge(row.status().name()));
        chips.add(UI.badge(discountLabel(row)));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        identity.add(name);
        identity.add(Box.createVerticalStrut(6));
        identity.add(chips);
        header.add(identity, BorderLayout.CENTER);

        card.add(UI.fullWidth(header));
        card.add(UI.gap(10));
        card.add(UI.fullWidth(UI.innerTabs(
            UI.tab("Account", buildAccountTab(row)),
            UI.tab("Discount Plan", buildDiscountTab(row)),
            UI.tab("Reminders", buildReminderTab(row))
        )));
        return card;
    }

    private JPanel buildAccountTab(CustomerRow row) {
        JPanel tab = UI.formCard();
        tab.add(UI.fullWidth(UI.detailLine("ACCOUNT NO", row.account())));
        tab.add(UI.fullWidth(UI.detailLine("CONTACT", row.contact())));
        tab.add(UI.fullWidth(UI.detailLine("PHONE", row.phone())));
        tab.add(UI.fullWidth(UI.detailLine("ADDRESS", row.address(), true)));
        tab.add(UI.fullWidth(UI.detailLine("CREDIT LIMIT", row.limit())));
        tab.add(UI.fullWidth(UI.detailLine("OUTSTANDING BALANCE", row.balance())));
        tab.add(UI.fullWidth(UI.detailLine("DISCOUNT TYPE", discountTypeName(row.discountType()))));
        if (row.discountType() == DiscountType.FIXED) {
            tab.add(UI.fullWidth(UI.detailLine("FIXED RATE", percent(row.fixedDiscountRate()))));
        }
        tab.add(UI.fullWidth(UI.detailLine("LAST PAYMENT", row.lastPayment())));
        tab.add(UI.gap(10));

        JButton edit = UI.button("Edit");
        JButton receivePayment = UI.button("Receive Payment");
        JButton confirmRestore = UI.button("Confirm Restore");
        JButton delete = UI.dangerButton("Delete Customer");
        edit.addActionListener(e -> editCustomer(row.customerId()));
        receivePayment.addActionListener(e -> receivePayment(row.customerId()));
        confirmRestore.addActionListener(e -> confirmRestore(row.customerId()));
        delete.addActionListener(e -> deleteCustomer(row.customerId()));
        confirmRestore.setEnabled(row.status() == AccountStatus.IN_DEFAULT && SessionManager.getInstance().hasRole(UserRole.MANAGER));
        tab.add(UI.fullWidth(UI.buttonRow(edit, receivePayment, confirmRestore)));
        tab.add(UI.gap(8));
        tab.add(UI.fullWidth(UI.buttonRow(delete)));
        return tab;
    }

    private JPanel buildDiscountTab(CustomerRow row) {
        JPanel tab = UI.formCard();
        tab.add(UI.fullWidth(UI.detailLine("DISCOUNT TYPE", discountTypeName(row.discountType()))));
        tab.add(UI.gap(8));

        if (row.discountType() == null) {
            tab.add(UI.fullWidth(UI.emptyState("No discount plan is configured for this customer.")));
            tab.add(UI.gap(10));
            JButton switchToFlexible = UI.button("Switch To Flexible");
            switchToFlexible.addActionListener(e -> switchToFlexible(row.customerId()));
            tab.add(UI.fullWidth(UI.buttonRow(switchToFlexible)));
            return tab;
        }

        if (row.discountType() == DiscountType.FIXED) {
            tab.add(UI.fullWidth(UI.statusBanner("FIXED", "Use Edit Customer to switch this account to flexible pricing.", UI.ACCENT)));
            tab.add(UI.gap(10));
            tab.add(UI.fullWidth(UI.detailLine("FIXED RATE", percent(row.fixedDiscountRate()))));
            JButton deletePlan = UI.dangerButton("Delete Discount Plan");
            JButton switchToFlexible = UI.button("Switch To Flexible");
            deletePlan.addActionListener(e -> deleteDiscountPlan(row.customerId()));
            switchToFlexible.addActionListener(e -> switchToFlexible(row.customerId()));
            tab.add(UI.gap(10));
            tab.add(UI.fullWidth(UI.buttonRow(switchToFlexible, deletePlan)));
            return tab;
        }

        UI.DataTable<DiscountTierRow> tierTable = UI.table(
            UI.col("Tier", DiscountTierRow::tier, 220),
            UI.monoCol("Min Spend", rowValue -> money(rowValue.minSpend()), 120),
            UI.monoCol("Rate", rowValue -> percent(rowValue.rate()), 90)
        ).rows(row.tiers());

        tierTable.table().setRowHeight(28);
        tierTable.scroll().setAlignmentX(Component.LEFT_ALIGNMENT);
        int height = 34 + Math.max(1, row.tiers().size()) * 28;
        tierTable.scroll().setPreferredSize(new Dimension(400, height));
        tierTable.scroll().setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        tab.add(UI.fullWidth(tierTable.scroll()));
        tab.add(UI.gap(10));

        JButton addTier = UI.button("Add Tier");
        JButton editTier = UI.button("Edit Tier");
        JButton deleteTier = UI.dangerButton("Delete Tier");
        JButton deletePlan = UI.dangerButton("Delete Discount Plan");
        addTier.addActionListener(e -> {
            try {
                showTierDialog(row.customerId(), null);
            } catch (Exception error) {
                showError("Tier Failed", error);
            }
        });
        editTier.addActionListener(e -> {
            DiscountTierRow selected = selectedTier(tierTable);
            if (selected == null) {
                UI.notifyInfo(this, "Select a tier first.");
                return;
            }
            try {
                showTierDialog(row.customerId(), selected);
            } catch (Exception error) {
                showError("Tier Failed", error);
            }
        });
        deleteTier.addActionListener(e -> {
            DiscountTierRow selected = selectedTier(tierTable);
            if (selected == null) {
                UI.notifyInfo(this, "Select a tier first.");
                return;
            }
            if (!UI.confirm(this, "Delete this discount tier?", "Confirm Delete")) {
                return;
            }
            try {
                customerService.deleteTier(selected.tierId());
                reload();
            } catch (Exception error) {
                showError("Delete Failed", error);
            }
        });
        deletePlan.addActionListener(e -> deleteDiscountPlan(row.customerId()));
        tab.add(UI.fullWidth(UI.buttonRow(addTier, editTier, deleteTier)));
        tab.add(UI.gap(8));
        tab.add(UI.fullWidth(UI.buttonRow(deletePlan)));
        return tab;
    }

    private JPanel buildReminderTab(CustomerRow row) {
        JPanel tab = UI.formCard();
        if (row.reminders().isEmpty()) {
            tab.add(UI.fullWidth(UI.emptyState("No reminders found for this customer.")));
            return tab;
        }

        UI.DataTable<ReminderRow> reminderTable = UI.table(
            UI.badgeCol("Type", ReminderRow::type, 96),
            UI.col("Amount", ReminderRow::amount, 96),
            UI.col("Due", ReminderRow::dueDate, 96),
            UI.col("Sent", ReminderRow::sentDate, 96),
            UI.col("Content", ReminderRow::content, 260)
        ).rows(row.reminders());

        reminderTable.table().setRowHeight(28);
        reminderTable.scroll().setAlignmentX(Component.LEFT_ALIGNMENT);
        int height = 34 + Math.max(1, row.reminders().size()) * 28;
        reminderTable.scroll().setPreferredSize(new Dimension(420, height));
        reminderTable.scroll().setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        tab.add(UI.fullWidth(reminderTable.scroll()));
        return tab;
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
            customer.getAccountStatus() != null ? customer.getAccountStatus() : AccountStatus.NORMAL,
            money(customer.getOutstandingBalance()),
            money(customer.getCreditLimit()),
            customer.getDiscountType(),
            customer.getFixedDiscountRate(),
            text(customer.getContactName()),
            text(customer.getPhone()),
            text(customer.getAddress()),
            customer.getLastPaymentDate() != null ? customer.getLastPaymentDate().toString() : "-",
            loadTiers(customer),
            loadReminders(customer.getCustomerId())
        );
    }

    private List<DiscountTierRow> loadTiers(Customer customer) {
        if (customer.getDiscountType() == DiscountType.FIXED) {
            BigDecimal rate = customer.getFixedDiscountRate() != null ? customer.getFixedDiscountRate() : BigDecimal.ZERO;
            return List.of(new DiscountTierRow(0, "Standard", BigDecimal.ZERO, rate));
        }

        try {
            List<DiscountTier> tiers = customerService.findTiers(customer.getCustomerId());
            if (tiers.isEmpty()) {
                return List.of();
            }
            return tiers.stream()
                .map(tier -> new DiscountTierRow(
                    tier.getTierId(),
                    text(tier.getTierName()),
                    safeMoney(tier.getMinMonthlySpend()),
                    safeMoney(tier.getDiscountRate())
                ))
                .toList();
        } catch (Exception error) {
            return List.of();
        }
    }

    private List<ReminderRow> loadReminders(int customerId) {
        try {
            List<Reminder> reminders = reminderDAO.findByCustomer(customerId);
            return reminders.stream()
                .map(reminder -> new ReminderRow(
                    reminder.getReminderType() != null ? reminder.getReminderType().name() : "-",
                    money(reminder.getAmountOwed()),
                    reminder.getDueDate() != null ? reminder.getDueDate().toString() : "-",
                    reminder.getSentAt() != null ? reminder.getSentAt().toString() : "-",
                    text(reminder.getContent())
                ))
                .toList();
        } catch (Exception error) {
            return List.of();
        }
    }

    private String discountLabel(CustomerRow row) {
        if (row.discountType() == null) {
            return "NONE";
        }
        if (row.discountType() == DiscountType.FIXED) {
            return "FIXED " + percent(row.fixedDiscountRate());
        }
        return "FLEXIBLE";
    }

    private void createCustomer() {
        JTextField name = new JTextField();
        JTextField contact = new JTextField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        JTextField creditLimit = new JTextField("500.00");
        JComboBox<DiscountType> discountType = new JComboBox<>(DiscountType.values());
        JTextField fixedRate = new JTextField("0.0300");

        discountType.addActionListener(e -> fixedRate.setEnabled(discountType.getSelectedItem() == DiscountType.FIXED));

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Contact"));
        form.add(contact);
        form.add(new JLabel("Phone"));
        form.add(phone);
        form.add(new JLabel("Address"));
        form.add(address);
        form.add(new JLabel("Credit Limit"));
        form.add(creditLimit);
        form.add(new JLabel("Discount Type"));
        form.add(discountType);
        form.add(new JLabel("Fixed Rate"));
        form.add(fixedRate);

        fixedRate.setEnabled(true);

        if (JOptionPane.showConfirmDialog(this, form, "New Customer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Customer customer = new Customer(name.getText().trim(), address.getText().trim(),
                DiscountType.valueOf(discountType.getSelectedItem().toString()));
            customer.setAccountNo("ACC" + System.currentTimeMillis());
            customer.setContactName(contact.getText().trim());
            customer.setPhone(phone.getText().trim());
            customer.setCreditLimit(parseMoney(creditLimit.getText().trim(), "Credit limit"));
            if (customer.getDiscountType() == DiscountType.FIXED) {
                customer.setFixedDiscountRate(parseRate(fixedRate.getText().trim(), "Fixed rate"));
            } else {
                customer.setFixedDiscountRate(null);
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
            JComboBox<DiscountType> discountType = new JComboBox<>(DiscountType.values());
            JTextField fixedRate = new JTextField(customer.getFixedDiscountRate() != null ? customer.getFixedDiscountRate().toPlainString() : "0.0300");

            discountType.setSelectedItem(customer.getDiscountType() != null ? customer.getDiscountType() : DiscountType.FIXED);
            fixedRate.setEnabled(discountType.getSelectedItem() == DiscountType.FIXED);
            discountType.addActionListener(e -> fixedRate.setEnabled(discountType.getSelectedItem() == DiscountType.FIXED));

            JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
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
            form.add(new JLabel("Discount Type"));
            form.add(discountType);
            form.add(new JLabel("Fixed Rate"));
            form.add(fixedRate);

            if (JOptionPane.showConfirmDialog(this, form, "Edit Customer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            customer.setName(name.getText().trim());
            customer.setContactName(contact.getText().trim());
            customer.setPhone(phone.getText().trim());
            customer.setAddress(address.getText().trim());
            customer.setCreditLimit(parseMoney(limit.getText().trim(), "Credit limit"));
            customer.setDiscountType(DiscountType.valueOf(discountType.getSelectedItem().toString()));
            if (customer.getDiscountType() == DiscountType.FIXED) {
                customer.setFixedDiscountRate(parseRate(fixedRate.getText().trim(), "Fixed rate"));
            } else {
                customer.setFixedDiscountRate(null);
            }
            customerService.update(customer);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void receivePayment(int customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return;
            }

            JTextField amount = new JTextField(customer.getOutstandingBalance() != null
                ? customer.getOutstandingBalance().toPlainString()
                : "0.00");
            JComboBox<String> cardType = new JComboBox<>(new String[]{"Visa", "Mastercard", "Amex"});
            JTextField cardNumber = new JTextField();
            JTextField expiry = new JTextField("12/2028");

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Amount"));
            form.add(amount);
            form.add(new JLabel("Card Type"));
            form.add(cardType);
            form.add(new JLabel("Card Number (16 digits)"));
            form.add(cardNumber);
            form.add(new JLabel("Expiry"));
            form.add(expiry);

            if (JOptionPane.showConfirmDialog(this, form, "Record Payment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            String number = cardNumber.getText().trim();
            if (!number.matches("\\d{16}")) {
                throw new IllegalArgumentException("Card number must be 16 digits");
            }

            String first4 = number.substring(0, 4);
            String last4 = number.substring(12);
            Customer updated = customerService.receivePayment(
                customerId,
                parseMoney(amount.getText().trim(), "Payment amount"),
                PaymentType.ACCOUNT_PAYMENT,
                cardType.getSelectedItem().toString(),
                first4,
                last4,
                expiry.getText().trim()
            );

            if (updated != null
                && updated.getOutstandingBalance() != null
                && updated.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0
                && updated.getAccountStatus() == AccountStatus.IN_DEFAULT) {
                if (SessionManager.getInstance().hasRole(UserRole.MANAGER)) {
                    if (UI.confirm(this, "Payment is cleared. Confirm restoring this customer to NORMAL?", "Manager Confirmation")) {
                        customerService.confirmDefaultedCustomer(customerId);
                        UI.notifySuccess(this, "Customer restored to NORMAL.");
                    } else {
                        UI.notifyInfo(this, "Customer remains IN_DEFAULT until a manager confirms.");
                    }
                } else {
                    UI.notifyInfo(this, "Payment recorded. A manager must confirm restoration.");
                }
            } else {
                UI.notifySuccess(this, "Payment recorded.");
            }
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Payment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmRestore(int customerId) {
        try {
            customerService.confirmDefaultedCustomer(customerId);
            UI.notifySuccess(this, "Customer restored to NORMAL.");
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Restore Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDiscountPlan(int customerId) {
        if (!UI.confirm(this, "Delete this discount plan?", "Delete Discount Plan")) {
            return;
        }
        try {
            customerService.deleteDiscountPlan(customerId);
            UI.notifySuccess(this, "Discount plan deleted.");
            reload();
        } catch (Exception error) {
            showError("Delete Failed", error);
        }
    }

    private void switchToFlexible(int customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return;
            }
            customer.setDiscountType(DiscountType.FLEXIBLE);
            customer.setFixedDiscountRate(null);
            customerService.update(customer);
            reload();
        } catch (Exception error) {
            showError("Update Failed", error);
        }
    }

    private void deleteCustomer(int customerId) {
        if (!UI.confirm(this, "Delete this customer?", "Confirm Delete")) {
            return;
        }

        try {
            customerService.delete(customerId);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTierDialog(int customerId, DiscountTierRow existing) throws Exception {
        boolean editing = existing != null;
        JTextField tierName = new JTextField(editing ? existing.tier() : "");
        JTextField minSpend = new JTextField(editing ? existing.minSpend().toPlainString() : "0.00");
        JTextField rate = new JTextField(editing ? existing.rate().toPlainString() : "0.0000");
        JLabel errorLabel = UI.errorLabel();

        JPanel form = UI.formCard();
        form.add(UI.fullWidth(UI.sectionLabel(editing ? "EDIT TIER" : "ADD TIER")));
        form.add(UI.gap(12));
        form.add(UI.formRow(
            UI.field("Tier Name", tierName),
            UI.field("Min Monthly Spend", minSpend)
        ));
        form.add(UI.formRow(
            UI.field("Discount Rate", rate),
            UI.field("Note", UI.dimLabel("Use decimal rates like 0.0200"))
        ));
        form.add(UI.gap(4));
        form.add(UI.fullWidth(errorLabel));
        form.add(UI.gap(12));

        JButton saveButton = UI.primaryButton(editing ? "Save Tier" : "Create Tier");
        JButton cancelButton = UI.button("Cancel");
        form.add(UI.fullWidth(UI.buttonRow(saveButton, cancelButton)));

        JDialog dialog = buildDialog(editing ? "Edit Discount Tier" : "Add Discount Tier", form);
        dialog.getRootPane().setDefaultButton(saveButton);
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            try {
                DiscountTier tier = new DiscountTier();
                tier.setCustomerId(customerId);
                tier.setTierName(tierName.getText().trim());
                tier.setMinMonthlySpend(parseMoney(minSpend.getText().trim(), "Min monthly spend"));
                tier.setDiscountRate(parseRate(rate.getText().trim(), "Discount rate"));
                if (editing) {
                    tier.setTierId(existing.tierId());
                    customerService.updateTier(tier);
                    UI.notifySuccess(this, "Discount tier updated.");
                } else {
                    customerService.saveTier(tier);
                    UI.notifySuccess(this, "Discount tier created.");
                }
                dialog.dispose();
                reload();
            } catch (Exception error) {
                errorLabel.setText(error.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private JDialog buildDialog(String title, JPanel form) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel page = UI.panel();
        page.add(form, BorderLayout.CENTER);
        dialog.setContentPane(page);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private DiscountTierRow selectedTier(UI.DataTable<DiscountTierRow> table) {
        int viewRow = table.table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return table.rowAtView(viewRow);
    }

    private void showError(String title, Exception error) {
        JOptionPane.showMessageDialog(this, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal parseMoney(String raw, String fieldName) {
        try {
            return new BigDecimal(raw.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception error) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private BigDecimal parseRate(String raw, String fieldName) {
        try {
            BigDecimal rate = new BigDecimal(raw.trim());
            if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException(fieldName + " must be between 0 and 1");
            }
            return rate.setScale(4, RoundingMode.HALF_UP);
        } catch (IllegalArgumentException error) {
            throw error;
        } catch (Exception error) {
            throw new IllegalArgumentException(fieldName + " must be a valid decimal");
        }
    }

    private String money(BigDecimal value) {
        BigDecimal safe = value != null ? value : BigDecimal.ZERO;
        return "£" + safe.setScale(2, RoundingMode.HALF_UP);
    }

    private String percent(BigDecimal rate) {
        BigDecimal safe = rate != null ? rate : BigDecimal.ZERO;
        return safe.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }

    private String text(String value) {
        return value != null ? value : "";
    }

    private String discountTypeName(DiscountType discountType) {
        return discountType != null ? discountType.name() : "NONE";
    }
}
