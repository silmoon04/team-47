package ie.cortexx.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Shared panel/layout primitives. These methods build containers and page
 * structure, but leave the panel-facing API exposed through {@link UI}.
 */
final class UILayouts {
    private UILayouts() {
    }

    static void applyPanel(JPanel panel) {
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBackground(UI.BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    static JPanel panel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UI.BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    static JPanel card() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UI.BG_CARD);
        panel.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        panel.setBorder(cardBorder(16, 20, 16, 20, UI.CARD_ARC));
        return panel;
    }

    static JPanel formCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UI.BG_CARD);
        panel.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        panel.setBorder(cardBorder(16, 20, 16, 20, UI.CARD_ARC));
        return panel;
    }

    static JPanel formPage(JPanel formCard) {
        JPanel panel = panel();
        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    static JPanel statsRow(int cols) {
        JPanel panel = new JPanel(new GridLayout(1, cols, 12, 0));
        panel.setOpaque(false);
        return panel;
    }

    static JPanel stats(UI.Stat... stats) {
        JPanel row = statsRow(stats.length);
        for (UI.Stat stat : stats) {
            row.add(UI.statCard(stat.label(), stat.value(), stat.colour(), stat.iconPath()));
        }
        return row;
    }

    static Component gap(int px) {
        return Box.createVerticalStrut(px);
    }

    static JPanel buttonRow(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    static JPanel splitPanel(JComponent left, JComponent right, int rightWidth) {
        right.setPreferredSize(new Dimension(rightWidth, 0));
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    static JPanel twoColumn(JComponent left, JComponent right, int gap) {
        JPanel panel = new JPanel(new GridLayout(1, 2, gap, 0));
        panel.setOpaque(false);
        panel.add(left);
        panel.add(right);
        return panel;
    }

    static void swap(JPanel host, JComponent content) {
        host.removeAll();
        host.add(content, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }

    static JPanel emptyState(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(message);
        label.setFont(UI.FONT);
        label.setForeground(UI.TEXT_DIM);
        panel.add(label);
        return panel;
    }

    static JPanel toolbar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));
        return panel;
    }

    static JPanel toolbar(String searchHint, JTable table, String buttonText) {
        JPanel bar = toolbar();
        bar.add(UI.searchField(searchHint, table), BorderLayout.WEST);
        bar.add(UI.primaryButton(buttonText), BorderLayout.EAST);
        return bar;
    }

    static JPanel toolbar(String searchHint, JTable table, JButton... rightButtons) {
        JPanel bar = toolbar();
        bar.add(UI.searchField(searchHint, table), BorderLayout.WEST);
        bar.add(buttonRow(rightButtons), BorderLayout.EAST);
        return bar;
    }

    static JPanel toolbarAndTable(JPanel toolbar, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    static JPanel pageWithStats(JPanel stats, JPanel toolbar, JComponent tableContent) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UI.BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(stats, BorderLayout.NORTH);
        panel.add(toolbarAndTable(toolbar, tableContent), BorderLayout.CENTER);
        return panel;
    }

    static JTabbedPane innerTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UI.FONT);
        tabs.setBackground(UI.BG);
        tabs.putClientProperty(
            "FlatLaf.style",
            "tabArc:" + UI.TAB_ARC + "; tabSelectionArc:" + UI.TAB_ARC + "; selectedInsets:2,6,0,6"
        );
        return tabs;
    }

    static JTabbedPane innerTabs(UI.Tab... tabs) {
        JTabbedPane pane = innerTabs();
        for (UI.Tab tab : tabs) {
            pane.addTab(tab.title(), tab.content());
        }
        return pane;
    }

    static JPanel statCard(String label, String value, Color colour) {
        return statCard(label, value, colour, null);
    }

    static JPanel statCard(String label, String value, Color colour, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UI.BG_CARD);
        card.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        card.setBorder(cardBorder(14, 18, 14, 18, UI.CARD_ARC));

        JPanel iconPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UI.BUTTON_ARC, UI.BUTTON_ARC);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setOpaque(false);

        if (iconPath != null && !iconPath.isBlank()) {
            JLabel iconLabel = new JLabel(new FlatSVGIcon(iconPath, 18, 18), SwingConstants.CENTER);
            iconLabel.setForeground(colour);
            iconPanel.add(iconLabel, BorderLayout.CENTER);
        }

        card.add(iconPanel, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UI.FONT_SMALL);
        labelComponent.setForeground(UI.TEXT_DIM);
        text.add(labelComponent);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(UI.FONT_STAT);
        valueComponent.setForeground(UI.TEXT);
        text.add(valueComponent);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    static JPanel centeredPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UI.BG);
        return panel;
    }

    static JPanel vcard(int padX, int padY, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UI.BG_CARD);
        panel.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        panel.setBorder(cardBorder(padY, padX, padY, padX, UI.CARD_ARC));
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    static JPanel transparentPanel(int vgap) {
        JPanel panel = new JPanel(new BorderLayout(0, vgap));
        panel.setOpaque(false);
        return panel;
    }

    static void applyPanelNoPad(JPanel panel) {
        panel.setLayout(new BorderLayout());
        panel.setBackground(UI.BG);
    }

    static JPanel cardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UI.BG_CARD);
        panel.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        panel.setBorder(roundedBorder(UI.CARD_ARC));
        return panel;
    }

    static JPanel flowRow(int hgap) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, 0));
        panel.setOpaque(false);
        return panel;
    }

    static JPanel gridRow(int cols, int hgap) {
        JPanel panel = new JPanel(new GridLayout(1, cols, hgap, 0));
        panel.setOpaque(false);
        return panel;
    }

    static JPanel paddedPanel(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(top, left, bottom, right));
        return panel;
    }

    static Border roundedBorder(int arc) {
        return new AbstractBorder() {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(1, 1, 1, 1);
                return insets;
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UI.BORDER);
                g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
                g2.dispose();
            }
        };
    }

    private static Border cardBorder(int top, int left, int bottom, int right, int arc) {
        return BorderFactory.createCompoundBorder(roundedBorder(arc), new EmptyBorder(top, left, bottom, right));
    }
}
