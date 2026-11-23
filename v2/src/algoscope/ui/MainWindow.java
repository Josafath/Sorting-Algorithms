package algoscope.ui;

import algoscope.ui.pathfinding.PathfindingPanel;
import algoscope.ui.sorting.SortingPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** The window: one tab per module. */
public final class MainWindow extends JFrame {

    public MainWindow() {
        super("AlgoScope");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        SortingPanel sorting = new SortingPanel();
        PathfindingPanel pathfinding = new PathfindingPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sorting", sorting);
        tabs.addTab("Pathfinding", pathfinding);
        // An animation keeps running on a hidden tab otherwise: pause both on every switch
        tabs.addChangeListener(e -> {
            sorting.pause();
            pathfinding.pause();
        });

        // Nimbus leaves the strip next to the tabs transparent: give it the app background
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);

        setMinimumSize(new Dimension(1000, 640));
        setSize(1200, 780);
        // Centered on the screen
        setLocationRelativeTo(null);
    }
}
