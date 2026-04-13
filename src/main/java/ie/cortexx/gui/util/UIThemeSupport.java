package ie.cortexx.gui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Insets;
import java.util.Map;

/**
 * Owns theme state and all FlatLaf/UIManager palette configuration.
 *
 * <p>The public {@link UI} class stays as the stable facade panels call into,
 * while this helper keeps the mutable theme implementation isolated in one
 * place.</p>
 */
final class UIThemeSupport {
    private static UI.Theme currentTheme = UI.Theme.LIGHT;
    private static Map<String, Color[]> badges = buildBadges();

    private UIThemeSupport() {
    }

    static void init() {
        applyTheme(currentTheme);
    }

    static UI.Theme theme() {
        return currentTheme;
    }

    static boolean isDarkTheme() {
        return currentTheme.isDark();
    }

    static void toggleTheme() {
        applyTheme(isDarkTheme() ? UI.Theme.LIGHT : UI.Theme.DARK);
    }

    static void applyTheme(UI.Theme theme) {
        currentTheme = theme;
        installLookAndFeel(theme);
        applyPalette(theme);
        badges = buildBadges();
        boolean dark = theme.isDark();

        Color neutralOutline = outlineColor();

        UIManager.put("defaultFont", UI.FONT);

        UIManager.put("Component.arc", UI.CARD_ARC);
        UIManager.put("Button.arc", UI.BUTTON_ARC);
        UIManager.put("TextComponent.arc", UI.FIELD_ARC);
        UIManager.put("CheckBox.arc", UI.BUTTON_ARC);
        UIManager.put("ComboBox.arc", UI.FIELD_ARC);
        UIManager.put("Spinner.arc", UI.FIELD_ARC);
        UIManager.put("ProgressBar.arc", UI.BUTTON_ARC);
        UIManager.put("TabbedPane.tabArc", UI.TAB_ARC);
        UIManager.put("TabbedPane.tabSelectionArc", UI.TAB_ARC);
        UIManager.put("TabbedPane.cardTabArc", UI.TAB_ARC);
        UIManager.put("TabbedPane.buttonArc", UI.TAB_ARC);

        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 8);

        UIManager.put("Panel.background", UI.BG);

