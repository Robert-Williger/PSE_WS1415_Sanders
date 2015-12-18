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
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import adminTool.elements.Node;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;
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

        IArea origArea = null;
        Node[] origNodes = null;
        List<Node> nodes = new ArrayList<Node>();

        int row = 0;
        int column = 0;
        int zoom = 10;

        PixelConverter converter = new PixelConverter(1 << 21);
        try {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                    "MaxArea.txt"))));

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
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

            row = minY / converter.getCoordDistance(256, zoom);
            column = minX / converter.getCoordDistance(256, zoom);

            origNumber = nodes.size();
            origNodes = nodes.toArray(new Node[origNumber]);
            int[] conversion = convert(origNodes);
            origArea = new Area(conversion, 3);

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BackgroundRenderer renderer = new BackgroundRenderer(new PixelConverter(1 << 21));

        Tile origTile = new Tile(zoom, row, column, new IWay[]{}, new IStreet[]{}, new IArea[]{origArea},
                new IBuilding[]{}, new POI[]{}, new Label[0]);
        renderer.render(origTile, orig);

        VisvalingamWhyatt simplificator = new VisvalingamWhyatt(converter, simpleThreshHold);

        final int[] simplifiedIndices = simplificator.simplifyMultiline(nodes.iterator(), zoom);
        final Node[] simplifiedNodes = new Node[simplifiedIndices.length];
        for (int i = 0; i < simplifiedNodes.length; i++) {
            simplifiedNodes[i] = origNodes[simplifiedIndices[i]];
        }
        int[] conversion = convert(simplifiedNodes);
        Tile simplifiedTile = new Tile(zoom, row, column, new IWay[]{}, new IStreet[]{}, new IArea[]{new Area(
                conversion, origArea.getType())}, new IBuilding[]{}, new POI[]{}, new Label[0]);
        renderer.render(simplifiedTile, simple);

        simplificator = new VisvalingamWhyatt(converter, doubleSimpleThreshHold);
        final int[] doubleSimplifiedIndices = simplificator.simplifyMultiline(nodes.iterator(), zoom);
        final Node[] doubleSimplifiedNodes = new Node[doubleSimplifiedIndices.length];
        for (int i = 0; i < doubleSimplifiedNodes.length; i++) {
            doubleSimplifiedNodes[i] = origNodes[doubleSimplifiedIndices[i]];
        }
        conversion = convert(doubleSimplifiedNodes);
        Tile doubleSimplifiedTile = new Tile(zoom, row, column, new IWay[]{}, new IStreet[]{}, new IArea[]{new Area(
                conversion, origArea.getType())}, new IBuilding[]{}, new POI[]{}, new Label[0]);
        renderer.render(doubleSimplifiedTile, doubleSimple);

        simpleNumber = simplifiedNodes.length;
        doubleSimpleNumber = doubleSimplifiedNodes.length;

        setVisible(true);

    }

    private int[] convert(final Node[] node) {
        int[] ret = new int[2 * node.length];
        for (int i = 0; i < node.length; i++) {
            ret[2 * i] = node[i].getX();
            ret[2 * i + 1] = node[i].getY();
        }
        return ret;
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
