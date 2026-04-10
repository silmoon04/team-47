package ie.cortexx.gui.user;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.gui.util.UI;
import ie.cortexx.model.User;
import ie.cortexx.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// crud for user accounts (admin only)
public class UserManagementPanel extends JPanel {
    private record UserRow(int id, String username, String fullName, String role, String status) {}
    private final List<UserRow> rows = new ArrayList<>();
    private final UserDAO userDAO = new UserDAO();
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
        JTextField search = UI.searchField("Search Users", table.table());
        JButton createButton = UI.primaryButton("+ Create User");
        JButton editButton = UI.button("Edit User");
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        actions.add(createButton);
        actions.add(editButton);
        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        createButton.addActionListener(e -> createUser());
        editButton.addActionListener(e -> editUser());

        add(UI.pageWithStats(stats, toolbar, table.scroll()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void createUser() {
        JDialog createUserDialogue = new JDialog((Frame) null, "Create User Account", true);
        createUserDialogue.setSize(360, 240);
        createUserDialogue.setLocationRelativeTo(this);
        createUserDialogue.setLayout(new BorderLayout(10, 10));
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JTextField username = new JTextField();
        JTextField fullName = new JTextField();
        JPasswordField password = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[]{"MANAGER", "PHARMACIST"});
        JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        JPanel fields = new JPanel(new GridLayout(4, 2, -50, 10));
        fields.add(new JLabel("Username", SwingConstants.CENTER));
        fields.add(username);
        fields.add(new JLabel("Full Name", SwingConstants.CENTER));
        fields.add(fullName);
        fields.add(new JLabel("Password", SwingConstants.CENTER));
        fields.add(password);
        fields.add(new JLabel("Role", SwingConstants.CENTER));
        fields.add(role);
        JButton createButton = new JButton("Create User");
        createUserDialogue.add(fields, BorderLayout.CENTER);
        createButton.addActionListener(e -> {
            String trimmedUsername = username.getText().trim();
            String trimmedFullName = fullName.getText().trim();
            String rawPassword = new String(password.getPassword()).trim();

            if (trimmedUsername.isEmpty() || trimmedFullName.isEmpty() || rawPassword.isEmpty()) {
                errorLabel.setText("Please enter username, full name, and password.");
                return;
            }
            try {
                User user = new User(
                    trimmedUsername,
                    AuthService.hashPassword(rawPassword),
                    trimmedFullName,
                    UserRole.valueOf(role.getSelectedItem().toString())
                );
                userDAO.save(user);
                createUserDialogue.dispose();
                reload();
            } catch (Exception error) {
                errorLabel.setText(error.getMessage());
            }
        });
        bottomPanel.add(errorLabel, BorderLayout.NORTH);
        bottomPanel.add(createButton, BorderLayout.CENTER);
        createUserDialogue.add(bottomPanel, BorderLayout.SOUTH);
        createUserDialogue.setVisible(true);
    }

    private void editUser() {
        User user = selectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        JTextField fullName = new JTextField(user.getFullName());
        JComboBox<String> role = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "PHARMACIST"});
        role.setSelectedItem(user.getRole().name());
        JCheckBox active = new JCheckBox("Active", user.isActive());

        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Username"));
        fields.add(new JLabel(user.getUsername()));
        fields.add(new JLabel("Full Name"));
        fields.add(fullName);
        fields.add(new JLabel("Role"));
        fields.add(role);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(fields, BorderLayout.CENTER);
        wrapper.add(active, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, wrapper, "Edit User", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            user.setFullName(fullName.getText().trim());
            user.setRole(UserRole.valueOf(role.getSelectedItem().toString()));
            user.setActive(active.isSelected());
            userDAO.update(user);
            reload();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<User> loadUsers() {
        try {
            return userDAO.findAll();
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
            UI.stat("Active Users", String.valueOf(activeUsers), UI.ACCENT),
            UI.stat("Admins", String.valueOf(admins), UI.GREEN),
            UI.stat("Managers", String.valueOf(managers), UI.PURPLE),
            UI.stat("Pharmacists", String.valueOf(pharmacists), UI.ORANGE)
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
}
