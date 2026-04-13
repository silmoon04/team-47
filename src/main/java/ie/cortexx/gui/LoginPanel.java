package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;
import ie.cortexx.model.User;
import ie.cortexx.service.AuthService;
import ie.cortexx.util.SessionManager;

import javax.swing.*;
import java.awt.*;

// centered login card: username, password, sign in
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuthService authService;
    private final JTextField usernameField = UI.inputField("Username");
    private final JPasswordField passwordField = UI.passwordField("Password");
    private final JLabel errorLabel = UI.errorLabel();
    private final JButton signInButton = UI.primaryButtonWide("SIGN IN");
    private Timer dotTimer;
    private boolean loggingIn;

    public LoginPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private JComponent buildTopBar() {
        JPanel bar = UI.paddedPanel(16, 16, 0, 16);
        bar.add(AppearanceDialog.createButton(mainFrame), BorderLayout.EAST);
        return bar;
    }

    private JComponent buildContent() {
        JPanel outer = UI.centeredPanel();
        JPanel card = UI.vcard(48, 40, 420, 310);
        card.add(UI.title("IPOS-CA"));
        card.add(UI.gap(4));
        card.add(UI.subtitle("Cosymed Ltd - Pharmacy Management System"));
        card.add(UI.gap(12));
        card.add(usernameField);
        card.add(UI.gap(12));
        card.add(passwordField);
        card.add(UI.gap(12));

        card.add(signInButton);
        card.add(UI.gap(6));
        card.add(errorLabel);

        signInButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        outer.add(card);
        return outer;
    }

    private void attemptLogin() {
        if (loggingIn) {
            return;
        }

        String user = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (user.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        setLoggingIn(true);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                if (!authService.authenticate(user, password)) {
                    return null;
                }
                return SessionManager.getInstance().getCurrentUser();
            }

            @Override
            protected void done() {
                try {
                    User currentUser = get();
                    if (currentUser == null) {
                        errorLabel.setText("Invalid username or password.");
                        setLoggingIn(false);
                        return;
                    }
                    mainFrame.login(currentUser.getUsername(), currentUser.getRole().name().toLowerCase());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    errorLabel.setText("Login interrupted.");
                    setLoggingIn(false);
                } catch (Exception error) {
                    errorLabel.setText("Unable to load session.");
                    setLoggingIn(false);
                }
            }
        }.execute();
    }

    private void setLoggingIn(boolean active) {
        loggingIn = active;
        usernameField.setEnabled(!active);
        passwordField.setEnabled(!active);
        signInButton.setEnabled(!active);
        if (active) {
            dotTimer = new Timer(400, null);
            final int[] tick = {0};
            dotTimer.addActionListener(e -> {
                tick[0] = (tick[0] + 1) % 4;
                String dots = ".".repeat(tick[0]);
                String pad = " ".repeat(3 - tick[0]);
                signInButton.setText(pad + "SIGNING IN" + dots + pad);
            });
            signInButton.setText("   SIGNING IN   ");
            dotTimer.start();
        } else {
            if (dotTimer != null) { dotTimer.stop(); dotTimer = null; }
            signInButton.setText("SIGN IN");
        }
        errorLabel.setText(active ? " " : errorLabel.getText());
    }
}
