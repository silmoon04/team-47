package ie.cortexx.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

// shared ui helpers for the whole app.
//
// how to use:
//   1. call UI.init() once in Main.java after FlatDarkLaf.setup()
//   2. use UI.table(), UI.statCard(), etc in your panels
//
// all colours come from a theme palette i looked up

// all flatlaf UIManager keys come from:
//   https://www.formdev.com/flatlaf/how-to-customize/
//   https://www.formdev.com/flatlaf/customizing/
//   https://www.formdev.com/flatlaf/components/table/
//   https://www.formdev.com/flatlaf/components/textfield/
// swing table/renderer docs:
//   https://docs.oracle.com/javase/tutorial/uiswing/components/table.html

public final class UI {

    private UI() {} // all static, never instantiate

    // -- colours --
    // having hex colrs here means we never hardcode a colour anywhere else.

    public static final Color BG         = new Color(0x0f1117);
    public static final Color BG_CARD    = new Color(0x181a20);
    public static final Color BG_HOVER   = new Color(0x1e2028);
    public static final Color BG_INPUT   = new Color(0x13151b);
    public static final Color BORDER     = new Color(0x2a2d37);
    public static final Color TEXT       = new Color(0xe4e6eb);
    public static final Color TEXT_DIM   = new Color(0x8b8fa3);
    public static final Color TEXT_MUTED = new Color(0x5c5f6e);
    public static final Color ACCENT     = new Color(0x4f8cff);
    public static final Color GREEN      = new Color(0x34d399);
    public static final Color YELLOW     = new Color(0xfbbf24);
    public static final Color RED        = new Color(0xf87171);
    public static final Color ORANGE     = new Color(0xfb923c);
    public static final Color PURPLE     = new Color(0xa78bfa);
    public static final int CARD_ARC     = 18;
    public static final int BUTTON_ARC   = 14;
    public static final int FIELD_ARC    = 14;
    public static final int TAB_ARC      = 12;

    // -- fonts --

    private static final String SANS = "Outfit Medium";
    private static final String DISPLAY = "Outfit Regular";
    private static final String MONO = "JetBrains Mono";

    public static final Font FONT          = new Font(SANS, Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font(SANS, Font.PLAIN, 12);
    public static final Font FONT_BOLD     = new Font(SANS, Font.BOLD, 13);
    public static final Font FONT_TITLE    = new Font(DISPLAY, Font.BOLD, 18);
    public static final Font FONT_HEADING  = new Font(DISPLAY, Font.BOLD, 16);
    public static final Font FONT_MONO     = new Font(MONO, Font.PLAIN, 12);
    public static final Font FONT_MONO_BIG = new Font(MONO, Font.BOLD, 20);

    // -- badge colour map --

    private static final Map<String, Color[]> BADGES = Map.ofEntries(
        badge("NORMAL", GREEN),      badge("DELIVERED", GREEN),
        badge("IN_STOCK", GREEN),    badge("ACTIVE", GREEN),
        badge("CASH", GREEN),        badge("PHARMACIST", GREEN),
        badge("SUSPENDED", YELLOW),  badge("LOW_STOCK", YELLOW),
        badge("PROCESSING", ORANGE), badge("ON_CREDIT", ORANGE),
        badge("PACKED", ORANGE),
        badge("IN_DEFAULT", RED),    badge("CANCELLED", RED),
        badge("OUT_OF_STOCK", RED),
        badge("ACCEPTED", ACCENT),   badge("CREDIT_CARD", ACCENT),
        badge("DEBIT_CARD", ACCENT), badge("FIXED", ACCENT),
        badge("ADMIN", ACCENT),
        badge("DISPATCHED", PURPLE), badge("FLEXIBLE", PURPLE),
        badge("MANAGER", PURPLE)
    );

    private static Map.Entry<String, Color[]> badge(String key, Color fg) {
        return Map.entry(key, new Color[]{
            new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 30), fg
        });
    }


    // -- init --

