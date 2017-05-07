package adminTool;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.PrimitiveIterator.OfInt;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.Typeable;
import adminTool.elements.Way;
import adminTool.quadtree.AreaQuadtreePolicy;
import adminTool.quadtree.BoundingBoxQuadtreePolicy;
import adminTool.quadtree.CollisionlessQuadtree;
import adminTool.quadtree.ICollisionPolicy;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.StoredQuadtreeWriter;
import adminTool.quadtree.WayQuadtreePolicy;

public class MapManagerWriter extends AbstractMapFileWriter {

    // number of tiles in lowest zoom step
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

    private Rectangle bounds;

    private Collection<Street> streets;
    private Collection<Area> areas;
    private Collection<Way> ways;
    private Collection<Building> buildings;
    private Collection<Label> labels;
    private Collection<POI> pois;

    // TODO improve this!
    public Sorting<Street> streetSorting;

    public MapManagerWriter(final Collection<Building> buildings, final Collection<Street> streets,
            final Collection<POI> pois, final Collection<Way> ways, final Collection<Area> terrain,
            final Collection<Label> labels, final Rectangle boundingBox, final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.bounds = boundingBox;
        this.streets = streets;
        this.areas = terrain;
        this.ways = ways;
        this.buildings = buildings;
        this.labels = labels;
        this.pois = pois;
    }

    @Override
    public void write() throws IOException {
        final int zoomOffset = (int) Math.ceil(log2(Math.min(bounds.getWidth(), bounds.getHeight()) / MIN_TILES));
        final int coordMapSize = (1 << zoomOffset);

        final int minZoomStep = Math.max(MIN_ZOOM_STEP, SCALE_FACTOR_BITS + TILE_LENGTH_BITS - zoomOffset);
        final int maxZoomStep = Math.min(MAX_ZOOM_STEP, minZoomStep + MAX_ZOOM_STEPS - 1);
        final int zoomSteps = maxZoomStep - minZoomStep + 1;
        final int conversionBits = SCALE_FACTOR_BITS;

        writeHeader(minZoomStep, maxZoomStep, conversionBits);

        final Sorting<Area> areaSorting = sort(areas, new Area[areas.size()]);
        areas = null;

        final Sorting<Way> waySorting = sort(ways, new Way[ways.size()]);
        ways = null;

        streetSorting = sort(streets, new Street[streets.size()]);
        streets = null;

        final Sorting<Building> buildingSorting = sort(buildings, new Building[buildings.size()]);
        buildings = null;

        final Sorting<Label> labelSorting = sort(labels, new Label[labels.size()]);
        labels = null;

        final Sorting<POI> poiSorting = sort(pois, new POI[pois.size()]);
        pois = null;

        ElementWriter elementWriter = new ElementWriter(areaSorting, streetSorting, waySorting, buildingSorting,
                labelSorting, poiSorting, zipOutput);
        elementWriter.write();

        //
        //
        //

        // TODO improve this
        final int[] maxWayWidths = new int[zoomSteps];
        for (int zoom = minZoomStep; zoom < maxZoomStep; ++zoom) {
            maxWayWidths[zoom - minZoomStep] = MAX_WAY_PIXEL_WIDTH << (conversionBits - (zoom + 3));
        }

        final String[] names = new String[] { "area", "way", "street", "building" };
        final IQuadtreePolicy[] policies = new IQuadtreePolicy[names.length];
        final int[] elements = new int[] { areaSorting.elements.length, waySorting.elements.length,
                streetSorting.elements.length, buildingSorting.elements.length };

        policies[0] = new AreaQuadtreePolicy(areaSorting.elements, getBounds(areaSorting, zoomSteps),
                MAX_ELEMENTS_PER_TILE);
        policies[1] = new WayQuadtreePolicy(waySorting.elements, getBounds(waySorting, zoomSteps),
                MAX_ELEMENTS_PER_TILE, maxWayWidths);
        policies[2] = new WayQuadtreePolicy(streetSorting.elements, getBounds(streetSorting, zoomSteps),
                MAX_ELEMENTS_PER_TILE, maxWayWidths);
        policies[3] = new AreaQuadtreePolicy(buildingSorting.elements, getBounds(buildingSorting, zoomSteps),
                MAX_ELEMENTS_PER_TILE);

        for (int i = 0; i < names.length; i++) {
            new StoredQuadtreeWriter(policies[i], zipOutput, names[i], elements[i], coordMapSize).write();
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

    private Rectangle[][] getBounds(final Sorting<? extends MultiElement> sorting, final int zoomSteps) {
        final MultiElement[] elements = sorting.elements;
        final Rectangle[][] ret = new Rectangle[zoomSteps][];
        final Rectangle[] bounds = new Rectangle[elements.length];
        for (int i = 0; i < elements.length; i++) {
            bounds[i] = Util.getBounds(elements[i].getNodes());
        }
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bounds;
        }
        return ret;
    }

    private CollisionlessQuadtree createLabelQuadtree(final Label[] labels, final int minZoomStep, final int zoomSteps,
            final int mapSize, final int conversionBits) {
        final Font font = new Font("TimesRoman", Font.PLAIN, 18);
        final FontRenderContext c = new FontRenderContext(new AffineTransform(), true, true);
        final Rectangle[][] lBounds = new Rectangle[zoomSteps][labels.length];

        for (int i = 0; i < labels.length; i++) {
            final Label label = labels[i];
            final Rectangle2D bounds = font.getStringBounds(label.getName(), c);
            final int pw = (int) Math.ceil(bounds.getWidth());
            final int ph = (int) Math.ceil(bounds.getHeight());
            for (int h = 0; h < zoomSteps; h++) {
                final int cw = pw << (conversionBits - (h + minZoomStep));
                final int ch = ph << (conversionBits - (h + minZoomStep));
                lBounds[h][i] = new Rectangle(label.getX() - cw / 2, label.getY() - ch / 2, cw, ch);
            }
        }
        final ICollisionPolicy cp = (e1, e2, height) -> lBounds[height][e1].intersects(lBounds[height][e2]);
        final IQuadtreePolicy qp = new BoundingBoxQuadtreePolicy(lBounds, MAX_ELEMENTS_PER_TILE);

        return new CollisionlessQuadtree(labels.length, qp, cp, mapSize);
    }

    private CollisionlessQuadtree createPOIQuadtree(final POI[] pois, final int minZoomStep, final int zoomSteps,
            final int mapSize, final int conversionBits) {
        final Rectangle[][] lBounds = new Rectangle[zoomSteps][pois.length];

        for (int h = 0; h < zoomSteps; h++) {
            final int size = MAX_POI_PIXEL_WIDTH << (conversionBits - (h + minZoomStep));
            for (int i = 0; i < pois.length; i++) {
                final POI poi = pois[i];
                lBounds[h][i] = new Rectangle(poi.getX() - size / 2, poi.getY() - size / 2, size, size);
            }
        }
        final ICollisionPolicy cp = (e1, e2, height) -> lBounds[height][e1].intersects(lBounds[height][e2]);
        final IQuadtreePolicy qp = new BoundingBoxQuadtreePolicy(lBounds, MAX_ELEMENTS_PER_TILE);

        return new CollisionlessQuadtree(pois.length, qp, cp, mapSize);
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
        dataOutput.writeInt(bounds.width);
        dataOutput.writeInt(bounds.height);
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