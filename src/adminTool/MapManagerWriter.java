package adminTool;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.Label;
import adminTool.elements.Typeable;
import adminTool.quadtree.AreaQuadtreePolicy;
import adminTool.quadtree.MultipleBoundingBoxQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.QuadtreeWriter;
import adminTool.quadtree.CollisionlessQuadtree;
import adminTool.quadtree.ICollisionPolicy;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.WayQuadtreePolicy;

public class MapManagerWriter extends AbstractMapFileWriter {

    // number of tiles in lowest zoom step per dimension
    private static final int MIN_TILES = 1;

    private static final int TILE_LENGTH_BITS = 8;
    private static final int TILE_LENGTH = 1 << TILE_LENGTH_BITS;

    private static final int MAX_ELEMENTS_PER_TILE = 4;

    private static final int MAX_ZOOM_STEP = 19;
    private static final int MIN_ZOOM_STEP = 0;
    private static final int MAX_ZOOM_STEPS = 15;

    private static final int SCALE_FACTOR_BITS = 21;

    private static final int MAX_POI_PIXEL_WIDTH = 20;
    private static final int MAX_WAY_PIXEL_WIDTH = 17;

    private Collection<Street> streets;
    private Collection<MultiElement> areas;
    private Collection<Building> buildings;
    private Collection<Label> labels;
    private Collection<POI> pois;

    private final Dimension2D size;
    private final IPointAccess points;
    private IntConversion conversion;

    // TODO improve this!
    public Sorting<Street> streetSorting;

    public MapManagerWriter(final Collection<Street> streets, final Collection<MultiElement> terrain,
            final Collection<Building> buildings, final Collection<POI> pois, final Collection<Label> labels,
            final IPointAccess points, final Dimension2D size, final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.streets = streets;
        this.areas = terrain;
        this.buildings = buildings;
        this.labels = labels;
        this.pois = pois;
        this.points = points;
        this.size = size;
    }

    @Override
    public void write() throws IOException {
        // final int coordBits = Math.getExponent(Math.max(size.getWidth(), size.getHeight())) + 1;
        // final int shiftBits = Integer.SIZE - 1 - coordBits;
        final double shift = 1 << 29; // TODO improve this!
        // final double shift = Math.pow(2, shiftBits);
        conversion = (value) -> (int) Math.round(value * shift);

        final int zoomOffset = (int) Math.ceil(log2(
                Math.max(conversion.convert(size.getWidth()), conversion.convert(size.getHeight())) / (int) MIN_TILES));
        final double coordMapSize = (1 << zoomOffset) / shift;

        final int minZoomStep = Math.max(MIN_ZOOM_STEP, SCALE_FACTOR_BITS + TILE_LENGTH_BITS - zoomOffset);
        final int maxZoomStep = Math.min(MAX_ZOOM_STEP, minZoomStep + MAX_ZOOM_STEPS - 1);
        final int zoomSteps = maxZoomStep - minZoomStep + 1;
        final int conversionBits = SCALE_FACTOR_BITS;

        writeHeader(minZoomStep, maxZoomStep, conversionBits);

        final Sorting<MultiElement> areaSorting = sort(areas, new MultiElement[areas.size()]);
        areas = null;

        streetSorting = sort(streets, new Street[streets.size()]);
        streets = null;

        final Sorting<Building> buildingSorting = sort(buildings, new Building[buildings.size()]);
        buildings = null;

        final Sorting<Label> labelSorting = sort(labels, new Label[labels.size()]);
        labels = null;

        final Sorting<POI> poiSorting = sort(pois, new POI[pois.size()]);
        pois = null;

        ElementWriter elementWriter = new ElementWriter(areaSorting, streetSorting, buildingSorting, labelSorting,
                poiSorting, points, conversion, zipOutput);
        elementWriter.write();

        //
        //
        //

        final double[] maxWayWidths = new double[zoomSteps];
        for (int zoom = minZoomStep; zoom <= maxZoomStep; ++zoom) {
            maxWayWidths[zoom - minZoomStep] = (MAX_WAY_PIXEL_WIDTH << (conversionBits - zoom)) / shift; // TODO improve
                                                                                                         // this!
        }

        final String[] names = new String[] { "area", "street", "building" };
        final IQuadtreePolicy[] policies = new IQuadtreePolicy[names.length];
        final int[] elements = new int[] { areaSorting.elements.length, streetSorting.elements.length,
                buildingSorting.elements.length };

        policies[0] = new AreaQuadtreePolicy(Arrays.asList(areaSorting.elements), points);
        policies[1] = new WayQuadtreePolicy(Arrays.asList(streetSorting.elements), points, maxWayWidths);
        policies[2] = new AreaQuadtreePolicy(Arrays.asList(buildingSorting.elements), points);

        for (int i = 0; i < names.length; i++) {
            final IQuadtree tree = new Quadtree(elements[i], policies[i], coordMapSize, zoomSteps,
                    MAX_ELEMENTS_PER_TILE);
            new QuadtreeWriter(tree, names[i], zipOutput).write();
        }

        putNextEntry("labelTree");
        CollisionlessQuadtree tree = createLabelQuadtree(labelSorting.elements, minZoomStep, zoomSteps, coordMapSize,
                conversionBits);
        for (final OfInt iterator = tree.toList().iterator(); iterator.hasNext();) {
            dataOutput.writeInt(iterator.nextInt());
        }
        closeEntry();

        putNextEntry("poiTree");
        tree = createPOIQuadtree(poiSorting.elements, minZoomStep, zoomSteps, coordMapSize, conversionBits);
        for (final OfInt iterator = tree.toList().iterator(); iterator.hasNext();) {
            dataOutput.writeInt(iterator.nextInt());
        }
        closeEntry();
    }

