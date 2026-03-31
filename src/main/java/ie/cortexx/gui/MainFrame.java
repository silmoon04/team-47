package ie.cortexx.gui;

import ie.cortexx.gui.customer.CustomerListPanel;
import ie.cortexx.gui.order.CataloguePanel;
import ie.cortexx.gui.order.OrderPanel;
import ie.cortexx.gui.reports.ReportPanel;
import ie.cortexx.gui.sales.POSPanel;
import ie.cortexx.gui.settings.SettingsPanel;
import ie.cortexx.gui.stock.StockPanel;

import javax.swing.*;
import java.awt.*;

// main application window
// shows login first, then switches to tabs based on user role

public class MainFrame extends JFrame {
    private static String currentUser;
    private static String currentRole = "manager";

    public MainFrame() {
        setTitle("IPOS-CA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        showLoginPanel();
    }

    // manages main frame
    public void showMainFrame(String role) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(showHeaders(), BorderLayout.NORTH);
        add(showTabs(role), BorderLayout.CENTER); // show tabs based off role
        revalidate();
        repaint();
    }

    // manages login panel
    public void showLoginPanel() {
        getContentPane().removeAll();
        setContentPane(new LoginPanel(this));
        revalidate();
        repaint();
    }

    // manages headers
    private JPanel showHeaders() {
        JPanel headers = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel(currentUser.toUpperCase());
        JLabel roleLabel = new JLabel(currentRole.toUpperCase());
        JButton logoutButton = new JButton("Logout");
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        rightPanel.add(userLabel);
        rightPanel.add(roleLabel);
        rightPanel.add(logoutButton);

        headers.add(leftPanel, BorderLayout.WEST);
        headers.add(rightPanel, BorderLayout.EAST);

        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Successfully logged out of IPOS-CA.");
            // prompt user back to login page
            showLoginPanel();
        });
        return headers;
    }

    // manages tabs
    private Component showTabs(String role) {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        // admin tabs
        if (role.equals("admin")) {
            tabs.addTab("User Management", new JPanel());
        } else {
            // default tabs
            tabs.addTab("Sales", new POSPanel());
            tabs.addTab("Stock", new StockPanel());
            tabs.addTab("Customers", new CustomerListPanel());
            tabs.addTab("Catalogue", new CataloguePanel());
            tabs.addTab("Orders", new OrderPanel());
            // manager tabs
            if (role.equals("manager")) {
                tabs.addTab("Reports", new ReportPanel());
                tabs.addTab("Settings", new SettingsPanel());
            }
        }
        return tabs;
    }

    // getters and setters
    public String getUsername() {
        return currentUser;
    }

    public String getRole() {
        return currentRole;
    }

    public static void setUsername(String username) {
        currentUser = username;
    }

    public static void setRole(String role) {
        currentRole = role;
    }
}
