package ie.cortexx.gui;

import javax.swing.*;
import java.awt.*;

// main application window
// shows login first, then switches to tabs based on user role
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("IPOS-CA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // TODO: show LoginPanel initially *
        // TODO: on login success, replace content with JTabbedPane
        // TODO: add/hide tabs based on role (pharmacist/admin/manager)

        setLayout(new BorderLayout());
        add(showHeader(), BorderLayout.NORTH);
        add(showTab(), BorderLayout.CENTER);
    }

    private JPanel showHeader() {
        JPanel headers = new JPanel(new BorderLayout());

        JLabel userLabel = new JLabel("USER");
        JLabel roleLabel = new JLabel("ROLE");
        JButton logoutButton = new JButton("Logout");

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        rightPanel.add(userLabel);
        rightPanel.add(roleLabel);
        rightPanel.add(logoutButton);

        headers.add(leftPanel, BorderLayout.WEST);
        headers.add(rightPanel, BorderLayout.EAST);

        logoutButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Successfully logged out of IPOS-CA."));
        // prompt user back to login page
        return headers;
    }

    // TODO: add role based tabs
    private Component showTab() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.addTab("Order", new JPanel());
        tabs.addTab("Customer", new JPanel());
        tabs.addTab("Stock", new JPanel());
        tabs.addTab("Reports", new JPanel());
        tabs.addTab("Settings", new JPanel());
        return tabs;
    }
}
