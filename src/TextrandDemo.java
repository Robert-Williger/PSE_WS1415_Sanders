/*
 * TextrandDemo.java
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TextrandDemo extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TextrandDemo() {
        super("Textrand Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        add(new MyPanel());
    }

    public static void main(final String args[]) {
        new TextrandDemo().setVisible(true);
    }
}


class MyPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    final private Shape shape;

    public MyPanel() {
        final TextLayout text = new TextLayout("Hallo", Font.decode("Arial-BOLD-100"), new FontRenderContext(null,
                false, false));
        final AffineTransform textAt = new AffineTransform();
        textAt.translate(0, (float) text.getBounds().getHeight());
        shape = text.getOutline(textAt);
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Stroke setzen:
        g2.setStroke(new BasicStroke(5.0f));
        // Shape positionieren:
        final AffineTransform pos = new AffineTransform();
        pos.translate(50, 100);
        g2.transform(pos);
        // Füllen und umranden:
        g2.setColor(Color.blue);
        g2.fill(shape);
        g2.setColor(Color.darkGray);
        g2.draw(shape);
    }
}