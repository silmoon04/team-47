package ie.cortexx;

import javax.swing.*;

// entry point - sets up flatlaf and launches the main window
// run with: mvn clean compile exec:java
public class Main {
    public static void main(String[] args) {
        // TODO: call FlatIntelliJLaf.setup() here before creating any swing stuff
        //   import com.formdev.flatlaf.FlatIntelliJLaf;
        //   FlatIntelliJLaf.setup();
        System.out.println("Test Commit");
        SwingUtilities.invokeLater(() -> {
            // TODO: create MainFrame and show it
            //   new MainFrame().setVisible(true);
        });
    }
}
