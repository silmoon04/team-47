package ie.cortexx.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class UINotifier {
    private UINotifier() {
    }

    static void notifyInfo(Component owner, String message) {
        show(owner, message, UI.ACCENT);
    }

    static void notifySuccess(Component owner, String message) {
        show(owner, message, UI.GREEN);
    }

    static void notifyError(Component owner, String message) {
        show(owner, message, UI.RED);
    }

    private static void show(Component owner, String message, Color color) {
        Runnable task = () -> {
            String safeMessage = message == null ? "" : message;
            Window window = owner != null ? SwingUtilities.getWindowAncestor(owner) : JOptionPane.getRootFrame();
            if (window == null) {
                JOptionPane.showMessageDialog(null, safeMessage);
                return;
            }

            JWindow toast = new JWindow(window);
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(UI.BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(12, 14, 12, 14)
            ));

            JLabel dot = new JLabel("●");
            dot.setFont(UI.FONT_MONO_BOLD);
            dot.setForeground(color);
            card.add(dot, BorderLayout.WEST);

            JLabel text = new JLabel(safeMessage);
            text.setFont(UI.FONT);
            text.setForeground(UI.TEXT);
            card.add(text, BorderLayout.CENTER);

            toast.add(card);
            toast.pack();

            Rectangle bounds = window.getBounds();
            int targetX = bounds.x + bounds.width - toast.getWidth() - 20;
            int targetY = bounds.y + bounds.height - toast.getHeight() - 24;
            toast.setLocation(targetX + 18, targetY);
            toast.setAlwaysOnTop(true);
            applyOpacity(toast, 0f);
            toast.setVisible(true);
            animateIn(toast, targetX, targetY);

            Timer timer = new Timer(2400, event -> {
                animateOut(toast, targetX + 14, targetY);
            });
            timer.setRepeats(false);
            timer.start();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private static void animateIn(JWindow toast, int targetX, int targetY) {
        Timer timer = new Timer(16, null);
        final int[] frame = {0};
        timer.addActionListener(event -> {
            frame[0]++;
            float progress = Math.min(1f, frame[0] / 12f);
            float eased = 1f - (float) Math.pow(1f - progress, 3);
            toast.setLocation(targetX + Math.round(18 * (1f - eased)), targetY);
            applyOpacity(toast, eased);
            if (progress >= 1f) {
                timer.stop();
            }
        });
        timer.start();
    }

    private static void animateOut(JWindow toast, int targetX, int targetY) {
        Timer timer = new Timer(16, null);
        final int[] frame = {0};
        int startX = toast.getX();
        timer.addActionListener(event -> {
            frame[0]++;
            float progress = Math.min(1f, frame[0] / 10f);
            float eased = progress * progress;
            toast.setLocation(startX + Math.round((targetX - startX) * eased), targetY);
            applyOpacity(toast, 1f - eased);
            if (progress >= 1f) {
                timer.stop();
                toast.setVisible(false);
                toast.dispose();
            }
        });
        timer.start();
    }

    private static void applyOpacity(Window window, float opacity) {
        try {
            window.setOpacity(Math.max(0f, Math.min(1f, opacity)));
        } catch (UnsupportedOperationException | IllegalComponentStateException ignored) {
            // Some platforms disable translucent windows; keep the toast functional.
        }
    }
}
