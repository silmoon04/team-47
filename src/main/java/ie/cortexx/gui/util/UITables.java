package ie.cortexx.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Table styling and search helpers backing the {@link UI} facade.
 */
final class UITables {
    private UITables() {}

    static DefaultTableModel readonlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static void styleTable(JTable table, boolean sortable) {
        table.setFont(UI.FONT);
        table.setRowHeight(34);
        table.setBackground(UI.BG_CARD);
        table.setForeground(UI.TEXT);
        table.setGridColor(UI.BORDER);
        table.setSelectionBackground(UI.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(UI.FONT_BOLD.deriveFont(11f));
        table.getTableHeader().setBackground(UI.BG_HOVER);
        table.getTableHeader().setForeground(UI.TEXT_DIM);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(headerRenderer());
        table.setDefaultRenderer(Object.class, defaultRenderer());
        table.setDefaultRenderer(Number.class, defaultRenderer());
        table.setDefaultRenderer(Integer.class, defaultRenderer());
        table.setDefaultRenderer(Long.class, defaultRenderer());
        table.setDefaultRenderer(Double.class, defaultRenderer());
        table.setDefaultRenderer(Float.class, defaultRenderer());
        if (sortable) {
            table.setRowSorter(new TableRowSorter<>(table.getModel()));
        }
    }

    static JScrollPane wrap(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.putClientProperty("JComponent.roundRect", true);
        scroll.setBorder(UIStyleSupport.roundedBorder(UI.CARD_ARC));
        scroll.getViewport().setBackground(UI.BG_CARD);
        return scroll;
    }

    static TableCellRenderer badgeRenderer() {
        return (table, value, selected, focused, row, column) -> {
            String raw = value != null ? value.toString() : "";
            String key = UIThemeSupport.badgeKey(raw);
            Color[] style = UIThemeSupport.badgeStyle(raw);

            JLabel label = new JLabel(UIThemeSupport.badgeText(raw));
            label.setOpaque(true);
            label.setFont(UI.FONT_SMALL);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setForeground(selected ? Color.WHITE : style[1]);
            label.setBackground(selected ? table.getSelectionBackground() : (row % 2 == 0 ? UI.BG_CARD : UI.BG_HOVER));
            label.setBorder(new EmptyBorder(0, 16, 0, 16));
            return label;
        };
    }

    static TableCellRenderer monoRenderer() {
        return new DefaultTableCellRenderer() {
            {
                setFont(UI.FONT_MONO_SMALL);
            }

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                if (!selected) setBackground(row % 2 == 0 ? UI.BG_CARD : UI.BG_HOVER);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                return this;
            }
        };
    }

    static JTextField searchField(String placeholder, JTable table) {
        JTextField field = new JTextField(20);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.showClearButton", true);
        field.putClientProperty("FlatLaf.style", UIStyleSupport.inputStyle());
        field.setFont(UI.FONT);
        field.setCaretColor(UI.TEXT);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            @SuppressWarnings("unchecked")
            private void filter() {
                if (!(table.getRowSorter() instanceof TableRowSorter<?> sorter)) return;
                String text = field.getText();
                ((TableRowSorter<TableModel>) sorter).setRowFilter(
                    text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text))
                );
            }
        });
        return field;
    }

    private static DefaultTableCellRenderer defaultRenderer() {
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

    private static DefaultTableCellRenderer headerRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setFont(UI.FONT_BOLD.deriveFont(11f));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBackground(UI.BG_HOVER);
                setForeground(UI.TEXT_DIM);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                return this;
            }
        };
    }
}
