import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;

import model.CompressedInputStream;
import model.elements.Area;
import model.elements.IArea;
import model.elements.IBuilding;
import model.elements.Label;
import model.elements.IMultiElement;
import model.elements.MultiElement;
import model.elements.POI;
import model.elements.IStreet;
import model.elements.Street;
import model.elements.IWay;
import model.elements.Way;
import model.map.IMapState;
import model.map.ITile;
import model.map.MapState;
import model.map.factories.ITileFactory;
import model.map.factories.StorageTileFactory;

public class ReaderTest {

    private CompressedInputStream reader;

    public void read(final File file) {
        MapManagerReader managerReader = new MapManagerReader();

        try {
            reader = new CompressedInputStream(new BufferedInputStream(new ProgressableInputStream(file)));
            readGraph();
            managerReader.readMapManager();
            readIndex(managerReader.origStreets, managerReader.labels);
            managerReader.cleanUp();
        } catch (final Exception e) {
            try {
                reader.close();
            } catch (final IOException e1) {
            }
            e.printStackTrace();
        }
    }

    /*
     * Reads the Graph-section of the tsk file and generates the Graph.
     */
    private void readGraph() throws IOException {
        reader.readCompressedInt();

        final int firstNodes = reader.readCompressedInt();
        final int oneways = reader.readCompressedInt();

        for (int i = 0; i < firstNodes; i++) {
            reader.readCompressedInt();
            reader.readCompressedInt();
            reader.readCompressedInt();
        }

        for (int i = 0; i < oneways; i++) {
            reader.readCompressedInt();
        }
    }

    private void readIndex(final IStreet[] streets, final Label[] labels) throws IOException {
        int cities = reader.readCompressedInt();

        for (int i = 0; i < cities; i++) {
            reader.readUTF();
        }

        reader.readCompressedInt();

        int maxCollisions = reader.readCompressedInt();

        for (int i = 0; i < maxCollisions; i++) {
            int occurances = reader.readCompressedInt();
            for (int j = 0; j < occurances; j++) {
                final String[] cityNames = new String[i + 1];
                reader.readCompressedInt();
                reader.readCompressedInt();

                for (int k = 1; k < cityNames.length; k++) {
                    reader.readCompressedInt();
                    reader.readCompressedInt();
                }
            }
        }
    }

    private class MapManagerReader {
        private Street[] origStreets;
        private Way[] origWays;
        private Area[] origAreas;

        private IStreet[] streets;
        private IWay[] ways;
        private IArea[] areas;

        private POI[] pois;
        private IBuilding[] buildings;
        private Label[] labels;
        private ITile[][][] tiles;
        private int[][] offsets;
        private String[] names;
        private String[] numbers;

        private int rows;

        private IMapState state;

        public void readMapManager() throws IOException {
            readHeader();
            readElements();
            readTiles();
        }

        private void readHeader() throws IOException {
            final int width = reader.readCompressedInt();
            final int height = reader.readCompressedInt();
            final int minZoomStep = reader.readCompressedInt();
            final int maxZoomStep = reader.readCompressedInt();

            final int zoomSteps = maxZoomStep - minZoomStep + 1;
            state = new MapState(width, height, minZoomStep, maxZoomStep);

            rows = reader.readCompressedInt();

            tiles = new ITile[zoomSteps][][];
            offsets = new int[zoomSteps][];

            reader.readDouble();
            reader.readCompressedInt();
            reader.readCompressedInt();
        }

        private void readElements() throws IOException {

            int count = 0;
            int points = reader.readCompressedInt();
            for (count = 0; count < points; count++) {
                reader.readCompressedInt();
                reader.readCompressedInt();
            }

            names = new String[reader.readCompressedInt()];
            for (count = 0; count < names.length; count++) {
                reader.readUTF();
            }

            numbers = new String[reader.readCompressedInt()];
            for (count = 0; count < numbers.length; count++) {
                reader.readUTF();
            }

            count = 0;
            int[] distribution = readIntArray(reader.readCompressedInt());

            origStreets = new Street[distribution[distribution.length - 1]];
            streets = new IStreet[origStreets.length];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    reader.readCompressedInt();
                    readPoints();
                    reader.readCompressedInt();
                }
            }

