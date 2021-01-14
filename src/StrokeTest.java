import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

//import model.renderEngine.TextStroke;

public class StrokeTest extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final HashMap<Object, Object> hints;

    static {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        hints = new HashMap<>();
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        if (map != null) {
            hints.putAll(map);
        }
    }

    public StrokeTest() {
        setSize(200, 200);
    }

    @Override
    public void paint(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHints(hints);
        //g2.setStroke(new TextStroke("Test", new Font("Arial", Font.BOLD, 20)));
        Path2D path = new Path2D.Double();
        path.moveTo(20, 20);
        path.lineTo(100, 100);
        path.lineTo(200, 100);
        g2.draw(path);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setContentPane(new StrokeTest());
        frame.setVisible(true);
    }
}
