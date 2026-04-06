package ie.cortexx.gui.customer;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// Customer list on the left with a denser profile card and discount tiers on the right.
public class CustomerListPanel extends JPanel {
    private final JPanel detailPanel = UI.transparentPanel(0);

    private record DiscountTierRow(String tier, String minSpend, String rate) {}

    private record CustomerRow(
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

        // TODO: replace demo rows with CustomerDAO + discount-tier queries so customer balances/status stay live.
        List<CustomerRow> rows = List.of(
            new CustomerRow(
                "ACC0001",
                "Ms Eva Bauyer",
                "NORMAL",
                "£0.00",
                "£500.00",
                "FIXED 3%",
                "Ms Eva Bauyer",
                "0207 321 8001",
                "1, Liverpool Street, London EC2V 8NS",
                "-",
                List.of(new DiscountTierRow("Standard", "£0.00", "3%"))
            ),
            new CustomerRow(
                "ACC0002",
                "Mr Glynne Morrison",
                "NORMAL",
                "£0.00",
                "£500.00",
                "FLEXIBLE",
                "Ms Glynne Morrison",
                "0207 321 8001",
                "1, Liverpool Street, London EC2V 8NS",
                "2026-03-29",
                List.of(
                    new DiscountTierRow("Under £100", "£0.00", "0%"),
                    new DiscountTierRow("£100 - £300", "£100.00", "1%"),
                    new DiscountTierRow("Over £300", "£300.00", "2%")
                )
            )
        );

        var customers = UI.table(
            UI.col("Account", CustomerRow::account, 96),
            UI.col("Name", CustomerRow::name, 180),
            UI.badgeCol("Status", CustomerRow::status, 86),
            UI.col("Balance", CustomerRow::balance, 84),
            UI.col("Credit Limit", CustomerRow::limit, 96),
            UI.badgeCol("Discount", CustomerRow::discount, 92)
        ).rows(rows).onSelect(this::showDetail);

        JPanel left = UI.toolbarAndTable(
            UI.toolbar("Search customers...", customers.table(), "+ New Customer"),
            customers.scroll()
        );

        add(UI.splitPanel(left, detailPanel, 460), BorderLayout.CENTER);

        if (!rows.isEmpty()) {
            customers.table().setRowSelectionInterval(0, 0);
            showDetail(rows.get(0));
        } else {
            UI.swap(detailPanel, UI.emptyState("Select a customer to view details"));
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

        JLabel name = UI.monoLabelBold(row.name(), 14f, UI.TEXT);

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
        // TODO: wire edit/payment/delete actions through CustomerService + Payment/Reminder flows instead of static buttons.
        edit.setFont(UI.FONT_MONO_BOLD);
        receivePayment.setFont(UI.FONT_MONO_BOLD);
        delete.setFont(UI.FONT_MONO_BOLD);
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

}
