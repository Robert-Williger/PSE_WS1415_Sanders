import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import model.IFactory;
import model.map.CollectiveAccessorFactory;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;
import model.map.IQuadtree;
import model.map.MapManager;
import model.map.MapState;
import model.map.PixelConverter;
import model.map.Quadtree;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.IPointAccessor;
import model.map.accessors.ITileAccessor;
import model.map.accessors.TileAccessor;
import model.renderEngine.BackgroundRenderer;
import model.renderEngine.OSMColorScheme;

public class ImageTest {

    private static int maxColors;

    public IMapManager read() {
        IMapManager manager = null;

        final String path = "quadtree";
        try {
            manager = readMapManager(new File(path).getAbsolutePath());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return manager;
    }

    private DataInputStream createInputStream(final String path) throws IOException {
        return createInputStream(new File(path));
    }

    private DataInputStream createInputStream(final File file) throws IOException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    }

    public IMapManager readMapManager(final String path) throws IOException {
        final DataInputStream reader = createInputStream(path + "/header");
        final IMapState state = readMapState(reader);
        final IPixelConverter converter = readConverter(reader);
        final int tileSize = reader.readInt();
        final int[][] distributions = readDistributions(reader);
        reader.close();

        final String[] strings = readStrings(path);
        final int[][] nodes = readNodes(path);
        final Map<String, IQuadtree> quadtreeMap = new HashMap<>();
        final Map<String, IFactory<ICollectiveAccessor>> collectiveMap = new HashMap<>();
        readElements(path, nodes, distributions, quadtreeMap, collectiveMap, state.getMinZoom());
        final IFactory<ITileAccessor> tileFactory = new IFactory<ITileAccessor>() {
            @Override
            public ITileAccessor create() {
                return new TileAccessor(quadtreeMap, converter, tileSize);
            }
        };
        return new MapManager(new HashMap<String, IFactory<IPointAccessor>>(), collectiveMap, tileFactory, strings,
                converter, state, tileSize);
    }

    private IMapState readMapState(final DataInputStream reader) throws IOException {
        final int width = reader.readInt();
        final int height = reader.readInt();
        final int minZoomStep = reader.readInt();
        final int maxZoomStep = reader.readInt();
        return new MapState(width, height, minZoomStep, maxZoomStep);
    }

    private IPixelConverter readConverter(final DataInputStream reader) throws IOException {
        final double conversionFactor = reader.readDouble();
        return new PixelConverter(conversionFactor);
    }

    private int[][] readDistributions(final DataInputStream reader) throws IOException {
        final int[][] distributions = new int[4][];
        for (int i = 0; i < 4; i++) {
            distributions[i] = readIntArray(reader.readInt(), reader);
        }

        return distributions;
    }

    private int[][] readNodes(final String path) throws IOException {
        DataInputStream reader = createInputStream(path + "/nodes");
        int count = 0;

        final int[] xPoints = new int[reader.readInt()];
        final int[] yPoints = new int[xPoints.length];
        for (count = 0; count < xPoints.length; count++) {
            xPoints[count] = reader.readInt();
            yPoints[count] = reader.readInt();
        }
        reader.close();

        return new int[][]{xPoints, yPoints};
    }

    private String[] readStrings(final String path) throws IOException {
        DataInputStream reader = createInputStream(path + "/strings");
        final String[] strings = new String[reader.readInt()];
        for (int count = 0; count < strings.length; count++) {
            strings[count] = reader.readUTF();
        }
        strings[0] = "Unbekannte Straße";
        reader.close();

        return strings;
    }

    private void readElements(final String path, final int[][] nodes, final int[][] distributions,
            final Map<String, IQuadtree> quadtreeMap, final Map<String, IFactory<ICollectiveAccessor>> collectiveMap,
            final int minZoomStep) throws IOException {

        final String[] names = {"street", "way", "area", "building"};

        final CollectiveAccessorFactory[] accessors = new CollectiveAccessorFactory[names.length];
        accessors[0] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[0]);
        accessors[1] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[1]);
        accessors[2] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[2]);
        accessors[3] = new CollectiveAccessorFactory(nodes[0], nodes[1], distributions[3]);
        for (int i = 0; i < names.length; i++) {
            accessors[i].setData(readIntArray(path + "/" + names[i] + "s"));
            collectiveMap.put(names[i], accessors[i]);

            int[] elementData = readIntArray(path + "/" + names[i] + "Data");
            int[] treeData = readIntArray(path + "/" + names[i] + "Tree");
            quadtreeMap.put(names[i], new Quadtree(treeData, elementData, minZoomStep));
        }
    }

    private int[] readIntArray(final int length, final DataInputStream reader) throws IOException {
        final int[] ret = new int[length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = reader.readInt();
        }

        return ret;
    }

    private int[] readIntArray(final String path) throws IOException {
        final DataInputStream reader = createInputStream(path);

        final int[] ret = new int[reader.available() / 4];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = reader.readInt();
        }

        reader.close();

        return ret;
    }

    private static final BufferedImage createImage() {
        // return new SpecialImage(256, 256);
        return new BufferedImage(256, 256, BufferedImage.TYPE_USHORT_555_RGB);
        // return
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
        // .createCompatibleImage(256, 256, BufferedImage.TRANSLUCENT);
    }

    public static void main(final String[] args) {
        final IMapManager manager = new ImageTest().read();
        if (manager == null) {
            System.err.println("failed at reading");
            return;
        }

        final BufferedImage image = createImage();

        long start = System.currentTimeMillis();

        final BackgroundRenderer renderer = new BackgroundRenderer(manager, new OSMColorScheme());

        final int min = manager.getState().getMinZoom();
        final int max = Math.min(min + 8, manager.getState().getMaxZoom());
        for (int zoom = min; zoom < max; zoom++) {
            int size = 1 << (zoom - min);
            for (int row = 0; row < size; row++) {
                for (int column = 0; column < size; column++) {
                    final long id = manager.getID(row, column, zoom);
                    renderer.render(id, image);
                    clear(image);
                }
            }
        }

        System.out.println(System.currentTimeMillis() - start);
        System.out.println("non graphics: " + renderer.nonGraphicsTime);
        System.out.println("graphics: " + renderer.graphicsTime);
    }

    private static void clear(final BufferedImage image) {
        int count = 0;
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                if (set.add(image.getRGB(i, j))) {
                    ++count;
                }
            }
        }
        maxColors = Math.max(maxColors, count);

        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, 256, 256);
        g.dispose();
    }
}