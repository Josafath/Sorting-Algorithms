package algoscope.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/** Small building blocks shared by the tabs, so they all look the same. */
public final class Widgets {

    private Widgets() {
    }

    /** A transparent row that lays its children out from left to right. */
    public static JPanel row() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panel.setOpaque(false);
        return panel;
    }

    /** A grey label for the name of a control, like "Algorithm". */
    public static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    /** A colored square followed by its meaning, for the legends under the drawings. */
    public static JLabel legendItem(Color color, String text) {
        JLabel label = caption(text);
        label.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, 10, 10);
                g.setColor(Theme.BORDER);
                g.drawRect(x, y, 9, 9);
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        });
        return label;
    }

    /** Shows the items of a combo box as text, without needing a toString(). */
    public static <T> ListCellRenderer<Object> textRenderer(Function<T, String> text) {
        return new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value != null) {
                    setText(text.apply((T) value));
                }
                return this;
            }
        };
    }
}
