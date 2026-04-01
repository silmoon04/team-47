package ie.cortexx.gui;

import ie.cortexx.gui.customer.CustomerListPanel;
import ie.cortexx.gui.order.CataloguePanel;
import ie.cortexx.gui.order.OrderPanel;
import ie.cortexx.gui.reports.ReportPanel;
import ie.cortexx.gui.sales.POSPanel;
import ie.cortexx.gui.settings.SettingsPanel;
import ie.cortexx.gui.stock.StockPanel;
import ie.cortexx.gui.user.UserManagementPanel;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;

// main application window
// shows login first, then switches to tabs based on user role

public class MainFrame extends JFrame {
    private static String currentUser;
    private static String currentRole = "manager";
    private String activePageTitle;
    private boolean showingLogin = true;

    public MainFrame() {
        setTitle("IPOS-CA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        showLoginPanel();
    }

    // manages main frame
    public void showMainFrame(String role) {
        currentRole = role;
        showingLogin = false;

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        CardLayout cardLayout = new CardLayout();
        JPanel content = new JPanel(cardLayout);

        List<SidePanel.Page> pages = buildPages(role, content);
        SidePanel sidePanel = new SidePanel(
            this,
            pages,
            page -> {
                activePageTitle = page.title();
                cardLayout.show(content, page.title());
            },
            currentUser,
            currentRole,
            activePageTitle,
            this::logout
        );

        root.add(sidePanel, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        if (!pages.isEmpty()) {
            String target = activePageTitle != null ? activePageTitle : pages.get(0).title();
            cardLayout.show(content, target);
        }

        revalidate();
        repaint();
    }

    // manages login panel
    public void showLoginPanel() {
        showingLogin = true;
        getContentPane().removeAll();
        setContentPane(new LoginPanel(this));
        revalidate();
        repaint();
    }

    public void refreshTheme() {
        if (showingLogin) {
            showLoginPanel();
            return;
        }
        showMainFrame(currentRole);
    }

    private List<SidePanel.Page> buildPages(String role, JPanel content) {
        List<SidePanel.Page> pages = new ArrayList<>();

        if ("admin".equalsIgnoreCase(role)) {
            addPage(content, pages, "User Management", "icons/user-cog.svg", new JPanel());
            return pages;
        }

        addPage(content, pages, "Sales", "icons/shopping-cart.svg", new POSPanel());
        addPage(content, pages, "Stock", "icons/package.svg", new StockPanel());
        addPage(content, pages, "Customers", "icons/users.svg", new CustomerListPanel());
        addPage(content, pages, "Catalogue", "icons/book-open.svg", new CataloguePanel());
        addPage(content, pages, "Orders", "icons/truck.svg", new OrderPanel());

        if ("manager".equalsIgnoreCase(role)) {
            addPage(content, pages, "Reports", "icons/bar-chart-3.svg", new ReportPanel());
            addPage(content, pages, "Settings", "icons/settings.svg", new SettingsPanel());
        }
        return pages;
    }

    private void addPage(JPanel content, List<SidePanel.Page> pages,
                         String title, String iconPath, JComponent page) {
        pages.add(new SidePanel.Page(title, iconPath));
        content.add(page, title);
    }

    private void logout() {
        JOptionPane.showMessageDialog(this, "Successfully logged out of IPOS-CA.");
        activePageTitle = null;
        showLoginPanel();
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
