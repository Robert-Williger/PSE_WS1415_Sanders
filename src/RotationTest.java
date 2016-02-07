import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.JFrame;

public class RotationTest extends JFrame {
    private static final long serialVersionUID = 1L;

    public RotationTest() {
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void paint(final Graphics g) {

        final Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g2.addRenderingHints(map);
        }

        g2.drawRect(90, 80, 50, 30);
        g2.drawString("Hallo", 100, 100);

        g2.rotate(Math.PI / 4);

        g2.drawRect(90, 80, 50, 30);
        g2.drawString("Hallo", 100, 100);
        // g2.fillOval(190, 190, 20, 20);
        //
        // g2.rotate(Math.PI / 4);
        // g2.fillOval(190, 190, 20, 20);
    }

    public static void main(String[] args) {
        new RotationTest();
    }
}
