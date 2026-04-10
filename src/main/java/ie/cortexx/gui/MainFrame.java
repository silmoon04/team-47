package ie.cortexx.gui;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.gui.customer.CustomerListPanel;
import ie.cortexx.gui.order.CataloguePanel;
import ie.cortexx.gui.order.OrderPanel;
import ie.cortexx.gui.reports.ReportPanel;
import ie.cortexx.gui.sales.POSPanel;
import ie.cortexx.gui.settings.SettingsPanel;
import ie.cortexx.gui.stock.StockPanel;
import ie.cortexx.gui.user.UserManagementPanel;
import ie.cortexx.service.AuthService;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// main application window
// shows login first, then switches to tabs based on user role

public class MainFrame extends JFrame {
    private final AuthService authService = new AuthService(new UserDAO());
    private UserSession session = UserSession.anonymous();
    private String activePageTitle;
    private boolean showingLogin = true;

    public MainFrame() {
        setTitle("IPOS-CA");
        setAppIcon();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        showLoginPanel();
    }

    public void showMainFrame(String role) {
        session = session.withRole(role);
        showMainFrame();
    }

    private void showMainFrame() {
        showingLogin = false;
        String role = session.role();
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        CardLayout cardLayout = new CardLayout();
        JPanel content = new JPanel(cardLayout);
        Map<String, JComponent> pageComponents = new LinkedHashMap<>();

        List<SidePanel.Page> pages = buildPages(role, content, pageComponents);
        SidePanel sidePanel = new SidePanel(this, pages, page -> {
                activePageTitle = page.title();
            refreshPage(pageComponents.get(page.title()));
                cardLayout.show(content, page.title());
            },
            session.username(),
            session.role(),
            activePageTitle,
            this::logout
        );
        root.add(sidePanel, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        if (!pages.isEmpty()) {
            String target = activePageTitle != null ? activePageTitle : pages.get(0).title();
            refreshPage(pageComponents.get(target));
            cardLayout.show(content, target);
        }
        revalidate();
        repaint();
    }

    public void showLoginPanel() {
        showingLogin = true;
        getContentPane().removeAll();
        setContentPane(new LoginPanel(this, authService));
        revalidate();
        repaint();
    }

    public void refreshTheme() {
        if (showingLogin) {
            showLoginPanel();
            return;
        }
        showMainFrame();
    }

    private void setAppIcon() {
        var url = getClass().getResource("/icons/cortexx_logo.png");
        if (url == null) return;
        Image image = new ImageIcon(url).getImage();
        setIconImage(image);
    }

    private List<SidePanel.Page> buildPages(String role, JPanel content, Map<String, JComponent> pageComponents) {
        List<SidePanel.Page> pages = new ArrayList<>();

        // admin tabs
        if ("admin".equalsIgnoreCase(role)) {
            addPage(content, pages, pageComponents, "User Management", "icons/user-cog.svg", new UserManagementPanel());
            return pages;
        }
        // pharmacist tabs
        addPage(content, pages, pageComponents, "Sales", "icons/shopping-cart.svg", new POSPanel());
        addPage(content, pages, pageComponents, "Stock", "icons/package.svg", new StockPanel());
        addPage(content, pages, pageComponents, "Customers", "icons/users.svg", new CustomerListPanel());
        addPage(content, pages, pageComponents, "Catalogue", "icons/book-open.svg", new CataloguePanel());
        addPage(content, pages, pageComponents, "Orders", "icons/truck.svg", new OrderPanel());
        // manager tabs
        if ("manager".equalsIgnoreCase(role)) {
            addPage(content, pages, pageComponents, "Reports", "icons/bar-chart-3.svg", new ReportPanel());
            addPage(content, pages, pageComponents, "Settings", "icons/settings.svg", new SettingsPanel());
        }
        return pages;
    }

    private void addPage(JPanel content, List<SidePanel.Page> pages, Map<String, JComponent> pageComponents, String title, String iconPath, JComponent page) {
        pages.add(new SidePanel.Page(title, iconPath));
        pageComponents.put(title, page);
        content.add(page, title);
    }

    private void refreshPage(JComponent page) {
        if (page instanceof RefreshablePage refreshablePage) {
            refreshablePage.refreshPage();
        }
    }

    public void login(String username, String role) {
        session = new UserSession(username, role);
        showMainFrame();
    }

    private void logout() {
        authService.logout();
        JOptionPane.showMessageDialog(this, "Successfully logged out of IPOS-CA.");
        session = UserSession.anonymous();
        activePageTitle = null;
        showLoginPanel();
    }

    public String getUsername() {
        return session.username();
    }

    public String getRole() {
        return session.role();
    }
}
