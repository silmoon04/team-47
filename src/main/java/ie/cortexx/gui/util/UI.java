package ie.cortexx.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.DefaultRowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Stable facade for the GUI helper layer.
 *
 * <p>Panels continue to depend on this single entry point, but the actual
 * implementation now lives in focused package-local helper classes by concern:
 * theme, layouts, tables, and controls.</p>
 */
public final class UI {
    public enum Theme {
        DARK("Dark"),
        LIGHT("Light"),
        GREEN("Green + White"),
        BLUE("Blue + White");

        private final String label;

        Theme(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public boolean isDark() {
            return this == DARK;
        }
    }

    private static final String SANS = "Outfit Medium";
    private static final String SANS_BOLD = "Outfit Bold";
    private static final String DISPLAY = "Outfit Regular";
    private static final String MONO = Font.MONOSPACED;

    public enum FontFamily {
        OUTFIT("Outfit", "Outfit Medium", "Outfit Bold", "Outfit Regular"),
        INTER("Inter", "Inter Medium", "Inter Bold", "Inter"),
        SYSTEM("System", Font.SANS_SERIF, Font.SANS_SERIF, Font.SANS_SERIF),
        JETBRAINS("JetBrains Mono", "JetBrains Mono", "JetBrains Mono", "JetBrains Mono");

        private final String label;
        final String sans, sansBold, display;

        FontFamily(String label, String sans, String sansBold, String display) {
            this.label = label;
            this.sans = sans;
            this.sansBold = sansBold;
            this.display = display;
        }

        public String label() { return label; }
        public String sansName() { return sans; }
    }

    private static FontFamily currentFontFamily = FontFamily.OUTFIT;

    public static Color BG = new Color(0x0f1117);
    public static Color BG_CARD = new Color(0x181a20);
    public static Color BG_HOVER = new Color(0x1e2028);
    public static Color BG_INPUT = new Color(0x13151b);
    public static Color BORDER = new Color(0x2a2d37);
    public static Color TEXT = new Color(0xe4e6eb);
    public static Color TEXT_DIM = new Color(0x8b8fa3);
    public static Color TEXT_MUTED = new Color(0x5c5f6e);
    public static Color ACCENT = new Color(0x4f8cff);
    public static Color GREEN = new Color(0x34d399);
    public static Color YELLOW = new Color(0xfbbf24);
    public static Color RED = new Color(0xf87171);
    public static Color ORANGE = new Color(0xfb923c);
    public static Color PURPLE = new Color(0xa78bfa);

    public static final int CARD_ARC = 18;
    public static final int BUTTON_ARC = 14;
    public static final int FIELD_ARC = 14;
    public static final int TAB_ARC = 12;

    public enum FontSize {
        SMALL(11, "Small"),
        MEDIUM(13, "Medium"),
        LARGE(15, "Large");

        private final int base;
        private final String label;

        FontSize(int base, String label) {
            this.base = base;
            this.label = label;
        }

        public int base() { return base; }
        public String label() { return label; }
    }

    private static FontSize currentFontSize = FontSize.MEDIUM;

    public static Font FONT = new Font(SANS, Font.PLAIN, 13);
    public static Font FONT_SMALL = new Font(SANS, Font.PLAIN, 12);
    public static Font FONT_BOLD = new Font(SANS, Font.BOLD, 13);
    public static Font FONT_STAT = new Font(SANS_BOLD, Font.BOLD, 18);
    public static Font FONT_TITLE = new Font(DISPLAY, Font.BOLD, 18);
    public static Font FONT_HEADING = new Font(DISPLAY, Font.BOLD, 16);
    public static Font FONT_MONO = new Font(MONO, Font.PLAIN, 12);
    public static Font FONT_MONO_SMALL = new Font(MONO, Font.PLAIN, 11);
    public static Font FONT_MONO_BOLD = new Font(MONO, Font.BOLD, 12);
    public static Font FONT_MONO_BIG = new Font(MONO, Font.BOLD, 20);

    private UI() {}

    public record Stat(String label, String value, Color colour, String iconPath) {}
    public record Detail(String label, String value) {}
    public record Tab(String title, JComponent content) {}
    public record Column<T>(String title, Function<T, ?> value, ColumnStyle style, Integer preferredWidth) {}

