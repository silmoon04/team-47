package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Dimension;

public final class ThemeSwitchButton {

    private ThemeSwitchButton() {}

    public static JButton create(MainFrame frame) {
        JButton button = new JButton(UI.icon(
            UI.isDarkTheme() ? "icons/sun.svg" : "icons/moon.svg",
            18,
            UI.TEXT
        ));
        button.putClientProperty("FlatLaf.styleClass", "themeSwitch");
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(44, 44));
        button.setToolTipText(UI.isDarkTheme() ? "Switch to light mode" : "Switch to dark mode");
        button.addActionListener(e -> {
            UI.toggleTheme();
            frame.refreshTheme();
        });
        return button;
    }
}
