package algoscope;

import algoscope.ui.MainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts AlgoScope. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Nimbus looks the same on Windows, macOS and Linux; if it is missing, keep the default
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            System.err.println("Nimbus look and feel not available, using the default: " + e);
        }
        // Swing must be created and used on its own thread
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
