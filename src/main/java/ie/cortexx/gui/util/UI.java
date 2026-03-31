package ie.cortexx.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.Map;

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

    public static final Color BG         = new Color(0x0f1117);  // darkest bg, used on main content area
    public static final Color BG_CARD    = new Color(0x181a20);  // slightly lighter, used on cards/tables/sidebar
    public static final Color BG_HOVER   = new Color(0x1e2028);  // hover states and alternating table rows
    public static final Color BG_INPUT   = new Color(0x13151b);  // text field and combo box backgrounds
    public static final Color BORDER     = new Color(0x2a2d37);  // all borders, dividers, grid lines
    public static final Color TEXT       = new Color(0xe4e6eb);  // primary text colour
    public static final Color TEXT_DIM   = new Color(0x8b8fa3);  // secondary text, labels, table headers
    public static final Color TEXT_MUTED = new Color(0x5c5f6e);  // placeholder text, disabled items
    public static final Color ACCENT     = new Color(0x4f8cff);  // primary blue for buttons, links, focus rings
    public static final Color GREEN      = new Color(0x34d399);  // success states: normal, delivered, in stock
    public static final Color YELLOW     = new Color(0xfbbf24);  // warning states: suspended, low stock
    public static final Color RED        = new Color(0xf87171);  // error states: in_default, out of stock
    public static final Color ORANGE     = new Color(0xfb923c);  // in-progress states: processing, on_credit
    public static final Color PURPLE     = new Color(0xa78bfa);  // misc: dispatched, flexible discount, manager role

    // -- fonts --
    // "Poppins Medium" is a clean sans-serif used for all ui text.
    // "JetBrains Mono" is a monospace font used for numbers, prices, product ids.

    //should probably include them in the codebase to install

    // if the user doesnt have these installed, java falls back to the system default
    // (segoe ui / consolas on windows, sf pro / menlo on mac) which still looks fine.

    private static final String SANS = "Poppins Medium";
    private static final String MONO = "JetBrains Mono";

    public static final Font FONT          = new Font(SANS, Font.PLAIN, 13);  // default body text
    public static final Font FONT_SMALL    = new Font(SANS, Font.PLAIN, 12);  // labels, secondary text
    public static final Font FONT_BOLD     = new Font(SANS, Font.BOLD, 13);   // emphasis, detail values
    public static final Font FONT_TITLE    = new Font(SANS, Font.BOLD, 18);   // page titles, brand name
    public static final Font FONT_MONO     = new Font(MONO, Font.PLAIN, 12);  // prices, ids, codes in tables
    public static final Font FONT_MONO_BIG = new Font(MONO, Font.BOLD, 20);   // big numbers in stat cards

    // -- badge colour map --
    // used by the badge renderer to colour status pills in tables.
    // the key is the status text (uppercased, underscored), the value is [bg, fg].
    // bg is the fg colour at ~12% opacity so it looks like a tinted pill.
    // to add a new badge, just add another badge() entry here.

    private static final Map<String, Color[]> BADGES = Map.ofEntries(
        // green: positive/ok states
        badge("NORMAL", GREEN),      badge("DELIVERED", GREEN),
        badge("IN_STOCK", GREEN),    badge("ACTIVE", GREEN),
        badge("CASH", GREEN),        badge("PHARMACIST", GREEN),
        // yellow: warning states
        badge("SUSPENDED", YELLOW),  badge("LOW_STOCK", YELLOW),
        // orange: in-progress states
        badge("PROCESSING", ORANGE), badge("ON_CREDIT", ORANGE),
        badge("PACKED", ORANGE),
        // red: error/blocked states
        badge("IN_DEFAULT", RED),    badge("CANCELLED", RED),
        badge("OUT_OF_STOCK", RED),
        // blue: informational
        badge("ACCEPTED", ACCENT),   badge("CREDIT_CARD", ACCENT),
        badge("DEBIT_CARD", ACCENT), badge("FIXED", ACCENT),
        badge("ADMIN", ACCENT),
        // purple: special
        badge("DISPATCHED", PURPLE), badge("FLEXIBLE", PURPLE),
        badge("MANAGER", PURPLE)
    );

    // helper to build a badge entry. creates a translucent bg from the fg colour.
    private static Map.Entry<String, Color[]> badge(String key, Color fg) {
        return Map.entry(key, new Color[]{
            new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 30), fg
        });
    }


    // -- init --
    // call this once in Main.java right after FlatDarkLaf.setup().
    // it sets all the UIManager keys that flatlaf reads to style every component.
    // without this, flatlaf uses its own default dark colours which dont match our design.
    // ref: https://www.formdev.com/flatlaf/how-to-customize/

    public static void init() {
        // global font for everything
        UIManager.put("defaultFont", FONT);

        // rounded corners on buttons, text fields, checkboxes
        // ref: https://www.formdev.com/flatlaf/customizing/ ("Rounded or Square Corners")
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        // thin rounded scrollbars instead of the chunky default
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 8);

        // main panel bg
        UIManager.put("Panel.background", BG);

        // table styling. almost every panel has a table so this matters a lot.
        // ref: https://www.formdev.com/flatlaf/components/table/
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.alternateRowColor", BG_HOVER);  // zebra striping
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);      // horizontal only looks cleaner
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", ACCENT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.rowHeight", 40);                 // taller rows = easier to read
        UIManager.put("TableHeader.background", BG_HOVER);
        UIManager.put("TableHeader.foreground", TEXT_DIM);

        // inner tabs use an underline indicator instead of the boxy default.
        // this matches the react prototype's tab style.
        // ref: https://www.formdev.com/flatlaf/customizing/ ("Tabbed Pane")
        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("TabbedPane.hoverColor", BG_HOVER);

        // primary buttons get the accent colour automatically
        UIManager.put("Button.default.background", ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusWidth", 1);

        // text fields and combo boxes
        // ref: https://www.formdev.com/flatlaf/components/textfield/
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.placeholderForeground", TEXT_MUTED);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("List.background", BG_CARD);
        UIManager.put("List.selectionBackground", ACCENT);
    }


    // -- layout helpers --
    // these build the common layout patterns so panels dont repeat boilerplate.

    /** applies dark bg + padding + BorderLayout to an existing panel (like `this`).
     *  use this at the top of every panel constructor. */
    public static void applyPanel(JPanel p) {
        p.setLayout(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    /** creates a new dark panel. use when you need a sub-view (e.g. inside a tab). */
    public static JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    /** card with lighter bg and border. use for grouping related content. */
    public static JPanel card() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(16, 20, 16, 20)));
        return p;
    }

    /** card with vertical stacking layout. use for forms (fields + gaps + button). */
    public static JPanel formCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(16, 20, 16, 20)));
        return p;
    }

    /** wraps a formCard inside a panel so it doesnt stretch to fill the whole area.
     *  use for settings tabs where the form should sit at the top, not fill the screen. */
    public static JPanel formPage(JPanel formCard) {
        JPanel p = panel();
        p.add(formCard, BorderLayout.NORTH);
        return p;
    }

    /** horizontal row of stat cards. pass how many columns you want. */
    public static JPanel statsRow(int cols) {
        JPanel p = new JPanel(new GridLayout(1, cols, 12, 0));
        p.setOpaque(false); // transparent so the parent bg shows through
        return p;
    }

    /** vertical spacer. shortcut for Box.createVerticalStrut(). */
    public static Component gap(int px) {
        return Box.createVerticalStrut(px);
    }

    /** horizontal row of buttons, left-aligned. */
    public static JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (var b : buttons) p.add(b);
        return p;
    }

    /** left/right split with a fixed-width right panel.
     *  use for POS (products left, cart right) or customers (table left, detail right). */
    public static JPanel splitPanel(JComponent left, JComponent right, int rightWidth) {
        right.setPreferredSize(new Dimension(rightWidth, 0));
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /** centered grey message for when theres nothing to show. */
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
    // the toolbar sits between stat cards and the table.
    // it always has something on the left (search) and something on the right (buttons).

    /** empty toolbar. add things to WEST and EAST yourself. */
    public static JPanel toolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 12, 0));
        return p;
    }

    /** toolbar with search on the left and a primary button on the right.
     *  this is the most common toolbar in the app (stock, customers, users). */
    public static JPanel toolbar(String searchHint, JTable table, String buttonText) {
        JPanel bar = toolbar();
        bar.add(searchField(searchHint, table), BorderLayout.WEST);
        bar.add(primaryButton(buttonText), BorderLayout.EAST);
        return bar;
    }

    /** toolbar with search on the left and multiple buttons on the right.
     *  use for catalogue (Sync + Place Order) or reports (Generate + Export). */
    public static JPanel toolbar(String searchHint, JTable table, JButton... rightButtons) {
        JPanel bar = toolbar();
        bar.add(searchField(searchHint, table), BorderLayout.WEST);
        bar.add(buttonRow(rightButtons), BorderLayout.EAST);
        return bar;
    }

    /** stacks a toolbar on top and content below.
     *  the content is usually t.scroll() but can be anything (e.g. form + table). */
    public static JPanel toolbarAndTable(JPanel toolbar, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(toolbar, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** the full "stats + toolbar + table" layout that most panels use.
     *  stats go in NORTH, toolbar+table go in CENTER. returns the assembled panel. */
    public static JPanel pageWithStats(JPanel stats, JPanel toolbar, JComponent tableContent) {
        var p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(stats, BorderLayout.NORTH);
        p.add(toolbarAndTable(toolbar, tableContent), BorderLayout.CENTER);
        return p;
    }


    // -- stat card --

    /** summary card with a coloured icon square, a label, and a big mono number.
     *  @param label  small grey text above the number (e.g. "Total Products")
     *  @param value  the big number (e.g. "14" or "£806.00")
     *  @param colour the accent colour for the icon square (e.g. UI.ACCENT, UI.RED) */
    public static JPanel statCard(String label, String value, Color colour) {
        JPanel c = new JPanel(new BorderLayout(12, 0));
        c.setBackground(BG_CARD);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 18, 14, 18)));

        // the coloured square on the left. we paint it manually so we can
        // use a translucent fill with rounded corners.
        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                var g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(colour.getRed(), colour.getGreen(),
                    colour.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setOpaque(false);
        c.add(icon, BorderLayout.WEST);

        // label + value stacked vertically on the right
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
    // tables are the main part of almost every panel. this method creates a fully styled,
    // non-editable, sortable/filterable JTable with alternating row colours.
    // ref: https://www.formdev.com/flatlaf/components/table/
    // ref: https://docs.oracle.com/javase/tutorial/uiswing/components/table.html

    /** creates a styled table. chain .badgeColumn() and .monoColumn() to set renderers,
     *  then use .model() to add rows and .scroll() to get the scroll pane for layout.
     *
     *  example:
     *    var t = UI.table("ID", "Name", "Qty", "Status");
     *    t.monoColumn(0).monoColumn(2).badgeColumn(3);
     *    t.model().addRow(new Object[]{"100 00001", "Paracetamol", 121, "IN_STOCK"});
     *    add(t.scroll(), BorderLayout.CENTER); */
    public static StyledTable table(String... columns) {
        // non-editable model so users cant accidentally change table data
        var model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        var table = new JTable(model);
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
        table.setFillsViewportHeight(true); // fills the scroll pane even if few rows
        table.getTableHeader().setFont(new Font(SANS, Font.BOLD, 11));
        table.getTableHeader().setBackground(BG_HOVER);
        table.getTableHeader().setForeground(TEXT_DIM);

        // TableRowSorter enables column sorting (click header) and row filtering
        // (used by searchField). without this, search wont work.
        table.setRowSorter(new TableRowSorter<>(model));

        // alternating row colours + cell padding for readability
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? BG_CARD : BG_HOVER);
                setBorder(new EmptyBorder(0, 16, 0, 16)); // horizontal padding
                return this;
            }
        });

        // wrap in scroll pane with matching border
        var scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);

        return new StyledTable(table, model, scroll);
    }

    /** wrapper around JTable + model + scroll pane. lets you chain column styling. */
    public static record StyledTable(JTable table, DefaultTableModel model, JScrollPane scroll) {
        /** makes this column show coloured status pills (e.g. "NORMAL" in green) */
        public StyledTable badgeColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(badgeRenderer());
            return this;
        }
        /** makes this column use monospace font (for ids, prices, quantities) */
        public StyledTable monoColumn(int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(monoRenderer());
            return this;
        }
    }

    // badge renderer: looks up the cell text in the BADGES map and colours it.
    // handles case differences and underscores automatically.
    // if the text isnt in the map, it shows in grey (unknown status).
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

    // mono renderer: same as the default renderer but uses monospace font.
    // keeps the alternating row colours and padding consistent.
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
    // creates a text field that live-filters a JTable as you type.
    // uses RowFilter.regexFilter with case-insensitive matching across all columns.
    // the table must have a TableRowSorter set (UI.table() does this automatically).
    // ref: https://www.formdev.com/flatlaf/components/textfield/
    // ref: https://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting

    /** search field that filters the given table on every keystroke.
     *  @param placeholder  greyed-out hint text (e.g. "Search stock...")
     *  @param table        the JTable to filter */
    public static JTextField searchField(String placeholder, JTable table) {
        var field = new JTextField(20);
        // flatlaf supports placeholder text natively via this client property
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setFont(FONT);
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { go(); }
            public void removeUpdate(DocumentEvent e)  { go(); }
            public void changedUpdate(DocumentEvent e) { go(); }
            @SuppressWarnings("unchecked")
            private void go() {
                var sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
                String t = field.getText();
                // (?i) = case insensitive. searches all columns at once.
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });
        return field;
    }


    // -- buttons --

    /** blue accent button. use for the main action on a page (e.g. "+ Add Stock").
     *  flatlaf renders "default" buttons with the accent bg colour automatically. */
    public static JButton primaryButton(String text) {
        var b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "default");
        b.setFont(FONT_BOLD);
        return b;
    }

    /** regular secondary button. */
    public static JButton button(String text) {
        var b = new JButton(text);
        b.setFont(FONT);
        return b;
    }

    /** red text button for destructive actions like delete. */
    public static JButton dangerButton(String text) {
        var b = new JButton(text);
        b.setForeground(RED);
        b.setFont(FONT);
        return b;
    }

    /** pill-shaped toggle button for filter bars (e.g. "All" / "Low Stock").
     *  add to a ButtonGroup so only one can be active at a time.
     *  ref: <a href="https://www.formdev.com/flatlaf/customizing/">...</a> ("Round components") */
    public static JToggleButton filterPill(String text) {
        var b = new JToggleButton(text);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setFont(FONT_SMALL);
        return b;
    }


    // -- labels --

    /** bold 16px heading. use for section titles. */
    public static JLabel heading(String text) {
        var l = new JLabel(text);
        l.setFont(new Font(SANS, Font.BOLD, 16));
        l.setForeground(TEXT);
        return l;
    }

    /** small grey label. use for descriptions, hints, timestamps. */
    public static JLabel dimLabel(String text) {
        var l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_DIM);
        return l;
    }

    /** monospace label. use for prices, ids, account numbers. */
    public static JLabel mono(String text) {
        var l = new JLabel(text);
        l.setFont(FONT_MONO);
        l.setForeground(TEXT);
        return l;
    }


    // -- forms --
    // form helpers for building settings pages, create dialogs, etc.

    /** label above an input component. works with JTextField, JComboBox, JScrollPane, etc. */
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
        // cap text field height so it doesnt stretch in BoxLayout
        if (input instanceof JTextField tf)
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(input);
        return p;
    }

    /** horizontal row of form fields with even spacing between them. */
    public static JPanel formRow(JComponent... fields) {
        var row = new JPanel(new GridLayout(1, fields.length, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 12, 0));
        for (var f : fields) row.add(f);
        return row;
    }

    /** styled monospace textarea. use for template editing, notes, etc. */
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

    /** label + scrollable textarea in one call. use for template fields in settings. */
    public static JPanel textAreaField(String label, String text, int rows) {
        return field(label, new JScrollPane(textArea(text, rows)));
    }


    // -- detail rows --
    // used in info/detail panels (e.g. customer detail on the right side).
    // shows a key on the left and a value on the right with a bottom border.

    /** key-value detail row with bottom divider. */
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


    // -- inner tabs --
    // for sub-navigation within a panel (e.g. "Point of Sale" / "Sale History").
    // uses the underline tab style set in init().

    /** creates a JTabbedPane with underline-style tabs. */
    public static JTabbedPane innerTabs() {
        var tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT);
        tabs.setBackground(BG);
        return tabs;
    }


    // -- label helpers --
    // creating a JLabel normally takes 3-4 lines (new, setFont, setForeground, setAlignment).
    // these one-liners cover the common cases so panels stay short.

    /** label with custom font and colour, left aligned. */
    public static JLabel label(String text, Font font, Color colour) {
        var lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(colour);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** label with custom font and colour, center aligned. */
    public static JLabel labelCenter(String text, Font font, Color colour) {
        var lbl = label(text, font, colour);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    /** big title label (24px bold, primary text colour). */
    public static JLabel title(String text) {
        return label(text, FONT_TITLE.deriveFont(24f), TEXT);
    }

    /** subtitle / secondary info label (small dim text). */
    public static JLabel subtitle(String text) {
        return label(text, FONT_SMALL, TEXT_DIM);
    }

    /** error label (small red text, starts with blank space so it takes up room). */
    public static JLabel errorLabel() {
        var lbl = label(" ", FONT_SMALL, RED);
        return lbl;
    }


    // -- input helpers --
    // text fields normally need placeholder + maxSize + alignment = 3 lines.
    // these do it in one call.

    /** text field with placeholder text, full width, fixed height. */
    public static JTextField inputField(String placeholder) {
        var tf = new JTextField();
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    /** password field with placeholder text, full width, fixed height. */
    public static JPasswordField passwordField(String placeholder) {
        var pf = new JPasswordField();
        pf.putClientProperty("JTextField.placeholderText", placeholder);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }


    // -- button helpers --
    // making a full-width button normally takes 3 lines (create + maxSize + alignment).

    /** primary button stretched to full width. good for login, save, etc. */
    public static JButton primaryButtonWide(String text) {
        var btn = primaryButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }


    // -- layout helpers --
    // centering a single component (like a login card) takes 2-3 lines every time.

    /** panel that centers its children using GridBagLayout. dark bg. */
    public static JPanel centeredPanel() {
        var p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        return p;
    }

    /** vertical card with custom padding and preferred size. good for login, dialogs. */
    public static JPanel vcard(int padX, int padY, int width, int height) {
        var p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(padY, padX, padY, padX)));
        p.setPreferredSize(new Dimension(width, height));
        return p;
    }

    /** transparent panel with BorderLayout and optional vertical gap. */
    public static JPanel transparentPanel(int vgap) {
        var p = new JPanel(new BorderLayout(0, vgap));
        p.setOpaque(false);
        return p;
    }
}
