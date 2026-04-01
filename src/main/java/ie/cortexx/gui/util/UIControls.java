package ie.cortexx.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Shared controls and small component compositions used by panels.
 *
 * <p>This includes the common button, label, form, and "small composition"
 * helpers that keep the panel classes readable without forcing them into a
 * heavier builder architecture.</p>
 */
final class UIControls {
    private UIControls() {}

    static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("FlatLaf.styleClass", "primary");
        button.setFont(UI.FONT_BOLD);
        return button;
    }

    static JButton button(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("FlatLaf.styleClass", "secondary");
        button.setFont(UI.FONT);
        return button;
    }

    static JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("FlatLaf.styleClass", "danger");
        button.setFont(UI.FONT);
        return button;
    }

    static JButton iconButton(String text, String iconPath, boolean primary) {
        JButton button = primary ? primaryButton(text) : button(text);
        button.setIcon(icon(iconPath, 16, primary ? Color.WHITE : UI.TEXT));
        button.setIconTextGap(8);
        button.setFont(UI.FONT_MONO_BOLD);
        return button;
    }

    static JButton stepperButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI.FONT_MONO_BOLD.deriveFont(13f));
        button.setForeground(UI.TEXT);
        button.setBackground(UI.BG_CARD);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(28, 30));
        button.setMaximumSize(new Dimension(28, 30));
        return button;
    }

    static JButton squareButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI.FONT_BOLD.deriveFont(11f));
        button.setForeground(UI.TEXT_DIM);
        button.setBackground(UI.BG_CARD);
        button.setBorder(BorderFactory.createLineBorder(UI.BORDER, 1, true));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(22, 22));
        button.setMaximumSize(new Dimension(22, 22));
        return button;
    }

    static JToggleButton filterPill(String text) {
        JToggleButton button = new JToggleButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFont(UI.FONT_SMALL);
        return button;
    }

    static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_HEADING);
        label.setForeground(UI.TEXT);
        return label;
    }

    static JLabel dimLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_SMALL);
        label.setForeground(UI.TEXT_DIM);
        return label;
    }

    static JLabel mono(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_MONO);
        label.setForeground(UI.TEXT);
        return label;
    }

    static JLabel monoLabel(String text, float size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_MONO.deriveFont(size));
        label.setForeground(color);
        return label;
    }

    static JLabel monoLabelBold(String text, float size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_MONO_BOLD.deriveFont(size));
        label.setForeground(color);
        return label;
    }

    static JLabel countBadge(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(UI.FONT_MONO_BOLD);
        label.setForeground(UI.TEXT);
        label.setOpaque(true);
        label.setBackground(UI.BG_CARD);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI.BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return label;
    }

    static JPanel summaryRow(JLabel label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    static JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel labelComponent = label(label, UI.FONT_SMALL, UI.TEXT_DIM);
        labelComponent.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(labelComponent);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (input instanceof JTextField textField) {
            textField.putClientProperty("FlatLaf.style", UIStyleSupport.inputStyle());
            textField.setCaretColor(UI.TEXT);
            UIStyleSupport.sizeField(textField, 42);
        }
        if (input instanceof JComboBox<?> comboBox) {
            comboBox.putClientProperty("FlatLaf.style", UIStyleSupport.inputStyle());
            UIStyleSupport.sizeField(comboBox, 42);
        }
        if (input instanceof JScrollPane scrollPane) {
            scrollPane.putClientProperty("FlatLaf.style", "arc:" + UI.CARD_ARC);
        }
        panel.add(input);
        return panel;
    }

    static JPanel formRow(JComponent... fields) {
        JPanel row = new JPanel(new GridLayout(1, fields.length, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 12, 0));
        for (var field : fields) row.add(field);
        return row;
    }

    static JTextArea textArea(String text, int rows) {
        JTextArea area = new JTextArea(text, rows, 40);
        area.setFont(UI.FONT_MONO);
        area.setBackground(UI.BG_INPUT);
        area.setForeground(UI.TEXT);
        area.setCaretColor(UI.TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    static JPanel textAreaField(String label, String text, int rows) {
        return field(label, new JScrollPane(textArea(text, rows)));
    }

    static JPanel detailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UI.BORDER),
            new EmptyBorder(8, 0, 8, 0)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UI.FONT_SMALL);
        labelComponent.setForeground(UI.TEXT_DIM);
        row.add(labelComponent, BorderLayout.WEST);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(UI.FONT_BOLD);
        valueComponent.setForeground(UI.TEXT);
        row.add(valueComponent, BorderLayout.EAST);
        return row;
    }

    static JPanel detailCard(String title, UI.Detail[] details, JButton... actions) {
        JPanel card = UILayouts.formCard();
        card.add(heading(title));
        card.add(UILayouts.gap(12));
        for (var detail : details) {
            card.add(detailRow(detail.label(), detail.value()));
        }
        if (actions.length > 0) {
            card.add(UILayouts.gap(16));
            card.add(UILayouts.buttonRow(actions));
        }
        return card;
    }

    static JPanel detailLine(String labelText, String valueText, int labelWidth, boolean wrapValue) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapValue ? 50 : 28));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UI.BORDER),
            new EmptyBorder(6, 0, 6, 0)
        ));

        JLabel label = new JLabel(labelText);
        label.setFont(UI.FONT_MONO_BOLD.deriveFont(11f));
        label.setForeground(UI.TEXT_DIM);
        label.setPreferredSize(new Dimension(labelWidth, wrapValue ? 34 : 18));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.anchor = wrapValue ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 0, 12);
        row.add(label, labelConstraints);

        JComponent value = wrapValue ? wrappedValue(valueText) : detailValue(valueText);
        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;
        valueConstraints.weightx = 1;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        valueConstraints.anchor = GridBagConstraints.WEST;
        row.add(value, valueConstraints);
        return row;
    }

    static JLabel detailValue(String valueText) {
        JLabel value = new JLabel(valueText, SwingConstants.LEFT);
        value.setFont(UI.FONT_MONO.deriveFont(12f));
        value.setForeground(UI.TEXT);
        return value;
    }

    static JTextArea wrappedValue(String valueText) {
        JTextArea value = new JTextArea(valueText);
        value.setFont(UI.FONT_MONO.deriveFont(12f));
        value.setForeground(UI.TEXT);
        value.setOpaque(false);
        value.setEditable(false);
        value.setLineWrap(true);
        value.setWrapStyleWord(true);
        value.setBorder(BorderFactory.createEmptyBorder());
        return value;
    }

    static JComponent fullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return component;
    }

    static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI.FONT_MONO_BOLD);
        label.setForeground(UI.TEXT_DIM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel label(String text, Font font, Color colour) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(colour);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel labelCenter(String text, Font font, Color colour) {
        JLabel label = label(text, font, colour);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    static JLabel title(String text) {
        return label(text, UI.FONT_TITLE.deriveFont(24f), UI.TEXT);
    }

    static JLabel subtitle(String text) {
        return label(text, UI.FONT_SMALL, UI.TEXT_DIM);
    }

    static JLabel errorLabel() {
        return label(" ", UI.FONT_SMALL, UI.RED);
    }

    static JComponent avatarBadge(String text, int size) {
        String safe = (text == null || text.isBlank()) ? "U" : text.trim().substring(0, 1).toUpperCase();
        JLabel icon = new JLabel(new FlatSVGIcon("icons/circle.svg", size, size)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(UI.FONT_BOLD.deriveFont(16f));
                g2.setColor(UI.TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(safe)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(safe, x, y);
                g2.dispose();
            }
        };
        icon.setForeground(UI.TEXT_DIM);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(size, size));
        return icon;
    }

    static FlatSVGIcon icon(String path, int size, Color colour) {
        return new FlatSVGIcon(path, size, size)
            .setColorFilter(new FlatSVGIcon.ColorFilter(c -> colour));
    }

    static JTextField inputField(String placeholder) {
        JTextField textField = new JTextField();
        textField.putClientProperty("JTextField.placeholderText", placeholder);
        textField.putClientProperty("FlatLaf.style", UIStyleSupport.inputStyle());
        textField.setCaretColor(UI.TEXT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        return textField;
    }

    static JPasswordField passwordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", placeholder);
        passwordField.putClientProperty("FlatLaf.style", UIStyleSupport.inputStyle());
        passwordField.setCaretColor(UI.TEXT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        return passwordField;
    }

    static JButton primaryButtonWide(String text) {
        JButton button = primaryButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    static TableCellRenderer plainTableRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setFont(UI.FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                if (!selected) setBackground(row % 2 == 0 ? UI.BG_CARD : UI.BG_HOVER);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                return this;
            }
        };
    }
}
