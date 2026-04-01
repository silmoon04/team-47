package ie.cortexx.gui.customer;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*

50/50 split using GridLayout(1, 2) so the table and detail sit side by side.
we dont use UI.splitPanel() here bc that fixes the right side width.

left side: toolbar + customer table. clicking a row shows detail on the right.
right side: starts with emptyState, swaps to formCard with detailRows.

getValueIsAdjusting() filters intermediate events (fires twice otherwise).
convertRowIndexToModel() needed bc table might be filtered via search.

placeholder data for now, swap with CustomerDAO.findAll() later.
*/

// table of all customers with detail panel on the right
public class CustomerListPanel extends JPanel {
    private final JPanel detailPanel = UI.transparentPanel(0);

    private record CustomerRow(
        String account,
        String name,
        String status,
        String balance,
        String limit,
        String discount
    ) {}

    public CustomerListPanel() {
        UI.applyPanel(this);

        var customers = UI.table(
            UI.monoCol("Account", CustomerRow::account),
            UI.col("Name", CustomerRow::name),
            UI.badgeCol("Status", CustomerRow::status),
            UI.monoCol("Balance", CustomerRow::balance),
            UI.monoCol("Limit", CustomerRow::limit),
            UI.badgeCol("Discount", CustomerRow::discount)
        ).rows(List.of(
            new CustomerRow("ACC0001", "Ms Eva Bauyer", "NORMAL", "£0.00", "£500.00", "FIXED"),
            new CustomerRow("ACC0002", "Mr Glynne Morrison", "NORMAL", "£0.00", "£500.00", "FLEXIBLE")
        )).onSelect(this::showDetail);

        JPanel left = UI.toolbarAndTable(
            UI.toolbar("Search customers...", customers.table(), "+ New Customer"),
            customers.scroll()
        );

        UI.swap(detailPanel, UI.emptyState("Select a customer to view details"));
        add(UI.twoColumn(left, detailPanel, 24), BorderLayout.CENTER);
    }

    private void showDetail(CustomerRow row) {
        UI.swap(detailPanel, UI.detailCard(
            row.name(),
            new UI.Detail[]{
                UI.detail("Account No", row.account()),
                UI.detail("Status", row.status()),
                UI.detail("Outstanding Balance", row.balance()),
                UI.detail("Credit Limit", row.limit()),
                UI.detail("Discount Type", row.discount())
            },
            UI.button("Edit"),
            UI.button("Receive Payment"),
            UI.dangerButton("Delete")
        ));
    }
}