    public static void init() {
        UIManager.put("defaultFont", FONT);

        // change this one constant if you want the app more or less rounded.
        UIManager.put("Component.arc", CARD_ARC);
        UIManager.put("Button.arc", BUTTON_ARC);
        UIManager.put("TextComponent.arc", FIELD_ARC);
        UIManager.put("CheckBox.arc", BUTTON_ARC);
        UIManager.put("ComboBox.arc", FIELD_ARC);
        UIManager.put("Spinner.arc", FIELD_ARC);
        UIManager.put("ProgressBar.arc", BUTTON_ARC);
        UIManager.put("TabbedPane.tabArc", TAB_ARC);
        UIManager.put("TabbedPane.tabSelectionArc", TAB_ARC);
        UIManager.put("TabbedPane.cardTabArc", TAB_ARC);
        UIManager.put("TabbedPane.buttonArc", TAB_ARC);

        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 8);

        UIManager.put("Panel.background", BG);

        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.alternateRowColor", BG_HOVER);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", ACCENT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.rowHeight", 40);
        UIManager.put("TableHeader.background", BG_HOVER);
        UIManager.put("TableHeader.foreground", TEXT_DIM);

        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("TabbedPane.selectedForeground", TEXT);
        UIManager.put("TabbedPane.hoverColor", BG_HOVER);

        UIManager.put("Button.default.background", ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Component.focusColor", BORDER);
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.focusedBorderColor", BORDER.brighter());

        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.focusedBackground", BG_INPUT);
        UIManager.put("TextField.borderColor", BORDER);
        UIManager.put("TextField.focusedBorderColor", BORDER.brighter());
        UIManager.put("TextField.focusWidth", 0);
        UIManager.put("TextField.innerFocusWidth", 0);
        UIManager.put("TextField.margin", new Insets(0, 14, 0, 14));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.placeholderForeground", TEXT_MUTED);
        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("PasswordField.focusedBackground", BG_INPUT);
        UIManager.put("PasswordField.borderColor", BORDER);
        UIManager.put("PasswordField.focusedBorderColor", BORDER.brighter());
        UIManager.put("PasswordField.focusWidth", 0);
        UIManager.put("PasswordField.innerFocusWidth", 0);
        UIManager.put("PasswordField.margin", new Insets(0, 14, 0, 14));
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.borderColor", BORDER);
        UIManager.put("ComboBox.focusedBorderColor", BORDER.brighter());
        UIManager.put("ComboBox.focusWidth", 0);
        UIManager.put("ComboBox.innerFocusWidth", 0);
        UIManager.put("ComboBox.padding", new Insets(0, 14, 0, 10));
        UIManager.put("List.background", BG_CARD);
        UIManager.put("List.selectionBackground", ACCENT);

        UIManager.put("[style]ToggleButton.sidebarNav",
            "arc:" + BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:11,12,11,12; iconTextGap:10; " +
            "background:#181a20; foreground:#8b8fa3; borderColor:#181a20; " +
            "hoverBackground:#1e2028; " +
            "selectedBackground:#303648; selectedForeground:#e4e6eb; selectedBorderColor:#2a2d37");

        UIManager.put("[style]Button.sidebarIcon",
            "arc:" + BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,8,8,8; " +
            "background:#181a20; foreground:#8b8fa3; borderColor:#2a2d37; " +
            "hoverBackground:#1e2028");

        UIManager.put("[style]Button.secondary",
            "arc:" + BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:#222735; foreground:#e4e6eb; borderColor:#394055; " +
            "hoverBackground:#2b3142; pressedBackground:#30384a");

        UIManager.put("[style]Button.primary",
            "arc:" + BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:#4f8cff; foreground:#ffffff; borderColor:#4f8cff; " +
            "hoverBackground:#6399ff; pressedBackground:#3d7ef6");

        UIManager.put("[style]Button.danger",
            "arc:" + BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:#2a1d1f; foreground:#f6b3b3; borderColor:#5c363b; " +
            "hoverBackground:#332326; pressedBackground:#3b282c");
    }


    // -- small descriptors --

    public record Stat(String label, String value, Color colour, String iconPath) {}
    public record Detail(String label, String value) {}
    public record Tab(String title, JComponent content) {}
    public record Column<T>(String title, Function<T, ?> value, ColumnStyle style) {}

    public enum ColumnStyle {
        TEXT,
        MONO,
        BADGE
    }

    public static Stat stat(String label, String value, Color colour) {
        return new Stat(label, value, colour, null);
    }

