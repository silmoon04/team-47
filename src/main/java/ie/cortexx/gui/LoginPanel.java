package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

/*
=== why this panel is built like this ===

UI.centeredPanel() centers the card in the window (GridBagLayout).
UI.vcard() creates a vertical BoxLayout card with custom padding + size.
UI.inputField() / UI.passwordField() handle placeholder + sizing in one call.
UI.primaryButtonWide() makes a full width accent button.
UI.title() / UI.subtitle() / UI.errorLabel() replace 3-4 lines each.

mock role assignment for now, swap with AuthService later.
*/

// centered login card: username, password, sign in
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField = UI.inputField("Username");
    private JPasswordField passwordField = UI.passwordField("Password");
    private JLabel errorLabel = UI.errorLabel();

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UI.BG);
        setLayout(new GridBagLayout());

        // card: vertical stack, 48px padding, 420x350
        JPanel card = UI.vcard(48, 48, 420, 350);

        card.add(UI.title("IPOS-CA"));
        card.add(Box.createVerticalStrut(4));
        card.add(UI.subtitle("Cosymed Ltd - Pharmacy Management System"));
        card.add(Box.createVerticalStrut(12));

        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));

        JButton btn = UI.primaryButtonWide("SIGN IN");
        card.add(btn);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);

        add(card);

        btn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        if (user.isEmpty() || passwordField.getPassword().length == 0) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        // TODO: swap with AuthService.authenticate(user, password)
        if (user.equalsIgnoreCase("sysdba")) mainFrame.setRole("admin");
        else if (user.equalsIgnoreCase("manager")) mainFrame.setRole("manager");
        else mainFrame.setRole("pharmacist");

        mainFrame.setUsername(user);
        mainFrame.showMainFrame(mainFrame.getRole());
    }
}
