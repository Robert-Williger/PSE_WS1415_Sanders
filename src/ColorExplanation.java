import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JFrame;

import model.elements.Area;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.IWay;
import model.elements.Way;
import model.map.PixelConverter;
import model.map.Tile;
import model.renderEngine.IRenderer;
import model.renderEngine.StorageBackgroundRenderer;

public class ColorExplanation extends JFrame {
    private static final long serialVersionUID = 1L;

    private BufferedImage image;

    public ColorExplanation() {
        final int areas = 24;
        final int ways = 24;

        image = new BufferedImage(1280, 1024, BufferedImage.TYPE_INT_ARGB);

        final IRenderer renderer = new StorageBackgroundRenderer(new PixelConverter(1 << 19));

        String[] areaNames = new String[]{"Wald", "Wald", "Gras", "Weide", "Wohngebiet", "Wasser", "Industriegebiet",
                "Park", "Handel", "Heide", "Sand", "Schlamm/Geröll", "Steinbruch", "Friedhof", "Parkplatz",
                "Fußgängerzone", "Feld", "Spielplatz", "Spielfeld", "Sportgebiet", "Laufbahn", "Golfplatz", "Bildung",
                "Zoo"};
        final AssociatedImage[] areaImages = new AssociatedImage[areas];

        for (int i = 0; i < areas; i++) {
            BufferedImage typeImage = new BufferedImage(35, 20, BufferedImage.TYPE_INT_ARGB);
            final IArea iArea = new Area(new int[]{1, 1, 1, 19, 34, 19, 34, 1}, i);

            final Tile tile = new Tile(19, 0, 0, new IWay[0], new IStreet[0], new IArea[]{iArea}, new IBuilding[0],
                    new POI[0], new Label[0]);
            renderer.render(tile, typeImage);
            areaImages[i] = new AssociatedImage(typeImage, areaNames[i]);
        }

        Arrays.sort(areaImages);

        String[] wayNames = new String[]{"Gemeindestraße", "Privatweg", "Landstraße", "Kreisstraße",
                "Unklassifizierte Straße", "Waldweg", "Fußweg", "Radweg", "Reitweg", "Wanderweg", "Fluss",
                "Schmaler Fluss", "Bahnschiene", "Straßenbahn", "Bundesstraße", "Autobahn", "Schnellstraße",
                "Bundesstraßen-Ausfahrt", "Autobahn-Ausfahrt", "Schnellstraßen-Ausfahrt", "Laufbahn", "Treppenstufen",
                "Mauer", "Hecke"};
        final AssociatedImage[] wayImages = new AssociatedImage[areas];
        for (int i = 0; i < ways; i++) {
            BufferedImage typeImage = new BufferedImage(35, 20, BufferedImage.TYPE_INT_ARGB);
            final IWay iWay = new Way(new int[]{-15, 10, 35, 10}, i, "");

            final Tile tile = new Tile(19, 0, 0, new IWay[]{iWay}, new IStreet[0], new IArea[0], new IBuilding[0],
                    new POI[0], new Label[0]);
            renderer.render(tile, typeImage);
            wayImages[i] = new AssociatedImage(typeImage, wayNames[i]);
        }

        // Arrays.sort(wayImages);

        final Graphics2D g = image.createGraphics();
        g.setColor(Color.black);

        for (int i = 0; i < areas; i++) {
            g.drawImage(areaImages[i].image, 15, 30 * i + 15, null);
            g.drawString(areaImages[i].name, 55, 30 * i + 30);

            g.drawImage(wayImages[i].image, 200, 30 * i + 15, null);
            g.drawString(wayImages[i].name, 240, 30 * i + 30);
        }
        g.dispose();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400);
        setVisible(true);
    }

    @Override
    public void paint(final Graphics g) {
        g.drawImage(image, 0, 20, this);
    }

    public static void main(String[] args) {
        new ColorExplanation();
    }

    private class AssociatedImage implements Comparable<AssociatedImage> {

        private final float[] hsb;
        private final BufferedImage image;
        private final String name;

        public AssociatedImage(final BufferedImage image, final String name) {
            hsb = getHSB(image.getRGB(15, 15));
            this.image = image;
            this.name = name;
        }

        @Override
        public int compareTo(final AssociatedImage o) {
            return (int) (360 * (hsb[0] - o.hsb[0]));
        }

        private float[] getHSB(final int rgb) {
            final Color color = new Color(rgb, true);
            float[] hsb = new float[3];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

            return hsb;
        }
    }
}
