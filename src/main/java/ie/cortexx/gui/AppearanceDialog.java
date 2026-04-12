package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class AppearanceDialog {

    private AppearanceDialog() {}

    public static JButton createButton(MainFrame frame) {
        JButton button = new JButton();
        button.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/palette.svg", 18, 18)
            .setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> UI.TEXT)));
        button.putClientProperty("FlatLaf.styleClass", "themeSwitch");
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(44, 44));
        button.setToolTipText("Appearance settings");
        button.addActionListener(e -> showDialog(frame));
        return button;
    }

    private static void showDialog(MainFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(8, 4, 8, 4));
        panel.setPreferredSize(new Dimension(320, 0));

        panel.add(section("THEME"));
        panel.add(Box.createVerticalStrut(6));
        ButtonGroup themeGroup = new ButtonGroup();
        JPanel themeGrid = new JPanel(new GridLayout(2, 2, 6, 6));
        themeGrid.setOpaque(false);
        themeGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (UI.Theme theme : UI.Theme.values()) {
            JToggleButton btn = pill(theme.label(), UI.theme() == theme);
            themeGroup.add(btn);
            btn.addActionListener(e -> { UI.applyTheme(theme); frame.refreshTheme(); });
            themeGrid.add(btn);
        }
        themeGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        panel.add(themeGrid);

        panel.add(Box.createVerticalStrut(16));
        panel.add(section("FONT FAMILY"));
        panel.add(Box.createVerticalStrut(6));
        ButtonGroup familyGroup = new ButtonGroup();
        JPanel familyGrid = new JPanel(new GridLayout(2, 2, 6, 6));
        familyGrid.setOpaque(false);
        familyGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (UI.FontFamily family : UI.FontFamily.values()) {
            JToggleButton btn = pill(family.label(), UI.fontFamily() == family);
            btn.setFont(new Font(family.sansName(), Font.PLAIN, 12));
            familyGroup.add(btn);
            btn.addActionListener(e -> { UI.applyFontFamily(family); frame.refreshTheme(); });
            familyGrid.add(btn);
        }
        familyGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        panel.add(familyGrid);

        panel.add(Box.createVerticalStrut(16));
        panel.add(section("FONT SIZE"));
        panel.add(Box.createVerticalStrut(6));
        ButtonGroup sizeGroup = new ButtonGroup();
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 6, 6));
        sizeRow.setOpaque(false);
        sizeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (UI.FontSize size : UI.FontSize.values()) {
            JToggleButton btn = pill(size.label(), UI.fontSize() == size);
            sizeGroup.add(btn);
            btn.addActionListener(e -> { UI.applyFontSize(size); frame.refreshTheme(); });
            sizeRow.add(btn);
        }
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(sizeRow);

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{"Done"});
        JDialog dialog = pane.createDialog(frame, "Appearance");
        dialog.setModal(false);
        dialog.setVisible(true);
    }

    private static JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_BOLD.deriveFont(10f));
        label.setForeground(UI.TEXT_DIM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JToggleButton pill(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.putClientProperty("FlatLaf.styleClass", "secondary");
        btn.setFont(UI.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
