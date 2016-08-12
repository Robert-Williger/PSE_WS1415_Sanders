import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JFrame;

import util.Arrays;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.IStreet;
import model.elements.IWay;
import model.elements.Label;
import model.elements.POI;
import model.elements.Street;
import model.map.AbstractTile;
import model.map.ITile;
import model.map.PixelConverter;
import model.renderEngine.ColorScheme;
import model.renderEngine.BackgroundRenderer;
import model.renderEngine.IRenderer;
import model.renderEngine.ShapeStyle;
import model.renderEngine.WayStyle;

public class ColorSchemeCreator extends JFrame {
    private static final long serialVersionUID = 1L;

    private final IRenderer renderer;
    private final ITile tile;
    private final BufferedImage image;
    private final VariableWayStyle wayStyle;
    private final ShapeStyle areaStyle;
    private final ShapeStyle buildingStyles;

    public ColorSchemeCreator() {
        wayStyle = new VariableWayStyle();
        areaStyle = new VariableAreaStyle();
        buildingStyles = new VariableAreaStyle();
        tile = new MyTile();
        image = new BufferedImage(100, 40, BufferedImage.TYPE_INT_ARGB);
        final ColorScheme scheme = new ColorScheme(new WayStyle[]{wayStyle}, new ShapeStyle[]{areaStyle},
                new ShapeStyle[]{buildingStyles}, new int[][]{{0}}, new int[]{0});
        renderer = new BackgroundRenderer(new PixelConverter(1), scheme);
        renderer.render(tile, image);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ColorSchemeCreator();
    }

    private class VariableWayStyle extends WayStyle {

        public VariableWayStyle() {
            super(10, Color.BLUE);
        }

    }

    private class VariableAreaStyle extends ShapeStyle {

        public VariableAreaStyle() {
            super(0, 0, null);
        }

    }

    private class MyTile extends AbstractTile {

        private final IStreet[] street;

        public MyTile() {
            super(0, 0, 0);
            street = new Street[]{new Street(new int[]{20, 20, 80, 20}, 0, "", 0)};
        }

        @Override
        public Iterator<IStreet> getStreets() {
            return Arrays.iterator(street);
        }

        @Override
        public Iterator<IWay> getWays() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IBuilding> getBuildings() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<IArea> getTerrain() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<POI> getPOIs() {
            return Arrays.iterator();
        }

        @Override
        public Iterator<Label> getLabels() {
            return Arrays.iterator();
        }

    }

    public void paint(final Graphics g) {
        super.paint(g);
        g.drawImage(image, 20, 40, this);
    }

    private static int[] createIdentityArray(final int length) {
        final int[] ret = new int[length];

        for (int i = 0; i < length; i++) {
            ret[i] = i;
        }

        return ret;
    }
}
