package ie.cortexx.gui.user;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

// CRUD for user accounts (admin only)
public class UserManagementPanel extends JPanel {
    private record UserRow(int id, String username, String fullName, String role, String status) {}
    private final List<UserRow> rows = new ArrayList<>();

    public UserManagementPanel() {
        UI.applyPanel(this);

        JPanel stats = UI.stats(
            UI.stat("Active Users", "4", UI.ACCENT),
            UI.stat("Admins", "1", UI.GREEN),
            UI.stat("Managers", "1", UI.PURPLE),
            UI.stat("Pharmacists", "2", UI.ORANGE)
        );

        var table = UI.table(
            UI.monoCol("ID", UserRow::id),
            UI.col("Username", UserRow::username),
            UI.col("Full Name", UserRow::fullName),
            UI.badgeCol("Role", UserRow::role),
            UI.badgeCol("Status", UserRow::status)
        );

        // user account data
        rows.add(new UserRow(1, "sysdba", "System Admin", "ADMIN", "ACTIVE"));
        rows.add(new UserRow(2, "manager", "Store Manager", "MANAGER", "ACTIVE"));
        rows.add(new UserRow(3, "pharmacist1", "Senior Pharmacist", "PHARMACIST", "ACTIVE"));
        rows.add(new UserRow(4, "pharmacist2", "Relief Pharmacist", "PHARMACIST", "ACTIVE"));
        // add data to table
        table.rows(rows);

        JTextField search = UI.searchField("Search Users", table.table());
        JButton createButton = UI.primaryButton("+ Create User");
        JButton editButton = UI.button("Edit User");

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(search, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(createButton);
        actions.add(editButton);

        toolbar.add(actions, BorderLayout.EAST);
        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
    }
}
