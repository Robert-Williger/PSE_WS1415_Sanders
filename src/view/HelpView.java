package view;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class HelpView extends JFrame {
    private static final long serialVersionUID = 1L;

    private BufferedImage image;

    public HelpView() {
        super("Traveling Salesman Routenplaner - Hilfe");
        try {
            image = ImageIO.read(getClass().getResource("guide.png"));
        } catch (final IOException e) {
            image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        try {
            final URL path = getClass().getResource("help.png");
            if (path != null) {
                setIconImage(ImageIO.read(path));
            }
        } catch (final IOException e) {

        }

        setSize(933, 906);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    @Override
    public void paint(final Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}