        UIManager.put("Table.background", UI.BG_CARD);
        UIManager.put("Table.alternateRowColor", UI.BG_HOVER);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", UI.BORDER);
        UIManager.put("Table.selectionBackground", UI.ACCENT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("TableHeader.background", UI.BG_HOVER);
        UIManager.put("TableHeader.foreground", UI.TEXT_DIM);

        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.underlineColor", UI.ACCENT);
        UIManager.put("TabbedPane.selectedBackground", UI.BG_CARD);
        UIManager.put("TabbedPane.selectedForeground", UI.TEXT);
        UIManager.put("TabbedPane.hoverColor", UI.BG_HOVER);

        UIManager.put("Button.default.background", UI.ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.hoverBorderColor", neutralOutline);
        UIManager.put("Button.focusedBorderColor", neutralOutline);
        UIManager.put("Button.default.hoverBorderColor", neutralOutline);
        UIManager.put("Button.default.focusedBorderColor", neutralOutline);
        UIManager.put("Component.focusColor", neutralOutline);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusedBorderColor", neutralOutline);

        UIManager.put("TextField.background", UI.BG_INPUT);
        UIManager.put("TextField.focusedBackground", UI.BG_INPUT);
        UIManager.put("TextField.borderColor", UI.BORDER);
        UIManager.put("TextField.focusedBorderColor", neutralOutline);
        UIManager.put("TextField.focusWidth", 1);
        UIManager.put("TextField.innerFocusWidth", 0);
        UIManager.put("TextField.margin", new Insets(0, 14, 0, 14));
        UIManager.put("TextField.foreground", UI.TEXT);
        UIManager.put("TextField.caretForeground", UI.TEXT);
        UIManager.put("TextField.placeholderForeground", UI.TEXT_MUTED);

        UIManager.put("PasswordField.background", UI.BG_INPUT);
        UIManager.put("PasswordField.focusedBackground", UI.BG_INPUT);
        UIManager.put("PasswordField.borderColor", UI.BORDER);
        UIManager.put("PasswordField.focusedBorderColor", neutralOutline);
        UIManager.put("PasswordField.focusWidth", 1);
        UIManager.put("PasswordField.innerFocusWidth", 0);
        UIManager.put("PasswordField.margin", new Insets(0, 14, 0, 14));
        UIManager.put("PasswordField.foreground", UI.TEXT);
        UIManager.put("PasswordField.caretForeground", UI.TEXT);

        UIManager.put("ComboBox.background", UI.BG_INPUT);
        UIManager.put("ComboBox.borderColor", UI.BORDER);
        UIManager.put("ComboBox.focusedBorderColor", neutralOutline);
        UIManager.put("ComboBox.focusWidth", 1);
        UIManager.put("ComboBox.innerFocusWidth", 0);
        UIManager.put("ComboBox.padding", new Insets(0, 14, 0, 10));

        UIManager.put("List.background", UI.BG_CARD);
        UIManager.put("List.selectionBackground", UI.ACCENT);

        UIManager.put("OptionPane.background", UI.BG_CARD);
        UIManager.put("OptionPane.messageForeground", UI.TEXT);
        UIManager.put("OptionPane.buttonFont", UI.FONT_BOLD);
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");

        UIManager.put("[style]ToggleButton.sidebarNav",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:11,12,11,12; iconTextGap:10; " +
            "background:" + hex(UI.BG_CARD) + "; foreground:" + hex(UI.TEXT_DIM) + "; borderColor:" + hex(UI.BG_CARD) + "; " +
            "hoverBackground:" + hex(UI.BG_HOVER) + "; hoverBorderColor:" + hex(UI.BORDER) + "; " +
            "focusedBorderColor:" + hex(neutralOutline) + "; " +
            "selectedBackground:" + hex(UI.ACCENT) + "; selectedForeground:" + hex(Color.WHITE) +
            "; selectedBorderColor:" + hex(UI.BORDER));

        UIManager.put("[style]Button.sidebarIcon",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,8,8,8; " +
            "background:" + hex(UI.BG_CARD) + "; foreground:" + hex(UI.TEXT_DIM) + "; borderColor:" + hex(UI.BORDER) + "; " +
            "hoverBackground:" + hex(UI.BG_HOVER) + "; hoverBorderColor:" + hex(UI.BORDER) + "; " +
            "focusedBorderColor:" + hex(neutralOutline));

        UIManager.put("[style]Button.themeSwitch",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:7,10,7,10; " +
            "background:" + hex(UI.BG_CARD) + "; foreground:" + hex(UI.TEXT) + "; borderColor:" + hex(UI.BORDER) + "; " +
            "hoverBackground:" + hex(UI.BG_HOVER) + "; hoverBorderColor:" + hex(UI.BORDER) + "; " +
            "focusedBorderColor:" + hex(neutralOutline));

        UIManager.put("[style]Button.secondary",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:" + hex(dark ? new Color(0x222735) : Color.WHITE) +
            "; foreground:" + hex(UI.TEXT) + "; borderColor:" + hex(dark ? new Color(0x394055) : UI.BORDER) +
            "; hoverBorderColor:" + hex(UI.BORDER) + "; focusedBorderColor:" + hex(neutralOutline) +
            "; hoverBackground:" + hex(dark ? new Color(0x2b3142) : UI.BG_HOVER) +
            "; pressedBackground:" + hex(dark ? new Color(0x30384a) : UI.BG_HOVER.darker()));

        UIManager.put("[style]Button.primary",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:" + hex(UI.ACCENT) + "; foreground:" + hex(dark ? Color.WHITE : UI.BG_CARD) +
            "; borderColor:" + hex(UI.ACCENT) + "; hoverBorderColor:" + hex(dark ? UI.ACCENT : UI.BORDER) +
            "; focusedBorderColor:" + hex(neutralOutline) +
            "; hoverBackground:" + hex(dark ? new Color(0x6399ff) : UI.BORDER) +
            "; pressedBackground:" + hex(dark ? new Color(0x3d7ef6) : UI.BORDER.darker()));

        UIManager.put("[style]Button.danger",
            "arc:" + UI.BUTTON_ARC + "; focusWidth:0; innerFocusWidth:0; borderWidth:1; " +
            "margin:8,14,8,14; " +
            "background:" + hex(dark ? new Color(0x2a1d1f) : new Color(0xfff1d8)) +
            "; foreground:" + hex(dark ? new Color(0xf6b3b3) : new Color(0x8b4b2b)) +
            "; borderColor:" + hex(dark ? new Color(0x5c363b) : new Color(0xf0dec0)) +
            "; hoverBorderColor:" + hex(UI.BORDER) + "; focusedBorderColor:" + hex(neutralOutline) + "; " +
            "hoverBackground:" + hex(dark ? new Color(0x332326) : new Color(0xffecd0)) +
            "; pressedBackground:" + hex(dark ? new Color(0x3b282c) : new Color(0xfbe3bc)));

        UIManager.put("OptionPane.background", UI.BG_CARD);
        UIManager.put("OptionPane.messageForeground", UI.TEXT);
        UIManager.put("OptionPane.buttonFont", UI.FONT_BOLD);
        UIManager.put("Panel.background", UI.BG);
    }

    static Color[] badgeStyle(String raw) {
        String safe = raw == null ? "" : raw.trim();
        return badges.getOrDefault(badgeKey(safe), new Color[]{UI.BG_HOVER, UI.TEXT_DIM});
    }

    static String badgeKey(String raw) {
        String key = raw == null ? "" : raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        if (key.startsWith("FIXED")) {
            return "FIXED";
        }
        if (key.startsWith("FLEXIBLE")) {
            return "FLEXIBLE";
        }
        if (key.startsWith("LOW_STOCK")) {
            return "LOW_STOCK";
        }
        if (key.startsWith("OUT_OF_STOCK")) {
            return "OUT_OF_STOCK";
        }
        if (key.startsWith("IN_STOCK")) {
            return "IN_STOCK";
        }
        if (key.startsWith("ON_CREDIT")) {
            return "ON_CREDIT";
        }
        if (key.startsWith("CREDIT_CARD")) {
            return "CREDIT_CARD";
        }
        if (key.startsWith("DEBIT_CARD")) {
            return "DEBIT_CARD";
        }
        return key;
    }

    static String badgeText(String raw) {
        return raw == null ? "" : raw.replace('_', ' ');
    }

    static boolean badgeDot(String key) {
        return switch (key) {
            case "NORMAL", "SUSPENDED", "IN_DEFAULT", "ACTIVE",
                "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK",
                "ACCEPTED", "PROCESSING", "PACKED", "DISPATCHED", "DELIVERED", "CANCELLED" -> true;
            default -> false;
        };
    }

    static Color outlineColor() {
        return isDarkTheme() ? UI.BORDER.brighter() : UI.BORDER;
    }

    static String hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static void installLookAndFeel(UI.Theme theme) {
        try {
            UIManager.setLookAndFeel(theme.isDark() ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException("Unable to switch theme", e);
        }
    }

    private static void applyPalette(UI.Theme theme) {
        switch (theme) {
            case DARK -> {
                UI.BG = new Color(0x0f1117);
                UI.BG_CARD = new Color(0x181a20);
                UI.BG_HOVER = new Color(0x1e2028);
                UI.BG_INPUT = new Color(0x13151b);
                UI.BORDER = new Color(0x2a2d37);
                UI.TEXT = new Color(0xe4e6eb);
                UI.TEXT_DIM = new Color(0x8b8fa3);
                UI.TEXT_MUTED = new Color(0x5c5f6e);
                UI.ACCENT = new Color(0x4f8cff);
                UI.GREEN = new Color(0x34d399);
                UI.YELLOW = new Color(0xfbbf24);
                UI.RED = new Color(0xf87171);
                UI.ORANGE = new Color(0xfb923c);
                UI.PURPLE = new Color(0xa78bfa);
            }
            case LIGHT -> {
                UI.BG = new Color(0xf5f6f7);
                UI.BG_CARD = Color.WHITE;
                UI.BG_HOVER = new Color(0xeff1f2);
                UI.BG_INPUT = Color.WHITE;
                UI.BORDER = new Color(0xd7dbdf);
                UI.TEXT = new Color(0x323130);
                UI.TEXT_DIM = new Color(0x6f7378);
                UI.TEXT_MUTED = new Color(0xa1a6ad);
                UI.ACCENT = new Color(0x323130);
                UI.GREEN = new Color(0x8fbe6f);
                UI.YELLOW = new Color(0xe3b96b);
                UI.RED = new Color(0xe29595);
                UI.ORANGE = new Color(0xd8a96f);
                UI.PURPLE = new Color(0xb4bdd2);
            }
            case GREEN -> {
                UI.BG = new Color(0xf3f7f3);
                UI.BG_CARD = Color.WHITE;
                UI.BG_HOVER = new Color(0xe7efe6);
                UI.BG_INPUT = Color.WHITE;
                UI.BORDER = new Color(0xccd8cc);
                UI.TEXT = new Color(0x223126);
                UI.TEXT_DIM = new Color(0x5f7465);
                UI.TEXT_MUTED = new Color(0x90a095);
                UI.ACCENT = new Color(0x2f7d4a);
                UI.GREEN = new Color(0x2f7d4a);
                UI.YELLOW = new Color(0xb48b30);
                UI.RED = new Color(0xc46262);
                UI.ORANGE = new Color(0xc9823b);
                UI.PURPLE = new Color(0x7692b7);
            }
            case BLUE -> {
                UI.BG = new Color(0xf2f6fb);
                UI.BG_CARD = Color.WHITE;
                UI.BG_HOVER = new Color(0xe6edf7);
                UI.BG_INPUT = Color.WHITE;
                UI.BORDER = new Color(0xc8d5e6);
                UI.TEXT = new Color(0x1e2d3d);
                UI.TEXT_DIM = new Color(0x5c7086);
                UI.TEXT_MUTED = new Color(0x8ea0b2);
                UI.ACCENT = new Color(0x2d6fb7);
                UI.GREEN = new Color(0x3d9b7a);
                UI.YELLOW = new Color(0xb4883b);
                UI.RED = new Color(0xc9686a);
                UI.ORANGE = new Color(0xc97945);
                UI.PURPLE = new Color(0x6f89c8);
            }
        }
    }

    private static Map<String, Color[]> buildBadges() {
        return Map.ofEntries(
            badge("NORMAL", UI.GREEN),      badge("DELIVERED", UI.GREEN),
            badge("IN_STOCK", UI.GREEN),    badge("ACTIVE", UI.GREEN),
            badge("CASH", UI.GREEN),        badge("PHARMACIST", UI.GREEN),
            badge("SUSPENDED", UI.YELLOW),  badge("LOW_STOCK", UI.YELLOW),
            badge("PROCESSING", UI.ORANGE), badge("ON_CREDIT", UI.ORANGE),
            badge("PACKED", UI.ORANGE),
            badge("IN_DEFAULT", UI.RED),    badge("CANCELLED", UI.RED),
            badge("OUT_OF_STOCK", UI.RED),
            badge("ACCEPTED", UI.ACCENT),   badge("CREDIT_CARD", UI.ACCENT),
            badge("DEBIT_CARD", UI.ACCENT), badge("FIXED", UI.ACCENT),
            badge("ADMIN", UI.ACCENT),
            badge("DISPATCHED", UI.PURPLE), badge("FLEXIBLE", UI.PURPLE),
            badge("MANAGER", UI.PURPLE)
        );
    }

    private static Map.Entry<String, Color[]> badge(String key, Color fg) {
        return Map.entry(key, new Color[]{
            new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 30), fg
        });
    }
}
