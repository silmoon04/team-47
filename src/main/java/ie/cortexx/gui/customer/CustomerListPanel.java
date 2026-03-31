package ie.cortexx.gui.customer;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
    private JPanel detailPanel;

    public CustomerListPanel() {
        setLayout(new GridLayout(1, 2, 24, 0));
        setBackground(UI.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- left: customer table ---
        var t = UI.table("Account", "Name", "Status", "Balance", "Limit", "Discount");
        t.monoColumn(0).badgeColumn(2).monoColumn(3).monoColumn(4).badgeColumn(5);
        // TODO: swap with CustomerDAO.findAll() loop
        t.model().addRow(new Object[]{"ACC0001", "Ms Eva Bauyer", "NORMAL", "£0.00", "£500.00", "FIXED"});
        t.model().addRow(new Object[]{"ACC0002", "Mr Glynne Morrison", "NORMAL", "£0.00", "£500.00", "FLEXIBLE"});

        JPanel left = UI.transparentPanel(12);
        left.add(UI.toolbar("Search customers...", t.table(), "+ New Customer"), BorderLayout.NORTH);
        left.add(t.scroll(), BorderLayout.CENTER);

        // click row to show detail on the right
        t.table().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.table().getSelectedRow();
            if (row < 0) return;
            row = t.table().convertRowIndexToModel(row);
            showDetail(
                (String) t.model().getValueAt(row, 0),
                (String) t.model().getValueAt(row, 1),
                (String) t.model().getValueAt(row, 2),
                (String) t.model().getValueAt(row, 3),
                (String) t.model().getValueAt(row, 4),
                (String) t.model().getValueAt(row, 5));
        });
        add(left);

        // --- right: starts with empty state ---
        detailPanel = UI.transparentPanel(0);
        detailPanel.add(UI.emptyState("Select a customer to view details"));
        add(detailPanel);
    }

    // swaps the right side to show selected customer info
    private void showDetail(String acc, String name, String status,
                            String balance, String limit, String discount) {
        detailPanel.removeAll();
        JPanel info = UI.formCard();
        info.add(UI.heading(name));
        info.add(UI.gap(12));
        info.add(UI.detailRow("Account No", acc));
        info.add(UI.detailRow("Status", status));
        info.add(UI.detailRow("Outstanding Balance", balance));
        info.add(UI.detailRow("Credit Limit", limit));
        info.add(UI.detailRow("Discount Type", discount));
        info.add(UI.gap(16));
        info.add(UI.buttonRow(UI.button("Edit"), UI.button("Receive Payment"), UI.dangerButton("Delete")));
        detailPanel.add(info, BorderLayout.NORTH);
        detailPanel.revalidate();
        detailPanel.repaint();
    }
}
