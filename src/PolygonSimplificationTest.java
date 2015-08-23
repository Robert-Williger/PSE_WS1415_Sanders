import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import adminTool.VisvalingamWhyatt;
import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.Way;
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.BackgroundRenderer;

public class PolygonSimplificationTest extends JFrame {
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        new PolygonSimplificationTest();
    }

    private BufferedImage orig;
    private BufferedImage simple;
    private BufferedImage doubleSimple;

    private int simpleThreshHold = 1;
    private int doubleSimpleThreshHold = 2;

    private int origNumber;
    private int simpleNumber;
    private int doubleSimpleNumber;

    public PolygonSimplificationTest() {
        orig = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        simple = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        doubleSimple = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        setSize(960, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Area origArea = null;
        Node[] origNodes = null;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        PixelConverter converter = new PixelConverter(1 << 21);
        try {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                    "MaxArea.txt"))));

            List<Node> nodes = new ArrayList<Node>();

            while (stream.available() > 0) {
                int x = stream.readInt();
                int y = stream.readInt();

                if (x < minX) {
                    minX = x;
                }
                if (y < minY) {
                    minY = y;
                }

                nodes.add(new Node(x, y));
            }
            // nodes.add(new Node(540228, 2724552));
            // nodes.add(new Node(509918, 2724552));
            // nodes.add(new Node(509918, 2672809));
            // nodes.add(new Node(540228, 2672809));
            // minX = 500000;
            // minY = 2600000;

            origNumber = nodes.size();
            origNodes = nodes.toArray(new Node[origNumber]);
            origArea = new Area(origNodes, 3);

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BackgroundRenderer renderer = new BackgroundRenderer(new PixelConverter(1 << 21));
        int zoom = 10;
        Tile origTile = new Tile(zoom, 0, 0, minX, minY, new Way[]{}, new Street[]{}, new Area[]{origArea},
                new Building[]{}, new POI[]{});
        renderer.render(origTile, orig);

        VisvalingamWhyatt simplificator = new VisvalingamWhyatt(converter, simpleThreshHold);

        final int[] simplifiedIndices = simplificator.simplifyMultiline(origArea.iterator(), zoom);
        final Node[] simplifiedNodes = new Node[simplifiedIndices.length];
        for (int i = 0; i < simplifiedNodes.length; i++) {
            simplifiedNodes[i] = origNodes[simplifiedIndices[i]];
        }
        Tile simplifiedTile = new Tile(zoom, 0, 0, minX, minY, new Way[]{}, new Street[]{}, new Area[]{new Area(
                simplifiedNodes, origArea.getType())}, new Building[]{}, new POI[]{});
        renderer.render(simplifiedTile, simple);

        simplificator = new VisvalingamWhyatt(converter, doubleSimpleThreshHold);
        final int[] doubleSimplifiedIndices = simplificator.simplifyMultiline(origArea.iterator(), zoom);
        final Node[] doubleSimplifiedNodes = new Node[doubleSimplifiedIndices.length];
        for (int i = 0; i < doubleSimplifiedNodes.length; i++) {
            doubleSimplifiedNodes[i] = origNodes[doubleSimplifiedIndices[i]];
        }
        Tile doubleSimplifiedTile = new Tile(zoom, 0, 0, minX, minY, new Way[]{}, new Street[]{}, new Area[]{new Area(
                doubleSimplifiedNodes, origArea.getType())}, new Building[]{}, new POI[]{});
        renderer.render(doubleSimplifiedTile, doubleSimple);

        simpleNumber = simplifiedNodes.length;
        doubleSimpleNumber = doubleSimplifiedNodes.length;

        setVisible(true);

    }

    public void paint(final Graphics g) {
        g.drawString("Original (" + origNumber + " nodes)", 140, 50);
        g.drawImage(orig, 40, 60, this);

        g.drawString("Vergöbert[" + simpleThreshHold + "] (" + simpleNumber + " nodes)", 450, 50);
        g.drawImage(simple, 350, 60, this);

        g.drawString("Vergöbert[" + doubleSimpleThreshHold + "] (" + doubleSimpleNumber + " nodes)", 760, 50);
        g.drawImage(doubleSimple, 660, 60, this);
    }
}
