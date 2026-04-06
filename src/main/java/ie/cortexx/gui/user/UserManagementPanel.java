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
    private UI.DataTable<UserRow> table;

    public UserManagementPanel() {
        UI.applyPanel(this);

        JPanel stats = UI.stats(
            UI.stat("Active Users", "4", UI.ACCENT),
            UI.stat("Admins", "1", UI.GREEN),
            UI.stat("Managers", "1", UI.PURPLE),
            UI.stat("Pharmacists", "2", UI.ORANGE)
        );

        table = UI.table(
            UI.monoCol("ID", UserRow::id),
            UI.col("Username", UserRow::username),
            UI.col("Full Name", UserRow::fullName),
            UI.badgeCol("Role", UserRow::role),
            UI.badgeCol("Status", UserRow::status)
        );
        // TODO: replace hardcoded rows/stats with UserDAO.findAll() so admin CRUD reflects the real database.
        // user account data
        rows.add(new UserRow(1, "sysdba", "System Admin", "ADMIN", "ACTIVE"));
        rows.add(new UserRow(2, "manager", "Store Manager", "MANAGER", "ACTIVE"));
        rows.add(new UserRow(3, "pharmacist1", "Senior Pharmacist", "PHARMACIST", "ACTIVE"));
        rows.add(new UserRow(4, "pharmacist2", "Relief Pharmacist", "PHARMACIST", "ACTIVE"));
        // add data to table
        table.rows(rows);
        // buttons and searchbar
        JTextField search = UI.searchField("Search Users", table.table());
        JButton createButton = UI.primaryButton("+ Create User");
        JButton editButton = UI.button("Edit User");
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        // TODO: wire edit/deactivate/delete role management through UserDAO/AuthService instead of local list mutation.

        actions.add(createButton);
        actions.add(editButton);
        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        createButton.addActionListener(e -> createUser());

        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
    }

    // manages create user panel
    private void createUser() {
        // dialogue
        JDialog createUserDialogue = new JDialog((Frame) null, "Create User Account", true);
        createUserDialogue.setSize(325, 200);
        createUserDialogue.setLocationRelativeTo(this);
        createUserDialogue.setLayout(new BorderLayout(10, 10));
        // labels
        JPanel createUserButton = new JPanel(new BorderLayout(5, 5));
        JTextField username = new JTextField();
        JTextField fullName = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"MANAGER", "PHARMACIST"});
        JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        // text fields
        JPanel fields = new JPanel(new GridLayout(3, 2, -50, 10));
        fields.add(new JLabel("Username", SwingConstants.CENTER));
        fields.add(username);
        fields.add(new JLabel("Full Name", SwingConstants.CENTER));
        fields.add(fullName);
        fields.add(new JLabel("Role", SwingConstants.CENTER));
        fields.add(role);
        // create user button
        JButton createButton = new JButton("Create User");
        createUserDialogue.add(fields, BorderLayout.CENTER);
        createButton.addActionListener(e -> {
            if (username.getText().isEmpty() || fullName.getText().isEmpty()) {
                errorLabel.setText("Please enter both username and full name.");
                return;
            }
            // TODO: add user to account database
            createUserAccount(username.getText(), fullName.getText(), role.getSelectedItem().toString());
            createUserDialogue.dispose();
        });
        createUserButton.add(errorLabel, BorderLayout.NORTH);
        createUserButton.add(createButton, BorderLayout.CENTER);
        createUserDialogue.add(createUserButton, BorderLayout.SOUTH);
        createUserDialogue.setVisible(true);
    }
    // manages create user account
    void createUserAccount(String username, String fullName, String role) {
        int userId = rows.size() + 1;
        UserRow newUser = new UserRow(userId, username, fullName, role, "ACTIVE");
        rows.add(newUser);
        table.rows(rows);
    }
}
