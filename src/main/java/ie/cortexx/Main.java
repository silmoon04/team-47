package ie.cortexx;

import javax.swing.*;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.gui.*;
import ie.cortexx.gui.util.UI;
import com.formdev.flatlaf.FlatDarkLaf;
import ie.cortexx.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// entry point - sets up flatlaf dark theme and launches the main window
// run with: mvn clean compile exec:java

public class Main {
    public static void main(String[] args) throws SQLException {
        FlatDarkLaf.setup();   // dark theme
        UI.init();             // apply all our custom colours, fonts, component styles
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
