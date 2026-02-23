package parking;

import parking.ui.MainFrame;
import javax.swing.*;

/**
 * Point d'entrée de l'application Gestion de Parking.
 */
public class App {
    public static void main(String[] args) {
        // Look & Feel système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
