package ie.cortexx;

import javax.swing.*;

import ie.cortexx.gui.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

// entry point - sets up flatlaf and launches the main window
// run with: mvn clean compile exec:java

public class Main {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true)); // displays main frame
    }
}
