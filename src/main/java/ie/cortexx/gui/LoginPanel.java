package ie.cortexx.gui;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import ie.cortexx.service.AuthService;

// login form: username, password, login button
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField usernameField = new JTextField(25);
    private JPasswordField passwordField = new JPasswordField(25);
    private JButton loginButton = new JButton("Login");
    private AuthService authService = new AuthService();

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        add(new JLabel("Username"));
        add(usernameField);
        add(new JLabel("Password"));
        add(passwordField);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                // mock login service
                if (username.equalsIgnoreCase("sysdba")) {
                    mainFrame.setRole("admin");
                } else if (username.equalsIgnoreCase("manager")) {
                    mainFrame.setRole("manager");
                } else {
                    mainFrame.setRole("pharmacist");
                }
                mainFrame.setUsername(username);
                // AuthService.authenticate(username, password) // to be implemented
                loginSuccess();
            }
        });
    }

    public void loginSuccess() {
        JOptionPane.showMessageDialog(this, "Login Successful.");
        mainFrame.setUsername(usernameField.getText());
        mainFrame.showMainFrame(mainFrame.getRole());
    }

    public void loginFail() {
        JOptionPane.showMessageDialog(this, "Login Failed.");
    }
}
