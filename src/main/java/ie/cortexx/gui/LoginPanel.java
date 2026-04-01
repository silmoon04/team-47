package ie.cortexx.gui;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

/*
why this panel is built like this

UI.centeredPanel() centers the card in the window (GridBagLayout).
UI.vcard() creates a vertical BoxLayout card with custom padding + size.
UI.inputField() / UI.passwordField() handle placeholder + sizing in one call.
UI.primaryButtonWide() makes a full width accent button.
UI.title() / UI.subtitle() / UI.errorLabel() replace 3-4 lines each.

mock role assignment for now, swap with AuthService later.
*/

// centered login card: username, password, sign in
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField usernameField = UI.inputField("Username");
    private final JPasswordField passwordField = UI.passwordField("Password");
    private final JLabel errorLabel = UI.errorLabel();

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
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

        // TODO: swap with AuthService.authenticate(user, password)
        String role = user.equalsIgnoreCase("sysdba")
            ? "admin"
            : user.equalsIgnoreCase("manager") ? "manager" : "pharmacist";

        mainFrame.login(user, role);
    }
}