            count = 0;
            distribution = readIntArray(reader.readCompressedInt());
            origWays = new Way[distribution[distribution.length - 1]];
            ways = new IWay[origWays.length];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    readPoints();
                    reader.readCompressedInt();
                }
            }

            count = 0;
            distribution = readIntArray(reader.readCompressedInt());
            origAreas = new Area[distribution[distribution.length - 1]];
            areas = new IArea[origAreas.length];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    readPoints();
                }
            }

            count = 0;
            distribution = readIntArray(reader.readCompressedInt());
            pois = new POI[distribution[distribution.length - 1]];

            for (int i = 0; i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    reader.readCompressedInt();
                    reader.readCompressedInt();
                }
            }

            buildings = new IBuilding[reader.readCompressedInt()];
            final int streetNodes = reader.readCompressedInt();

            for (count = 0; count < streetNodes; count++) {
                readPoints();

                reader.readFloat();
                reader.readCompressedInt();
                reader.readCompressedInt();
            }

            for (; count < buildings.length; count++) {
                readPoints();
            }

            count = 0;
            distribution = readIntArray(reader.readCompressedInt());
            labels = new Label[distribution[distribution.length - 1]];

            for (int i = 0; i < Math.min(20, distribution.length); i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    reader.readCompressedInt();
                    reader.readCompressedInt();
                    reader.readUTF();
                }
            }

            for (int i = Math.min(20, distribution.length); i < distribution.length; i++) {
                final int number = distribution[i];
                for (; count < number; count++) {
                    reader.readCompressedInt();
                    reader.readCompressedInt();
                    reader.readUTF();
                    reader.readFloat();
                }
            }
        }

        private void readPoints() throws IOException {
            final int length = reader.readCompressedInt();
            for (int i = 0; i < length; i++) {
                reader.readCompressedInt();
            }
        }

        private void readTiles() throws IOException {
            int currentRows = rows;

            final ITileFactory factory = new StorageTileFactory(reader, pois, streets, ways, buildings, areas, labels);

            int[] totalFilledCount = new int[4];
            int totalShapedCount = 0;
            int[] threshHolds = new int[]{4, 12, 16, 6};

            for (int zoom = state.getMaxZoomStep(); zoom >= state.getMinZoomStep(); zoom--) {
                final int relativeZoom = zoom - state.getMinZoomStep();

                final BufferedImage[] images = new BufferedImage[4];

                if (zoom == 19)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(2471, 3416, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 18)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(1236, 1708, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 17)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(618, 854, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 16)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(309 * 2, 427 * 2, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 15)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(155 * 4, 214 * 4, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 14)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(78 * 8, 107 * 8, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 13)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(39 * 16, 54 * 16, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 12)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(20 * 32, 27 * 32, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 11)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(10 * 64, 14 * 64, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 10)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(5 * 128, 7 * 128, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 9)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(3 * 256, 4 * 256, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 8)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(2 * 512, 2 * 512, BufferedImage.TYPE_INT_ARGB);
                else if (zoom == 7)
                    for (int i = 0; i < 4; i++)
                        images[i] = new BufferedImage(1 * 1024, 1 * 1024, BufferedImage.TYPE_INT_ARGB);

                for (final BufferedImage image : images) {
                    if (image != null) {
                        final Graphics g = image.getGraphics();
                        g.setColor(new Color(255, 255, 255));
                        g.fillRect(0, 0, image.getWidth(), image.getHeight());
                        g.dispose();
                    }
                }

                tiles[relativeZoom] = new ITile[currentRows][];
                final int[] currentOffsets = new int[currentRows];
                offsets[relativeZoom] = currentOffsets;

                int[] filledCount = new int[]{0, 0, 0, 0};
                int[] shapedFreeCount = new int[]{0, 0, 0, 0};
                for (int row = 0; row < currentRows; row++) {
                    final int offset = reader.readCompressedInt();
                    currentOffsets[row] = offset;

                    final int columns = reader.readCompressedInt();
                    tiles[relativeZoom][row] = new ITile[columns];

                    for (int column = 0; column < columns; column++) {
                        tiles[relativeZoom][row][column] = factory.createTile(row, column + offset, zoom);

                        int[] localItemCount = new int[]{0, 0, 0, 0};
                        for (final Iterator<IWay> wayIt = tiles[relativeZoom][row][column].getWays(); wayIt.hasNext();) {
                            wayIt.next();
                            ++localItemCount[0];
                        }
                        for (final Iterator<IStreet> streetIt = tiles[relativeZoom][row][column].getStreets(); streetIt
                                .hasNext();) {
                            streetIt.next();
                            ++localItemCount[1];
                        }
                        for (final Iterator<IBuilding> buildingIt = tiles[relativeZoom][row][column].getBuildings(); buildingIt
                                .hasNext();) {
                            buildingIt.next();
                            ++localItemCount[2];
                        }
                        for (final Iterator<IArea> areaIt = tiles[relativeZoom][row][column].getTerrain(); areaIt
                                .hasNext();) {
                            areaIt.next();
                            ++localItemCount[3];
                        }

                        int factor = 1 << Math.max(17 - zoom, 0);
                        int x = factor * (column + offset);
                        int y = factor * row;

                        for (int im = 0; im < 4; im++) {
                            int value = 255 - Math.min(255, (int) (localItemCount[im] / threshHolds[im]) * 255);

                            if (value == 255) {
                                ++shapedFreeCount[im];
                            } else {
                                ++filledCount[im];
                            }
                            for (int i = 0; i < factor; i++) {
                                for (int j = 0; j < factor; j++) {
                                    images[im].setRGB(x + i, y + j, new Color(value, value, value).getRGB());
                                }
                            }
                        }
                    }
                }

                int factor = 1 << Math.max(17 - zoom, 0);
                for (int i = 0; i < 4; i++) {
                    int shapedCount = shapedFreeCount[i] + filledCount[i];
                    int unshapedCount = images[i].getHeight() * images[i].getWidth() / (factor * factor);
                    System.out.println("Shaped: " + filledCount[i] + ", " + shapedCount + " ("
                            + (100. * filledCount[i] / shapedCount) + "%)");
                    System.out.println("Unshaped: " + filledCount[i] + ", " + unshapedCount + " ("
                            + (100. * filledCount[i] / unshapedCount) + "%)");

                    if (images[i] != null)
                        ImageIO.write(images[i], "png", new File("Distribution [" + i + "] " + zoom + ".png"));

                    totalFilledCount[i] += filledCount[i];

                }

                totalShapedCount += shapedFreeCount[0] + filledCount[0];
                currentRows = (currentRows + 1) / 2;
                if (zoom != state.getMinZoomStep()) {
                    readSimplifications();
                }
            }
            System.out.println();
            System.out.println("Total tiles count: " + totalShapedCount);
            System.out.println("Total filled count: " + Arrays.toString(totalFilledCount));
        }

        private void readSimplifications() throws IOException {
            readSimplifications(origAreas, areas);
            readSimplifications(origWays, ways);
            readSimplifications(origStreets, streets);
        }

        private void readSimplifications(final MultiElement[] origElements, final IMultiElement[] elements)
                throws IOException {
            int size = reader.readCompressedInt();
            for (int i = 0; i < size; i++) {
                reader.readCompressedInt();
                readElementSimplifications();
            }
        }

        private void readElementSimplifications() throws IOException {
            final int simplifications = reader.readCompressedInt();

            for (int i = 0; i < simplifications; i++) {
                reader.readCompressedInt();
            }
        }

        private int[] readIntArray(final int length) throws IOException {
            final int[] ret = new int[length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = reader.readCompressedInt();
            }

            return ret;
        }

        public void cleanUp() {
            pois = null;
            streets = null;
            ways = null;
            areas = null;
            origStreets = null;
            origWays = null;
            buildings = null;
            origAreas = null;
            names = null;
            numbers = null;
            labels = null;
            tiles = null;
            offsets = null;

            state = null;
        }
    }

    private class ProgressableInputStream extends FileInputStream {
        private final int size;
        private int current;
        private int progress;

        public ProgressableInputStream(final File in) throws IOException {
            super(in);

            progress = -1;
            size = (int) Math.ceil(super.available() / 100.0);
        }

        @Override
        public int read() throws IOException {
            final int tmp = ++current / size;
            if (tmp != progress) {
                progress = tmp;
            }

            return super.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            final int nr = super.read(b, off, len);

            final int tmp = (current += nr) / size;
            if (tmp != progress) {
                progress = tmp;
            }

            return nr;
        }

        @Override
        public int read(final byte[] b) throws IOException {
            final int nr = super.read(b);

            final int tmp = (current += nr) / size;
            if (tmp != progress) {
                progress = tmp;
            }

            return nr;
        }
    }

    public static void main(String[] args) {
        new ReaderTest().read(new File("default.map"));
    }
}