package ie.cortexx;

import javax.swing.*;

import ie.cortexx.gui.*;
import ie.cortexx.gui.util.UI;

// entry point - sets up flatlaf dark theme and launches the main window
// run with: mvn clean compile exec:java

public class Main {
    public static void main(String[] args) {
        UI.init();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
