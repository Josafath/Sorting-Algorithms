package algoscope.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Colors and fonts used everywhere, so the whole app changes from one place.
 * <p>
 * Only Java's logical fonts (Dialog, SansSerif) are used: they exist on Windows,
 * macOS and Linux. v1 used Sitka Text and SimSun, which only exist on Windows.
 */
public final class Theme {

    public static final Color BACKGROUND = new Color(0xF8FAFC);
    public static final Color CANVAS = Color.WHITE;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x1E293B);
    public static final Color MUTED_TEXT = new Color(0x64748B);

    // Bars and the meaning of each highlight
    public static final Color BAR = new Color(0x94A3B8);
    public static final Color COMPARE = new Color(0xF59E0B);
    public static final Color SWAP = new Color(0xEF4444);
    public static final Color WRITE = new Color(0x3B82F6);
    public static final Color DONE = new Color(0x22C55E);

    private Theme() {
    }

    /** The default font of the look and feel, in another size and style. */
    public static Font font(int style, float size) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.DIALOG, Font.PLAIN, 13);
        }
        return base.deriveFont(style, size);
    }
}
