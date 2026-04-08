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

        JButton btn = UI.primaryButtonWide("SIGN IN");
        card.add(btn);
        card.add(UI.gap(12));
        card.add(errorLabel);

        btn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        outer.add(card);
        return outer;
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        if (user.isEmpty() || passwordField.getPassword().length == 0) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        if (!authService.authenticate(user, new String(passwordField.getPassword()))) {
            errorLabel.setText("Invalid username or password.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorLabel.setText("Unable to load session.");
            return;
        }

        mainFrame.login(currentUser.getUsername(), currentUser.getRole().name().toLowerCase());
    }
}
