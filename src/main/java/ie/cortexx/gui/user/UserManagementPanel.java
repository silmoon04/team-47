package ie.cortexx.gui.user;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// CRUD for user accounts (admin only)
public class UserManagementPanel extends JPanel {
    private record UserRow(String id, String username, String fullName, String role, String status) {}

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
        ).rows(List.of(
            new UserRow("1", "sysdba", "System Admin", "ADMIN", "ACTIVE"),
            new UserRow("2", "manager", "Store Manager", "MANAGER", "ACTIVE"),
            new UserRow("3", "pharmacist1", "Senior Pharmacist", "PHARMACIST", "ACTIVE"),
            new UserRow("4", "pharmacist2", "Relief Pharmacist", "PHARMACIST", "ACTIVE")
        ));

        JPanel toolbar = UI.toolbar("Search users...", table.table(), "+ Create User");
        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
    }
}
