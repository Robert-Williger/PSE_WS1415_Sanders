import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JFrame;

public class TextShadowTest extends JFrame {
    private static final long serialVersionUID = 1L;
    private final BufferedImage image;

    public TextShadowTest() {

        image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        final Font font = new Font("Times New Roman", Font.PLAIN, 12);

        final Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, 400, 400);
        g2.setColor(Color.black);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Map<?, ?> map = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g2.addRenderingHints(map);
        }

        g2.setFont(font);

        g2.setColor(new Color(151, 208, 208));
        g2.fillRect(0, 0, 400, 400);
        final double theta = Math.PI / 4;
        final String text = "Oder";
        final AffineTransform at = AffineTransform.getTranslateInstance(150, 120);
        at.rotate(theta);

        g2.setColor(Color.white);
        at.translate(0, -Math.cos(theta));
        g2.setFont(font.deriveFont(at));
        g2.drawString(text, 0, 0);

        at.translate(-Math.sin(theta), 0);
        g2.setFont(font.deriveFont(at));
        g2.drawString(text, 0, 0);

        at.translate(0, 2 * Math.cos(theta));
        g2.setFont(font.deriveFont(at));
        g2.drawString(text, 0, 0);

        at.translate(2 * Math.sin(theta), 0);
        g2.setFont(font.deriveFont(at));
        g2.drawString(text, 0, 0);

        g2.setColor(new Color(102, 153, 204));
        at.translate(-Math.sin(theta), -Math.cos(theta));
        g2.setFont(font.deriveFont(at));
        g2.drawString(text, 0, 0);

        g2.dispose();

        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    public static void main(final String[] args) {
        new TextShadowTest();
    }
}
