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
            tabs.addTab("Sales", new JPanel());
            tabs.addTab("Stock", new JPanel());
            tabs.addTab("Customers", new JPanel());
            tabs.addTab("Catalogue", new JPanel());
            tabs.addTab("Orders", new JPanel());
            // manager tabs
            if (role.equals("manager")) {
                tabs.addTab("Reports", new JPanel());
                tabs.addTab("Settings", new JPanel());
            }
        }
        return tabs;
    }
}
