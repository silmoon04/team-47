package ie.cortexx.gui.util;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Shared style primitives used by the higher-level UI helpers.
 */
final class UIStyleSupport {
    private UIStyleSupport() {
    }

    static Border cardBorder(int top, int left, int bottom, int right, int arc) {
        return BorderFactory.createCompoundBorder(
            roundedBorder(arc),
            new EmptyBorder(top, left, bottom, right)
        );
    }

    static String inputStyle() {
        return "arc:" + UI.FIELD_ARC +
            "; borderWidth:1; focusWidth:" + (UI.isDarkTheme() ? 0 : 1) +
            "; innerFocusWidth:0; borderColor:" + UIThemeSupport.hex(UI.BORDER) +
            "; focusedBorderColor:" + UIThemeSupport.hex(UIThemeSupport.outlineColor()) +
            "; background:" + UIThemeSupport.hex(UI.BG_INPUT);
    }

    static void sizeField(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
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
}
