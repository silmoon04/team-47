package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Dimension;

public final class ThemeSwitchButton {

    private ThemeSwitchButton() {}

    public static JButton create(MainFrame frame) {
        JButton button = new JButton(UI.theme().name());
        button.putClientProperty("FlatLaf.styleClass", "themeSwitch");
        button.setFont(UI.FONT_MONO_BOLD);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 44));
        button.setToolTipText("Choose theme (" + UI.theme().label() + ")");
        button.addActionListener(e -> showMenu(button, frame));
        return button;
    }

    private static void showMenu(JButton button, MainFrame frame) {
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        for (UI.Theme theme : UI.Theme.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(theme.label(), UI.theme() == theme);
            item.setFont(UI.FONT);
            item.addActionListener(e -> {
                UI.applyTheme(theme);
                frame.refreshTheme();
            });
            group.add(item);
            menu.add(item);
        }

        menu.show(button, 0, button.getHeight());
    }
}
