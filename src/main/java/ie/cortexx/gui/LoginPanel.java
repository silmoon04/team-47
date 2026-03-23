package ie.cortexx.gui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// login form: username, password, login button
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField = new JTextField(25);
    private JPasswordField passwordField = new JPasswordField(25);
    private JButton loginButton = new JButton("Login");

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        add(new JLabel("Username"));
        add(usernameField);
        add(new JLabel("Password"));
        add(passwordField);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginSuccess();
            }
        });
    }

    public void loginSuccess() {
        JOptionPane.showMessageDialog(this, "Login Successful.");
//        mainFrame.showMainFrame("admin");
//        mainFrame.showMainFrame("pharmacist");
        mainFrame.showMainFrame("manager");
    }

    public void loginFail() {
        JOptionPane.showMessageDialog(this, "Login Unsuccessful.");
    }
}