    public enum ColumnStyle {
        TEXT,
        MONO,
        BADGE
    }

    public static record StyledTable(JTable table, DefaultTableModel model, JScrollPane scroll) {
        public StyledTable badgeColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(UITables.badgeRenderer());
            return this;
        }

        public StyledTable monoColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(UITables.monoRenderer());
            return this;
        }
    }

    public static record DataTable<T>(JTable table, DataTableModel<T> model, JScrollPane scroll) {
        public DataTable<T> rows(Iterable<? extends T> rows) {
            model.setRows(rows);
            refreshSorter();
            return this;
        }

        public DataTable<T> addRow(T row) {
            model.addRow(row);
            refreshSorter();
            return this;
        }

        public DataTable<T> clear() {
            model.clear();
            refreshSorter();
            return this;
        }

        public DataTable<T> onSelect(Consumer<T> consumer) {
            table.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int row = table.getSelectedRow();
                if (row < 0) {
                    return;
                }
                consumer.accept(model.rowAt(table.convertRowIndexToModel(row)));
            });
            return this;
        }

        public T rowAtView(int row) {
            if (row < 0) {
                return null;
            }
            return model.rowAt(table.convertRowIndexToModel(row));
        }

        private void refreshSorter() {
            if (table.getRowSorter() instanceof DefaultRowSorter<?, ?> sorter) {
                sorter.sort();
            }
        }
    }

    private static final class DataTableModel<T> extends AbstractTableModel {
        private final List<Column<T>> columns;
        private final List<T> rows = new ArrayList<>();

        private DataTableModel(List<Column<T>> columns) {
            this.columns = columns;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column).title();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columns.get(columnIndex).value().apply(rows.get(rowIndex));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            for (T row : rows) {
                Object value = columns.get(columnIndex).value().apply(row);
                if (value != null) {
                    return value.getClass();
                }
            }
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        private void setRows(Iterable<? extends T> items) {
            rows.clear();
            for (T item : items) {
                rows.add(item);
            }
            fireTableDataChanged();
        }

        private void addRow(T item) {
            rows.add(item);
            int row = rows.size() - 1;
            fireTableRowsInserted(row, row);
        }

        private void clear() {
            if (rows.isEmpty()) {
                return;
            }
            rows.clear();
            fireTableDataChanged();
        }

        private T rowAt(int row) {
            return rows.get(row);
        }
    }

    public static void init() {
        UIThemeSupport.init();
    }

    public static Theme theme() {
        return UIThemeSupport.theme();
    }

    public static boolean isDarkTheme() {
        return UIThemeSupport.isDarkTheme();
    }

    public static void toggleTheme() {
        UIThemeSupport.toggleTheme();
    }

    public static void applyTheme(Theme theme) {
        UIThemeSupport.applyTheme(theme);
    }

    public static FontSize fontSize() {
        return currentFontSize;
    }

    public static FontFamily fontFamily() {
        return currentFontFamily;
    }

    public static void applyFontSize(FontSize size) {
        currentFontSize = size;
        rebuildFonts();
    }

    public static void applyFontFamily(FontFamily family) {
        currentFontFamily = family;
        rebuildFonts();
    }

    private static void rebuildFonts() {
        int b = currentFontSize.base();
        String s = currentFontFamily.sans;
        String sb = currentFontFamily.sansBold;
        String d = currentFontFamily.display;
        FONT = new Font(s, Font.PLAIN, b);
        FONT_SMALL = new Font(s, Font.PLAIN, b - 1);
        FONT_BOLD = new Font(sb, Font.BOLD, b);
        FONT_STAT = new Font(sb, Font.BOLD, b + 5);
        FONT_TITLE = new Font(d, Font.BOLD, b + 5);
        FONT_HEADING = new Font(d, Font.BOLD, b + 3);
        FONT_MONO = new Font(MONO, Font.PLAIN, b - 1);
        FONT_MONO_SMALL = new Font(MONO, Font.PLAIN, b - 2);
        FONT_MONO_BOLD = new Font(MONO, Font.BOLD, b - 1);
        FONT_MONO_BIG = new Font(MONO, Font.BOLD, b + 7);
        UIThemeSupport.applyTheme(UIThemeSupport.theme());
    }

    public static JComponent badge(String raw) {
        String safe = raw == null ? "" : raw.trim();
        String key = UIThemeSupport.badgeKey(safe);
        Color[] style = UIThemeSupport.badgeStyle(raw);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, UIThemeSupport.badgeDot(key) ? 6 : 0, 0));
        chip.setOpaque(true);
        chip.setBackground(style[0]);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(style[1], 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (UIThemeSupport.badgeDot(key)) {
            JLabel dot = new JLabel("●");
            dot.setFont(FONT_MONO_SMALL);
            dot.setForeground(style[1]);
            chip.add(dot);
        }

        JLabel text = new JLabel(UIThemeSupport.badgeText(safe));
        text.setFont(FONT_MONO_BOLD);
        text.setForeground(style[1]);
        chip.add(text);
        chip.setMaximumSize(chip.getPreferredSize());
        return chip;
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
        return new Column<>(title, value, ColumnStyle.TEXT, null);
    }

    public static <T> Column<T> col(String title, Function<T, ?> value, int preferredWidth) {
        return new Column<>(title, value, ColumnStyle.TEXT, preferredWidth);
    }

    public static <T> Column<T> monoCol(String title, Function<T, ?> value) {
        return new Column<>(title, value, ColumnStyle.MONO, null);
    }

    public static <T> Column<T> monoCol(String title, Function<T, ?> value, int preferredWidth) {
        return new Column<>(title, value, ColumnStyle.MONO, preferredWidth);
    }

    public static <T> Column<T> badgeCol(String title, Function<T, ?> value) {
        return new Column<>(title, value, ColumnStyle.BADGE, null);
    }

    public static <T> Column<T> badgeCol(String title, Function<T, ?> value, int preferredWidth) {
        return new Column<>(title, value, ColumnStyle.BADGE, preferredWidth);
    }

    public static void applyPanel(JPanel panel) {
        UILayouts.applyPanel(panel);
    }

    public static JPanel panel() {
        return UILayouts.panel();
    }

    public static JPanel card() {
        return UILayouts.card();
    }

    public static JPanel formCard() {
        return UILayouts.formCard();
    }

    public static JPanel formPage(JPanel formCard) {
        return UILayouts.formPage(formCard);
    }

    public static JPanel statsRow(int cols) {
        return UILayouts.statsRow(cols);
    }

    public static JPanel stats(Stat... stats) {
        return UILayouts.stats(stats);
    }

    public static Component gap(int px) {
        return UILayouts.gap(px);
    }

    public static JPanel buttonRow(JButton... buttons) {
        return UILayouts.buttonRow(buttons);
    }

    public static JPanel splitPanel(JComponent left, JComponent right, int rightWidth) {
        return UILayouts.splitPanel(left, right, rightWidth);
    }

    public static JPanel twoColumn(JComponent left, JComponent right, int gap) {
        return UILayouts.twoColumn(left, right, gap);
    }

    public static void swap(JPanel host, JComponent content) {
        UILayouts.swap(host, content);
    }

    public static JPanel emptyState(String message) {
        return UILayouts.emptyState(message);
    }

    public static JPanel toolbar() {
        return UILayouts.toolbar();
    }

    public static JPanel toolbar(String searchHint, JTable table, String buttonText) {
        return UILayouts.toolbar(searchHint, table, buttonText);
    }

    public static JPanel toolbar(String searchHint, JTable table, JButton... rightButtons) {
        return UILayouts.toolbar(searchHint, table, rightButtons);
    }

    public static JPanel toolbarAndTable(JPanel toolbar, JComponent content) {
        return UILayouts.toolbarAndTable(toolbar, content);
    }

    public static JComponent withFooter(JComponent content, JComponent footer) {
        return UILayouts.withFooter(content, footer);
    }

    public static JPanel pageWithStats(JPanel stats, JPanel toolbar, JComponent tableContent) {
        return UILayouts.pageWithStats(stats, toolbar, tableContent);
    }

    public static JPanel statCard(String label, String value, Color colour) {
        return UILayouts.statCard(label, value, colour);
    }

    public static JPanel statCard(String label, String value, Color colour, String iconPath) {
        return UILayouts.statCard(label, value, colour, iconPath);
    }

    public static DefaultTableModel readonlyModel(String... columns) {
        return UITables.readonlyModel(columns);
    }

    public static StyledTable table(String... columns) {
        return table(readonlyModel(columns), true);
    }

    public static StyledTable table(DefaultTableModel model) {
        return table(model, true);
    }

    public static StyledTable table(DefaultTableModel model, boolean sortable) {
        JTable table = new JTable(model);
        UITables.styleTable(table, sortable);
        return new StyledTable(table, model, UITables.wrap(table));
    }

    @SafeVarargs
    public static <T> DataTable<T> table(Column<T>... columns) {
        DataTableModel<T> model = new DataTableModel<>(List.of(columns));
        JTable table = new JTable(model);
        UITables.styleTable(table, true);

        for (int i = 0; i < columns.length; i++) {
            switch (columns[i].style()) {
                case MONO -> table.getColumnModel().getColumn(i).setCellRenderer(UITables.monoRenderer());
                case BADGE -> table.getColumnModel().getColumn(i).setCellRenderer(UITables.badgeRenderer());
                default -> {
                }
            }
            if (columns[i].preferredWidth() != null) {
                table.getColumnModel().getColumn(i).setPreferredWidth(columns[i].preferredWidth());
            }
        }

        return new DataTable<>(table, model, UITables.wrap(table));
    }

    public static JTextField searchField(String placeholder, JTable table) {
        return UITables.searchField(placeholder, table);
    }

    public static JButton primaryButton(String text) {
        return UIControls.primaryButton(text);
    }

    public static JButton button(String text) {
        return UIControls.button(text);
    }

    public static JButton dangerButton(String text) {
        return UIControls.dangerButton(text);
    }

    public static JButton iconButton(String text, String iconPath, boolean primary) {
        return UIControls.iconButton(text, iconPath, primary);
    }

    public static JButton stepperButton(String text) {
        return UIControls.stepperButton(text);
    }

    public static JButton squareButton(String text) {
        return UIControls.squareButton(text);
    }

    public static JButton iconActionButton(String iconPath, String tooltip, boolean danger) {
        return UIControls.iconActionButton(iconPath, tooltip, danger);
    }

    public static JToggleButton filterPill(String text) {
        return UIControls.filterPill(text);
    }

    public static JLabel heading(String text) {
        return UIControls.heading(text);
    }

    public static JLabel dimLabel(String text) {
        return UIControls.dimLabel(text);
    }

    public static JLabel mono(String text) {
        return UIControls.mono(text);
    }

    public static JLabel monoLabel(String text, float size, Color color) {
        return UIControls.monoLabel(text, size, color);
    }

    public static JLabel monoLabelBold(String text, float size, Color color) {
        return UIControls.monoLabelBold(text, size, color);
    }

    public static JLabel countBadge(String text) {
        return UIControls.countBadge(text);
    }

    public static JPanel summaryRow(JLabel label, JLabel value) {
        return UIControls.summaryRow(label, value);
    }

    public static JPanel field(String label, JComponent input) {
        return UIControls.field(label, input);
    }

    public static JPanel formRow(JComponent... fields) {
        return UIControls.formRow(fields);
    }

    public static JTextArea textArea(String text, int rows) {
        return UIControls.textArea(text, rows);
    }

    public static JPanel textAreaField(String label, String text, int rows) {
        return UIControls.textAreaField(label, text, rows);
    }

    public static JPanel detailRow(String label, String value) {
        return UIControls.detailRow(label, value);
    }

    public static JPanel detailCard(String title, Detail... details) {
        return detailCard(title, details, new JButton[0]);
    }

    public static JPanel detailCard(String title, Detail[] details, JButton... actions) {
        return UIControls.detailCard(title, details, actions);
    }

    public static JPanel detailLine(String labelText, String valueText) {
        return UIControls.detailLine(labelText, valueText, 132, false);
    }

    public static JPanel detailLine(String labelText, String valueText, boolean wrapValue) {
        return UIControls.detailLine(labelText, valueText, 132, wrapValue);
    }

    public static JPanel detailLine(String labelText, String valueText, int labelWidth, boolean wrapValue) {
        return UIControls.detailLine(labelText, valueText, labelWidth, wrapValue);
    }

    public static JLabel detailValue(String valueText) {
        return UIControls.detailValue(valueText);
    }

    public static JComponent fullWidth(JComponent component) {
        return UIControls.fullWidth(component);
    }

    public static JLabel sectionLabel(String text) {
        return UIControls.sectionLabel(text);
    }

    public static JTabbedPane innerTabs() {
        return UILayouts.innerTabs();
    }

    public static JTabbedPane innerTabs(Tab... tabs) {
        return UILayouts.innerTabs(tabs);
    }

    public static JLabel label(String text, Font font, Color colour) {
        return UIControls.label(text, font, colour);
    }

    public static JLabel labelCenter(String text, Font font, Color colour) {
        return UIControls.labelCenter(text, font, colour);
    }

    public static JLabel title(String text) {
        return UIControls.title(text);
    }

    public static JLabel subtitle(String text) {
        return UIControls.subtitle(text);
    }

    public static JLabel errorLabel() {
        return UIControls.errorLabel();
    }

    public static JComponent avatarBadge(String text, int size) {
        return UIControls.avatarBadge(text, size);
    }

    public static FlatSVGIcon icon(String path, int size, Color colour) {
        return UIControls.icon(path, size, colour);
    }

    public static JTextField inputField(String placeholder) {
        return UIControls.inputField(placeholder);
    }

    public static JPasswordField passwordField(String placeholder) {
        return UIControls.passwordField(placeholder);
    }

    public static JButton primaryButtonWide(String text) {
        return UIControls.primaryButtonWide(text);
    }

    public static JPanel centeredPanel() {
        return UILayouts.centeredPanel();
    }

    public static JPanel vcard(int padX, int padY, int width, int height) {
        return UILayouts.vcard(padX, padY, width, height);
    }

    public static JPanel transparentPanel(int vgap) {
        return UILayouts.transparentPanel(vgap);
    }

    public static void applyPanelNoPad(JPanel panel) {
        UILayouts.applyPanelNoPad(panel);
    }

    public static JPanel cardPanel() {
        return UILayouts.cardPanel();
    }

    public static JPanel flowRow(int hgap) {
        return UILayouts.flowRow(hgap);
    }

    public static JPanel gridRow(int cols, int hgap) {
        return UILayouts.gridRow(cols, hgap);
    }

    public static JPanel paddedPanel(int top, int left, int bottom, int right) {
        return UILayouts.paddedPanel(top, left, bottom, right);
    }

    public static TableCellRenderer plainTableRenderer() {
        return UIControls.plainTableRenderer();
    }

    public static void notifyInfo(Component owner, String message) {
        UINotifier.notifyInfo(owner, message);
    }

    public static void notifySuccess(Component owner, String message) {
        UINotifier.notifySuccess(owner, message);
    }

    public static void notifyError(Component owner, String message) {
        UINotifier.notifyError(owner, message);
    }

    public static JComponent statusBanner(String labelText, String message, Color tone) {
        Color border = mix(BORDER, tone, isDarkTheme() ? 0.55f : 0.35f);
        Color fill = mix(BG, tone, isDarkTheme() ? 0.18f : 0.08f);

        JPanel banner = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), FIELD_ARC, FIELD_ARC);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, FIELD_ARC, FIELD_ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(8, 12, 8, 12));
        banner.add(monoLabelBold(labelText, 11f, tone), BorderLayout.WEST);
        banner.add(monoLabel(message == null ? "" : message, 11f, isDarkTheme() ? TEXT_DIM : TEXT_MUTED), BorderLayout.CENTER);
        return banner;
    }

    private static Color mix(Color base, Color accent, float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        int red = Math.round(base.getRed() + (accent.getRed() - base.getRed()) * clamped);
        int green = Math.round(base.getGreen() + (accent.getGreen() - base.getGreen()) * clamped);
        int blue = Math.round(base.getBlue() + (accent.getBlue() - base.getBlue()) * clamped);
        return new Color(red, green, blue);
    }
}
