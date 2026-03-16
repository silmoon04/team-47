package ie.cortexx.gui;

import javax.swing.*;

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

        setContentPane(new LoginPanel(this));
    }

    public void showMainFrame() {
        JTabbedPane tabs = new JTabbedPane();
        // placeholder tabs
        tabs.addTab("Tab 1", new JPanel());
        tabs.addTab("Tab 2", new JPanel());
        tabs.addTab("Tab 3", new JPanel());

        setContentPane(tabs);
        revalidate();
    }


}
