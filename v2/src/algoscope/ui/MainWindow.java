package algoscope.ui;

import algoscope.ui.sorting.SortingPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** The window: one tab per module. Pathfinding arrives in phase 3. */
public final class MainWindow extends JFrame {

    public MainWindow() {
        super("AlgoScope");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sorting", new SortingPanel());
        // Nimbus leaves the strip next to the tabs transparent: give it the app background
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);

        setMinimumSize(new Dimension(900, 600));
        setSize(1150, 720);
        // Centered on the screen
        setLocationRelativeTo(null);
    }
}
