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
    private final JProgressBar loginProgress = new JProgressBar();
    private boolean loggingIn;

    public LoginPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JComponent buildTopBar() {
        JPanel bar = UI.paddedPanel(16, 16, 0, 16);
        bar.add(ThemeSwitchButton.create(mainFrame), BorderLayout.EAST);
        return bar;
    }

    private JComponent buildContent() {
        JPanel outer = UI.centeredPanel();
        JPanel card = UI.vcard(48, 48, 420, 350);
        card.add(UI.title("IPOS-CA"));
        card.add(UI.gap(4));
        card.add(UI.subtitle("Cosymed Ltd - Pharmacy Management System"));
        card.add(UI.gap(12));
        card.add(usernameField);
        card.add(UI.gap(12));
        card.add(passwordField);
        card.add(UI.gap(12));

        configureLoginProgress();
        card.add(signInButton);
        card.add(UI.gap(8));
        card.add(loginProgress);
        card.add(UI.gap(12));
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

    private void configureLoginProgress() {
        loginProgress.setIndeterminate(true);
        loginProgress.setString("Signing in...");
        loginProgress.setStringPainted(true);
        loginProgress.setVisible(false);
        loginProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        loginProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void setLoggingIn(boolean active) {
        loggingIn = active;
        usernameField.setEnabled(!active);
        passwordField.setEnabled(!active);
        signInButton.setEnabled(!active);
        signInButton.setText(active ? "SIGNING IN..." : "SIGN IN");
        loginProgress.setVisible(active);
        errorLabel.setText(active ? " " : errorLabel.getText());
    }
}
