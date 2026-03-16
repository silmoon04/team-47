package ie.cortexx.gui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// login form: username, password, login button
public class LoginPanel extends JPanel {
    // TODO: add JTextField for username *
    // TODO: add JPasswordField for password *
    // TODO: add JButton that calls AuthService.authenticate() *
    // TODO: show error label if login fails *
    // TODO: on success, call the callback to switch to main view *

    private JTextField usernameField = new JTextField(25);
    private JPasswordField passwordField = new JPasswordField(25);
    private JButton loginButton = new JButton("Login");
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        add(new JLabel("Username"));
        add(usernameField);
        add(new JLabel("Password"));
        add(passwordField);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // AuthService.authenticate();
            }
        });
    }

    public void loginSuccess() {
        JOptionPane.showMessageDialog(this, "Login Successful");
        mainFrame.showMainFrame();
    }

    public void loginFail() {
        JOptionPane.showMessageDialog(this, "Login Unsuccessful");
    }
}
