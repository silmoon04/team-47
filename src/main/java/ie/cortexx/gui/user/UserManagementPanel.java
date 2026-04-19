package ie.cortexx.gui.user;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.User;
import ie.cortexx.service.AuthService;
import ie.cortexx.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// crud for user accounts (admin only)
public class UserManagementPanel extends JPanel {
    private record UserRow(int id, String username, String fullName, String role, String status) {}
    private final List<UserRow> rows = new ArrayList<>();
    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService(userDAO);
    private UI.DataTable<UserRow> table;

    public UserManagementPanel() {
        UI.applyPanel(this);
        reload();
    }

    private void reload() {
        removeAll();
        rows.clear();
        List<User> users = loadUsers();
        for (User user : users) {
            rows.add(toRow(user));
        }

        JPanel stats = buildStats(users);

        table = UI.table(
            UI.monoCol("ID", UserRow::id),
            UI.col("Username", UserRow::username),
            UI.col("Full Name", UserRow::fullName),
            UI.badgeCol("Role", UserRow::role),
            UI.badgeCol("Status", UserRow::status)
        );
        table.rows(rows);
        JButton createButton = UI.iconButton("Create User", "icons/users.svg", true);
        JButton editButton = UI.iconButton("Edit User", "icons/user-cog.svg", false);
        JButton deleteButton = UI.dangerButton("Delete User");
        JPanel toolbar = UI.toolbar("Search Users", table.table(), createButton, editButton, deleteButton);
        createButton.addActionListener(e -> createUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());

        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void createUser() {
        showUserDialog(null);
    }

    private void editUser() {
        User user = selectedUser();
        if (user == null) {
            UI.notifyInfo(this, "Select a user first.");
            return;
        }
        showUserDialog(user);
    }

    private void deleteUser() {
        User user = selectedUser();
        if (user == null) {
            UI.notifyInfo(this, "Select a user first.");
            return;
        }
        if (!UI.confirm(this, "Delete this user account?", "Confirm Delete")) {
            return;
        }

        try {
            userService.delete(user.getUserId());
            UI.notifySuccess(this, "User deleted.");
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserDialog(User existing) {
        boolean editing = existing != null;
        JTextField username = UI.inputField("Username");
        username.setText(editing ? existing.getUsername() : "");
        username.setEnabled(!editing);

        JTextField fullName = UI.inputField("Full Name");
        fullName.setText(editing ? text(existing.getFullName()) : "");

        JPasswordField password = UI.passwordField("Password");
        JComboBox<String> role = new JComboBox<>(editing
            ? new String[]{"ADMIN", "MANAGER", "PHARMACIST"}
            : new String[]{"MANAGER", "PHARMACIST"});
        JComboBox<String> status = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        JLabel errorLabel = UI.errorLabel();

        if (editing) {
            role.setSelectedItem(existing.getRole().name());
            status.setSelectedItem(existing.isActive() ? "ACTIVE" : "INACTIVE");
        }

        JPanel form = UI.formCard();
        form.add(UI.fullWidth(UI.sectionLabel(editing ? "EDIT USER" : "CREATE USER")));
        form.add(UI.gap(12));
        form.add(UI.formRow(
            UI.field("Username", username),
            UI.field("Role", role)
        ));
        form.add(UI.formRow(
            UI.field("Full Name", fullName),
            editing ? UI.field("Status", status) : UI.field("Password", password)
        ));
        form.add(UI.gap(4));
        form.add(UI.fullWidth(errorLabel));
        form.add(UI.gap(12));

        JButton saveButton = UI.primaryButton(editing ? "Save Changes" : "Create User");
        JButton cancelButton = UI.button("Cancel");
        form.add(UI.fullWidth(UI.buttonRow(saveButton, cancelButton)));

        JDialog dialog = buildDialog(editing ? "Edit User Account" : "Create User Account", form);
        dialog.getRootPane().setDefaultButton(saveButton);
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> submitUserForm(existing, username, fullName, password, role, status, errorLabel, dialog));
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
        dialog.setMinimumSize(new Dimension(480, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private void submitUserForm(
        User existing,
        JTextField username,
        JTextField fullName,
        JPasswordField password,
        JComboBox<String> role,
        JComboBox<String> status,
        JLabel errorLabel,
        JDialog dialog
    ) {
        String trimmedUsername = username.getText().trim();
        String trimmedFullName = fullName.getText().trim();
        String rawPassword = new String(password.getPassword()).trim();

        if (trimmedUsername.isEmpty() || trimmedFullName.isEmpty()) {
            errorLabel.setText("Please enter username and full name.");
            return;
        }
        if (existing == null && rawPassword.isEmpty()) {
            errorLabel.setText("Please enter a password.");
            return;
        }

        try {
            if (existing == null) {
                User user = new User(
                    trimmedUsername,
                    AuthService.hashPassword(rawPassword),
                    trimmedFullName,
                    UserRole.valueOf(role.getSelectedItem().toString())
                );
                userDAO.save(user);
                UI.notifySuccess(this, "User created.");
            } else {
                existing.setFullName(trimmedFullName);
                existing.setRole(UserRole.valueOf(role.getSelectedItem().toString()));
                existing.setActive("ACTIVE".equals(status.getSelectedItem()));
                userDAO.update(existing);
                UI.notifySuccess(this, "User updated.");
            }
            dialog.dispose();
            reload();
        } catch (Exception error) {
            errorLabel.setText(error.getMessage());
        }
    }

    private List<User> loadUsers() {
        try {
            return userDAO.findAll().stream().filter(User::isActive).toList();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private JPanel buildStats(List<User> users) {
        long activeUsers = users.stream().filter(User::isActive).count();
        long admins = users.stream().filter(user -> user.getRole() == UserRole.ADMIN && user.isActive()).count();
        long managers = users.stream().filter(user -> user.getRole() == UserRole.MANAGER && user.isActive()).count();
        long pharmacists = users.stream().filter(user -> user.getRole() == UserRole.PHARMACIST && user.isActive()).count();

        return UI.stats(
            UI.stat("Active Users", String.valueOf(activeUsers), UI.ACCENT, "icons/users.svg"),
            UI.stat("Admins", String.valueOf(admins), UI.GREEN, "icons/user-cog.svg"),
            UI.stat("Managers", String.valueOf(managers), UI.PURPLE, "icons/user-cog.svg"),
            UI.stat("Pharmacists", String.valueOf(pharmacists), UI.ORANGE, "icons/user-cog.svg")
        );
    }

    private UserRow toRow(User user) {
        return new UserRow(
            user.getUserId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole().name(),
            user.isActive() ? "ACTIVE" : "INACTIVE"
        );
    }

    private User selectedUser() {
        int viewRow = table.table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.table().convertRowIndexToModel(viewRow);
        try {
            return userDAO.findById(rows.get(modelRow).id());
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private String text(String value) {
        return value != null ? value : "";
    }
}
