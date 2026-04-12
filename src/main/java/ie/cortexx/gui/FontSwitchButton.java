package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Dimension;

public final class FontSwitchButton {

    private FontSwitchButton() {}

    public static JButton create(MainFrame frame) {
        JButton button = new JButton(UI.fontSize().label());
        button.putClientProperty("FlatLaf.styleClass", "themeSwitch");
        button.setFont(UI.FONT_MONO_BOLD);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 44));
        button.setToolTipText("Font size (" + UI.fontSize().label() + ")");
        button.addActionListener(e -> showMenu(button, frame));
        return button;
    }

    private static void showMenu(JButton button, MainFrame frame) {
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        for (UI.FontSize size : UI.FontSize.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(size.label(), UI.fontSize() == size);
            item.setFont(UI.FONT);
            item.addActionListener(e -> {
                UI.applyFontSize(size);
                frame.refreshTheme();
            });
            group.add(item);
            menu.add(item);
        }

        menu.show(button, 0, button.getHeight());
    }
}