    private CollisionlessQuadtree createLabelQuadtree(final Label[] labels, final int minZoomStep, final int zoomSteps,
            final double mapSize, final int conversionBits) {
        final Font font = new Font("TimesRoman", Font.PLAIN, 18);
        final FontRenderContext c = new FontRenderContext(new AffineTransform(), true, true);
        final List<List<Rectangle2D>> labelBounds = new ArrayList<>(zoomSteps);

        for (int h = 0; h < zoomSteps; h++) {
            labelBounds.add(new ArrayList<>(labels.length));
        }

        final double shift = 1 << 29;
        for (int i = 0; i < labels.length; i++) {
            final Label label = labels[i];
            final Rectangle2D stringBounds = font.getStringBounds(label.getName(), c);
            final double pw = stringBounds.getWidth();
            final double ph = stringBounds.getHeight();
            for (int h = 0; h < zoomSteps; h++) {
                final double cw = pw * (1 << (conversionBits - (h + minZoomStep))) / shift;
                final double ch = ph * (1 << (conversionBits - (h + minZoomStep))) / shift;
                labelBounds.get(h).add(new Rectangle2D.Double(points.getX(label.getNode()) - cw / 2,
                        points.getY(label.getNode()) - ch / 2, cw, ch));
            }
        }
        final ICollisionPolicy cp = (e1, e2, height) -> labelBounds.get(height).get(e1)
                .intersects(labelBounds.get(height).get(e2));
        final IQuadtreePolicy qp = new MultipleBoundingBoxQuadtreePolicy(labelBounds);

        return new CollisionlessQuadtree(labels.length, zoomSteps, qp, cp, mapSize);
    }

    private CollisionlessQuadtree createPOIQuadtree(final POI[] pois, final int minZoomStep, final int zoomSteps,
            final double mapSize, final int conversionBits) {
        final List<List<Rectangle2D>> poiBounds = new ArrayList<>(zoomSteps);

        final double shift = 1 << 29;
        for (int h = 0; h < zoomSteps; h++) {
            final List<Rectangle2D> bounds = new ArrayList<>(pois.length);
            final double size = (MAX_POI_PIXEL_WIDTH << (conversionBits - (h + minZoomStep))) / shift;
            for (int i = 0; i < pois.length; i++) {
                final POI poi = pois[i];
                bounds.add(new Rectangle2D.Double(points.getX(poi.getNode()) - size / 2,
                        points.getY(poi.getNode()) - size / 2, size, size));
            }
            poiBounds.add(bounds);
        }
        final ICollisionPolicy cp = (e1, e2, height) -> poiBounds.get(height).get(e1)
                .intersects(poiBounds.get(height).get(e2));
        final IQuadtreePolicy qp = new MultipleBoundingBoxQuadtreePolicy(poiBounds);

        return new CollisionlessQuadtree(pois.length, zoomSteps, qp, cp, mapSize);
    }

    private <T extends Typeable> Sorting<T> sort(final Collection<T> elements, final T[] data) {
        TypeSorter<T> sorter = new TypeSorter<>(elements, data);
        return sorter.sort();
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private void writeHeader(final int minZoomStep, final int maxZoomStep, final int conversionBits)
            throws IOException {
        putNextEntry("header");

        dataOutput.writeInt(conversionBits);
        dataOutput.writeInt(conversion.convert(size.getWidth()));
        dataOutput.writeInt(conversion.convert(size.getHeight()));
        dataOutput.writeInt(minZoomStep);
        dataOutput.writeInt(maxZoomStep);
        dataOutput.writeInt(TILE_LENGTH);

        closeEntry();
    }

    private static class TypeSorter<T extends Typeable> {

        private final Collection<T> source;
        private final Sorting<T> sorting;

        public TypeSorter(final Collection<T> source, final T[] destination) {
            this.source = source;
            sorting = new Sorting<>(destination, new int[getMaximumType(source) + 1]);
        }

        public Sorting<T> sort() {
            for (final T t : source) {
                ++sorting.distribution[t.getType()];
            }
            int[] stack = new int[sorting.distribution.length];
            stack[0] = -1;
            for (int i = 1; i < stack.length; i++) {
                stack[i] = stack[i - 1] + sorting.distribution[i - 1];
            }

            for (final T t : source) {
                sorting.elements[++stack[t.getType()]] = t;
            }

            return sorting;
        }

        private int getMaximumType(final Collection<T> source) {
            int maxValue = 0;
            for (final Typeable t : source) {
                if (t.getType() > maxValue) {
                    maxValue = t.getType();
                }
            }
            return maxValue;
        }
    }
}