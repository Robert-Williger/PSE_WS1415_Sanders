package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Map;

import javax.swing.JTextField;

public class DefaultTextedTextField extends JTextField {
    private static final long serialVersionUID = 1L;

    private boolean defaultTexted;
    private String defaultText;

    public DefaultTextedTextField(final String text) {
        super(text);

        setBackground(Color.white);
        defaultText = "default text";
        defaultTexted = true;
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                defaultTexted = false;
                repaint();
            }
        });
    }

    public DefaultTextedTextField() {
        this("");
    }

    public void setDefaultText(final String defaultText) {
        this.defaultText = defaultText;
    }

    @Override
    public void setText(final String text) {
        defaultTexted = false;
        super.setText(text);
    }

    public void clear() {
        super.setText("");
        defaultTexted = true;
        repaint();
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (defaultTexted) {
            final Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
            if (map != null) {
                g2.addRenderingHints(map);
            }
            g2.setColor(Color.white);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(isEnabled() ? getForeground() : getDisabledTextColor());
            g2.setFont(new Font("Tahoma", Font.ITALIC, 11));
            g2.drawString(defaultText, 2, 14);
        }
    }
}