    public static Stat stat(String label, String value, Color colour, String iconPath) {
        return new Stat(label, value, colour, iconPath);
    }

    public static Detail detail(String label, String value) {
        return new Detail(label, value);
    }

    public static Tab tab(String title, JComponent content) {
        return new Tab(title, content);
    }

    public static <T> Column<T> col(String title, Function<T, ?> value) {
        return new Column<>(title, value, ColumnStyle.TEXT);
    }

    public static <T> Column<T> monoCol(String title, Function<T, ?> value) {
        return new Column<>(title, value, ColumnStyle.MONO);
    }

    public static <T> Column<T> badgeCol(String title, Function<T, ?> value) {
        return new Column<>(title, value, ColumnStyle.BADGE);
    }


    // -- layout helpers --

    public static void applyPanel(JPanel p) {
        p.setLayout(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    public static JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    public static JPanel card() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG_CARD);
        p.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        p.setBorder(cardBorder(16, 20, 16, 20, CARD_ARC));
        return p;
    }

    public static JPanel formCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        p.setBorder(cardBorder(16, 20, 16, 20, CARD_ARC));
        return p;
    }

    public static JPanel formPage(JPanel formCard) {
        JPanel p = panel();
        p.add(formCard, BorderLayout.NORTH);
        return p;
    }

    public static JPanel statsRow(int cols) {
        JPanel p = new JPanel(new GridLayout(1, cols, 12, 0));
        p.setOpaque(false);
        return p;
    }

    public static JPanel stats(Stat... stats) {
        JPanel row = statsRow(stats.length);
        for (var stat : stats) {
            row.add(statCard(stat.label(), stat.value(), stat.colour(), stat.iconPath()));
        }
        return row;
    }

    public static Component gap(int px) {
        return Box.createVerticalStrut(px);
    }

    public static JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (var b : buttons) p.add(b);
        return p;
    }

    public static JPanel splitPanel(JComponent left, JComponent right, int rightWidth) {
        right.setPreferredSize(new Dimension(rightWidth, 0));
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    public static JPanel twoColumn(JComponent left, JComponent right, int gap) {
        JPanel p = new JPanel(new GridLayout(1, 2, gap, 0));
        p.setOpaque(false);
        p.add(left);
        p.add(right);
        return p;
    }

    public static void swap(JPanel host, JComponent content) {
        host.removeAll();
        host.add(content, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }

    public static JPanel emptyState(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(message);
        l.setFont(FONT);
        l.setForeground(TEXT_DIM);
        p.add(l);
        return p;
    }


    // -- toolbar --

    public static JPanel toolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 12, 0));
        return p;
    }

    public static JPanel toolbar(String searchHint, JTable table, String buttonText) {
        JPanel bar = toolbar();
        bar.add(searchField(searchHint, table), BorderLayout.WEST);
        bar.add(primaryButton(buttonText), BorderLayout.EAST);
        return bar;
    }

    public static JPanel toolbar(String searchHint, JTable table, JButton... rightButtons) {
        JPanel bar = toolbar();
        bar.add(searchField(searchHint, table), BorderLayout.WEST);
        bar.add(buttonRow(rightButtons), BorderLayout.EAST);
        return bar;
    }

    public static JPanel toolbarAndTable(JPanel toolbar, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(toolbar, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    public static JPanel pageWithStats(JPanel stats, JPanel toolbar, JComponent tableContent) {
        var p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(stats, BorderLayout.NORTH);
        p.add(toolbarAndTable(toolbar, tableContent), BorderLayout.CENTER);
        return p;
    }


    // -- stat card --

    public static JPanel statCard(String label, String value, Color colour) {
        return statCard(label, value, colour, null);
    }

    public static JPanel statCard(String label, String value, Color colour, String iconPath) {
        JPanel c = new JPanel(new BorderLayout(12, 0));
        c.setBackground(BG_CARD);
        c.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        c.setBorder(cardBorder(14, 18, 14, 18, CARD_ARC));

        JPanel icon = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                var g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setOpaque(false);

        if (iconPath != null && !iconPath.isBlank()) {
            JLabel iconLabel = new JLabel(new FlatSVGIcon(iconPath, 18, 18), SwingConstants.CENTER);
            iconLabel.setForeground(colour);
            icon.add(iconLabel, BorderLayout.CENTER);
        }

        c.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        text.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(FONT_MONO_BIG);
        val.setForeground(TEXT);
        text.add(val);

        c.add(text, BorderLayout.CENTER);
        return c;
    }


    // -- tables --

    public static DefaultTableModel readonlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static StyledTable table(String... columns) {
        return table(readonlyModel(columns), true);
    }

    public static StyledTable table(DefaultTableModel model) {
        return table(model, true);
    }

    public static StyledTable table(DefaultTableModel model, boolean sortable) {
        JTable table = new JTable(model);
        styleTable(table, sortable);
        return new StyledTable(table, model, wrap(table));
    }

    @SafeVarargs
    public static <T> DataTable<T> table(Column<T>... columns) {
        var model = new DataTableModel<>(List.of(columns));
        JTable table = new JTable(model);
        styleTable(table, true);

        for (int i = 0; i < columns.length; i++) {
            switch (columns[i].style()) {
                case MONO -> table.getColumnModel().getColumn(i).setCellRenderer(monoRenderer());
                case BADGE -> table.getColumnModel().getColumn(i).setCellRenderer(badgeRenderer());
                default -> {
                }
            }
        }

        return new DataTable<>(table, model, wrap(table));
    }

    public static record StyledTable(JTable table, DefaultTableModel model, JScrollPane scroll) {
        public StyledTable badgeColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(badgeRenderer());
            return this;
        }

        public StyledTable monoColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(monoRenderer());
            return this;
        }
    }

    public static record DataTable<T>(JTable table, DataTableModel<T> model, JScrollPane scroll) {
        public DataTable<T> rows(Iterable<? extends T> rows) {
            model.setRows(rows);
            return this;
        }

        public DataTable<T> addRow(T row) {
            model.addRow(row);
            return this;
        }

        public DataTable<T> clear() {
            model.clear();
            return this;
        }

        public DataTable<T> onSelect(Consumer<T> consumer) {
            table.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                int row = table.getSelectedRow();
                if (row < 0) return;
                consumer.accept(model.rowAt(table.convertRowIndexToModel(row)));
            });
            return this;
        }

        public T rowAtView(int row) {
            if (row < 0) return null;
            return model.rowAt(table.convertRowIndexToModel(row));
        }
    }

    private static final class DataTableModel<T> extends AbstractTableModel {
        private final List<Column<T>> columns;
        private final List<T> rows = new ArrayList<>();

        private DataTableModel(List<Column<T>> columns) {
            this.columns = columns;
        }

        @Override public int getRowCount() {
            return rows.size();
        }

        @Override public int getColumnCount() {
            return columns.size();
        }

        @Override public String getColumnName(int column) {
            return columns.get(column).title();
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            return columns.get(columnIndex).value().apply(rows.get(rowIndex));
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        private void setRows(Iterable<? extends T> items) {
            rows.clear();
            for (var item : items) rows.add(item);
            fireTableDataChanged();
        }

        private void addRow(T item) {
            rows.add(item);
            int row = rows.size() - 1;
            fireTableRowsInserted(row, row);
        }

        private void clear() {
            if (rows.isEmpty()) return;
            rows.clear();
            fireTableDataChanged();
        }

        private T rowAt(int row) {
            return rows.get(row);
        }
    }

    private static void styleTable(JTable table, boolean sortable) {
        table.setFont(FONT);
        table.setRowHeight(40);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font(SANS, Font.BOLD, 11));
        table.getTableHeader().setBackground(BG_HOVER);
        table.getTableHeader().setForeground(TEXT_DIM);
        table.setDefaultRenderer(Object.class, defaultRenderer());
        table.putClientProperty("FlatLaf.style", "selectionArc:" + CARD_ARC);

        if (sortable) {
            table.setRowSorter(new TableRowSorter<>((TableModel) table.getModel()));
        }
    }

    private static JScrollPane wrap(JTable table) {
        var scroll = new JScrollPane(table);
        scroll.putClientProperty("JComponent.roundRect", true);
        scroll.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        scroll.setBorder(roundedBorder(CARD_ARC));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private static DefaultTableCellRenderer defaultRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? BG_CARD : BG_HOVER);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                return this;
            }
        };
    }

    private static TableCellRenderer badgeRenderer() {
        return (tbl, val, sel, foc, row, col) -> {
            String raw = val != null ? val.toString() : "";
            String key = raw.toUpperCase().replace(' ', '_');
            var label = new JLabel(raw, SwingConstants.CENTER);
            label.setFont(new Font(SANS, Font.BOLD, 11));
            label.setOpaque(true);
            Color[] s = BADGES.getOrDefault(key, new Color[]{BG_HOVER, TEXT_DIM});
            label.setBackground(sel ? ACCENT : s[0]);
            label.setForeground(sel ? Color.WHITE : s[1]);
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            return label;
        };
    }

    private static TableCellRenderer monoRenderer() {
        return new DefaultTableCellRenderer() {
            { setFont(FONT_MONO); }

            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? BG_CARD : BG_HOVER);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                return this;
            }
        };
    }


    // -- search field --

    public static JTextField searchField(String placeholder, JTable table) {
        var field = new JTextField(20);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.showClearButton", true);
        field.putClientProperty("FlatLaf.style", "arc:" + FIELD_ARC + "; focusWidth:0; innerFocusWidth:0");
        field.setFont(FONT);
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { go(); }
            public void removeUpdate(DocumentEvent e)  { go(); }
            public void changedUpdate(DocumentEvent e) { go(); }

            @SuppressWarnings("unchecked")
            private void go() {
                if (!(table.getRowSorter() instanceof TableRowSorter<?> sorter)) return;
                String text = field.getText();
                ((TableRowSorter<TableModel>) sorter).setRowFilter(
                    text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text))
                );
            }
        });
        return field;
    }


    // -- buttons --

    public static JButton primaryButton(String text) {
        var b = new JButton(text);
        b.putClientProperty("FlatLaf.styleClass", "primary");
        b.setFont(FONT_BOLD);
        return b;
    }

    public static JButton button(String text) {
        var b = new JButton(text);
        b.putClientProperty("FlatLaf.styleClass", "secondary");
        b.setFont(FONT);
        return b;
    }

    public static JButton dangerButton(String text) {
        var b = new JButton(text);
        b.putClientProperty("FlatLaf.styleClass", "danger");
        b.setFont(FONT);
        return b;
    }

    public static JToggleButton filterPill(String text) {
        var b = new JToggleButton(text);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setFont(FONT_SMALL);
        return b;
    }


    // -- labels --

    public static JLabel heading(String text) {
        var l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel dimLabel(String text) {
        var l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_DIM);
        return l;
    }

    public static JLabel mono(String text) {
        var l = new JLabel(text);
        l.setFont(FONT_MONO);
        l.setForeground(TEXT);
        return l;
    }


    // -- forms --

    public static JPanel field(String label, JComponent input) {
        var p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        var lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(lbl);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (input instanceof JTextField tf) {
            tf.putClientProperty("FlatLaf.style", "arc:" + FIELD_ARC + "; focusWidth:0; innerFocusWidth:0");
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        }
        if (input instanceof JComboBox<?> box) {
            box.putClientProperty("FlatLaf.style", "arc:" + FIELD_ARC + "; focusWidth:0; innerFocusWidth:0");
        }
        if (input instanceof JScrollPane scroll) {
            scroll.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        }
        p.add(input);
        return p;
    }

    public static JPanel formRow(JComponent... fields) {
        var row = new JPanel(new GridLayout(1, fields.length, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 12, 0));
        for (var f : fields) row.add(f);
        return row;
    }

    public static JTextArea textArea(String text, int rows) {
        var ta = new JTextArea(text, rows, 40);
        ta.setFont(FONT_MONO);
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT);
        ta.setCaretColor(TEXT);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    public static JPanel textAreaField(String label, String text, int rows) {
        return field(label, new JScrollPane(textArea(text, rows)));
    }


    // -- detail rows --

    public static JPanel detailRow(String label, String value) {
        var row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(8, 0, 8, 0)));

        var lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        row.add(lbl, BorderLayout.WEST);

        var val = new JLabel(value);
        val.setFont(FONT_BOLD);
        val.setForeground(TEXT);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    public static JPanel detailCard(String title, Detail... details) {
        return detailCard(title, details, new JButton[0]);
    }

    public static JPanel detailCard(String title, Detail[] details, JButton... actions) {
        JPanel card = formCard();
        card.add(heading(title));
        card.add(gap(12));
        for (var detail : details) {
            card.add(detailRow(detail.label(), detail.value()));
        }
        if (actions.length > 0) {
            card.add(gap(16));
            card.add(buttonRow(actions));
        }
        return card;
    }


    // -- inner tabs --

    public static JTabbedPane innerTabs() {
        var tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT);
        tabs.setBackground(BG);
        tabs.putClientProperty("FlatLaf.style",
            "tabArc:" + TAB_ARC + "; tabSelectionArc:" + TAB_ARC + "; selectedInsets:2,6,0,6");
        return tabs;
    }

    public static JTabbedPane innerTabs(Tab... tabs) {
        var pane = innerTabs();
        for (var tab : tabs) {
            pane.addTab(tab.title(), tab.content());
        }
        return pane;
    }


    // -- label helpers --

    public static JLabel label(String text, Font font, Color colour) {
        var lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(colour);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public static JLabel labelCenter(String text, Font font, Color colour) {
        var lbl = label(text, font, colour);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    public static JLabel title(String text) {
        return label(text, FONT_TITLE.deriveFont(24f), TEXT);
    }

    public static JLabel subtitle(String text) {
        return label(text, FONT_SMALL, TEXT_DIM);
    }

    public static JLabel errorLabel() {
        return label(" ", FONT_SMALL, RED);
    }

    public static JComponent avatarBadge(String text, int size) {
        String safe = (text == null || text.isBlank()) ? "U" : text.trim().substring(0, 1).toUpperCase();
        JLabel icon = new JLabel(new FlatSVGIcon("icons/circle.svg", size, size)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(FONT_BOLD.deriveFont(16f));
                g2.setColor(TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(safe)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(safe, x, y);
                g2.dispose();
            }
        };
        icon.setForeground(TEXT_DIM);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(size, size));
        return icon;
    }


    // -- input helpers --

    public static JTextField inputField(String placeholder) {
        var tf = new JTextField();
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.putClientProperty("FlatLaf.style", "arc:" + FIELD_ARC + "; focusWidth:0; innerFocusWidth:0");
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    public static JPasswordField passwordField(String placeholder) {
        var pf = new JPasswordField();
        pf.putClientProperty("JTextField.placeholderText", placeholder);
        pf.putClientProperty("FlatLaf.style", "arc:" + FIELD_ARC + "; focusWidth:0; innerFocusWidth:0");
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }


    // -- button helpers --

    public static JButton primaryButtonWide(String text) {
        var btn = primaryButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }


    // -- more layout helpers --

    public static JPanel centeredPanel() {
        var p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        return p;
    }

    public static JPanel vcard(int padX, int padY, int width, int height) {
        var p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        p.setBorder(cardBorder(padY, padX, padY, padX, CARD_ARC));
        p.setPreferredSize(new Dimension(width, height));
        return p;
    }

    public static JPanel transparentPanel(int vgap) {
        var p = new JPanel(new BorderLayout(0, vgap));
        p.setOpaque(false);
        return p;
    }

    public static void applyPanelNoPad(JPanel p) {
        p.setLayout(new BorderLayout());
        p.setBackground(BG);
    }

    public static JPanel cardPanel() {
        var p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.putClientProperty("FlatLaf.style", "arc:" + CARD_ARC);
        p.setBorder(roundedBorder(CARD_ARC));
        return p;
    }

    public static JPanel flowRow(int hgap) {
        var p = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, 0));
        p.setOpaque(false);
        return p;
    }

    public static JPanel gridRow(int cols, int hgap) {
        var p = new JPanel(new GridLayout(1, cols, hgap, 0));
        p.setOpaque(false);
        return p;
    }

    public static JPanel paddedPanel(int top, int left, int bottom, int right) {
        var p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(top, left, bottom, right));
        return p;
    }

    private static Border cardBorder(int top, int left, int bottom, int right, int arc) {
        return BorderFactory.createCompoundBorder(roundedBorder(arc), new EmptyBorder(top, left, bottom, right));
    }

    private static Border roundedBorder(int arc) {
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
                g2.setColor(BORDER);
                g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
                g2.dispose();
            }
        };
    }
}
