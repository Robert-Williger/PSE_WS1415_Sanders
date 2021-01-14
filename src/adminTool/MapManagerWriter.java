package adminTool;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.PointLabel;
import adminTool.elements.Street;
import adminTool.elements.LineLabel;
import adminTool.elements.Typeable;
import adminTool.metrics.IDistanceMap;
import adminTool.metrics.PixelToCoordDistanceMap;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.QuadtreeWriter;
import adminTool.quadtree.CollisionlessQuadtree;
import adminTool.quadtree.ICollisionPolicy;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.policies.AreaQuadtreePolicy;
import adminTool.quadtree.policies.IQuadtreePolicy;
import adminTool.quadtree.policies.MultipleBoundingBoxQuadtreePolicy;
import adminTool.quadtree.policies.WayQuadtreePolicy;

public class MapManagerWriter extends AbstractMapFileWriter {
    private static final int TILE_LENGTH_BITS = 8;
    private static final int TILE_LENGTH = 1 << TILE_LENGTH_BITS;

    private static final int MAX_ELEMENTS_PER_TILE = 8;

    private static final int MAX_ZOOM_STEP = 19;
    private static final int MIN_ZOOM_STEP = 0;

    private static final int SCALE_FACTOR_BITS = 21;

    private static final int MAX_POI_PIXEL_WIDTH = 20;
    private static final int MAX_WAY_PIXEL_WIDTH = 17;

    private Collection<Street> streets;
    private Collection<MultiElement> areas;
    private Collection<Building> buildings;
    private Collection<LineLabel> lineLabels;
    private Collection<POI> pois;
    private Collection<PointLabel> pointLabels;

    private final Rectangle2D bounds;
    private final IPointAccess points;
    private IntConversion conversion;

    // TODO improve this!
    public Sorting<Street> streetSorting;

    public MapManagerWriter(final Collection<Street> streets, final Collection<MultiElement> terrain,
            final Collection<Building> buildings, final Collection<LineLabel> lineLabels, final Collection<POI> pois,
            final Collection<PointLabel> pointLabels, final IPointAccess points, final Rectangle2D bounds,
            final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.streets = streets;
        this.areas = terrain;
        this.buildings = buildings;
        this.lineLabels = lineLabels;
        this.pois = pois;
        this.pointLabels = pointLabels;
        this.points = points;
        this.bounds = bounds;
    }

    @Override
    public void write() throws IOException {
        // final int coordBits = Math.getExponent(Math.max(size.getWidth(), size.getHeight())) + 1;
        // final int shiftBits = Integer.SIZE - 1 - coordBits;
        final double shift = 1 << 29; // TODO improve this!
        // final double shift = Math.pow(2, shiftBits);
        conversion = (value) -> (int) Math.round(value * shift);

        final int zoomOffset = 29 - MIN_ZOOM_STEP;
        final double coordMapSize = (1 << zoomOffset) / shift;

        final int minZoomStep = MIN_ZOOM_STEP;
        final int maxZoomStep = MAX_ZOOM_STEP;
        final int zoomSteps = maxZoomStep - minZoomStep + 1;
        final int conversionBits = SCALE_FACTOR_BITS;

        writeHeader(minZoomStep, maxZoomStep, conversionBits);

        final Sorting<MultiElement> areaSorting = sort(areas, new MultiElement[areas.size()]);
        areas = null;

        streetSorting = sort(streets, new Street[streets.size()]);
        streets = null;

        final Sorting<Building> buildingSorting = sort(buildings, new Building[buildings.size()]);
        buildings = null;

        final Sorting<LineLabel> lineLabelSorting = sort(lineLabels, new LineLabel[lineLabels.size()]);
        lineLabels = null;

        final Sorting<POI> poiSorting = sort(pois, new POI[pois.size()]);
        pois = null;

        final Sorting<PointLabel> pointLabelSorting = sort(pointLabels, new PointLabel[pointLabels.size()]);
        pointLabels = null;

        ElementWriter elementWriter = new ElementWriter(areaSorting, streetSorting, buildingSorting, lineLabelSorting,
                poiSorting, pointLabelSorting, points, conversion, zipOutput);
        elementWriter.write();

        //
        //
        //

        final double[] maxWayWidths = new double[zoomSteps];
        for (int zoom = minZoomStep; zoom <= maxZoomStep; ++zoom)
            maxWayWidths[zoom - minZoomStep] = new PixelToCoordDistanceMap(zoom).map(MAX_WAY_PIXEL_WIDTH);

        final String[] collectiveNames = new String[] { "area", "street", "building", "lineLabel" };
        final IQuadtreePolicy[] policies = new IQuadtreePolicy[collectiveNames.length];
        final int[] elements = new int[] { areaSorting.elements.length, streetSorting.elements.length,
                buildingSorting.elements.length, lineLabelSorting.elements.length };

        policies[0] = new AreaQuadtreePolicy(Arrays.asList(areaSorting.elements), points);
        policies[1] = new WayQuadtreePolicy(Arrays.asList(streetSorting.elements), points, maxWayWidths);
        policies[2] = new AreaQuadtreePolicy(Arrays.asList(buildingSorting.elements), points);
        policies[3] = new WayQuadtreePolicy(Arrays.asList(lineLabelSorting.elements), points, maxWayWidths);

        for (int i = 0; i < collectiveNames.length; i++) {
            long start = System.currentTimeMillis();
            final IQuadtree tree = new Quadtree(elements[i], policies[i], coordMapSize, zoomSteps,
                    MAX_ELEMENTS_PER_TILE);
            System.out.println(collectiveNames[i] + " creation: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            new QuadtreeWriter(tree, collectiveNames[i], zipOutput).write();
            System.out.println(collectiveNames[i] + " writing: " + (System.currentTimeMillis() - start));
        }

        CollisionlessQuadtree tree = createPointLabelQuadtree(pointLabelSorting.elements, minZoomStep, zoomSteps,
                coordMapSize, conversionBits);
        new QuadtreeWriter(tree, "pointLabel", zipOutput).write();

        tree = createPOIQuadtree(poiSorting.elements, minZoomStep, zoomSteps, coordMapSize, conversionBits);
        new QuadtreeWriter(tree, "poi", zipOutput).write();
    }

    private CollisionlessQuadtree createPointLabelQuadtree(final PointLabel[] labels, final int minZoomStep,
            final int zoomSteps, final double mapSize, final int conversionBits) {
        final Font font = new Font("TimesRoman", Font.PLAIN, 18);
        final FontRenderContext c = new FontRenderContext(new AffineTransform(), true, true);
        final List<List<Rectangle2D>> labelBounds = new ArrayList<>(zoomSteps);

        for (int h = 0; h < zoomSteps; h++) {
            final IDistanceMap pixelToCoord = new PixelToCoordDistanceMap(h + minZoomStep);
            final List<Rectangle2D> bounds = new ArrayList<>(labels.length);
            for (int i = 0; i < labels.length; i++) {
                final PointLabel label = labels[i];
                final Rectangle2D stringBounds = font.getStringBounds(label.getName(), c);
                final double cw = pixelToCoord.map(stringBounds.getWidth());
                final double ch = pixelToCoord.map(stringBounds.getHeight());

                bounds.add(new Rectangle2D.Double(points.getX(label.getPoint()) - cw / 2,
                        points.getY(label.getPoint()) - ch / 2, cw, ch));
            }
            labelBounds.add(bounds);
        }
        final ICollisionPolicy cp = (e1, e2, height) -> labelBounds.get(height).get(e1)
                .intersects(labelBounds.get(height).get(e2));
        final IQuadtreePolicy qp = new MultipleBoundingBoxQuadtreePolicy(labelBounds);

        return new CollisionlessQuadtree(labels.length, qp, cp, mapSize, zoomSteps);
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
                bounds.add(new Rectangle2D.Double(points.getX(poi.getPoint()) - size / 2,
                        points.getY(poi.getPoint()) - size / 2, size, size));
            }
            poiBounds.add(bounds);
        }
        final ICollisionPolicy cp = (e1, e2, height) -> poiBounds.get(height).get(e1)
                .intersects(poiBounds.get(height).get(e2));
        final IQuadtreePolicy qp = new MultipleBoundingBoxQuadtreePolicy(poiBounds);

        return new CollisionlessQuadtree(pois.length, qp, cp, mapSize, zoomSteps);
    }

    private <T extends Typeable> Sorting<T> sort(final Collection<T> elements, final T[] data) {
        TypeSorter<T> sorter = new TypeSorter<>(elements, data);
        return sorter.sort();
    }

    private void writeHeader(final int minZoomStep, final int maxZoomStep, final int conversionBits)
            throws IOException {
        putNextEntry("header");

        dataOutput.writeInt(conversionBits);
        dataOutput.writeInt(conversion.convert(bounds.getX()));
        dataOutput.writeInt(conversion.convert(bounds.getY()));
        dataOutput.writeInt(conversion.convert(bounds.getWidth()));
        dataOutput.writeInt(conversion.convert(bounds.getHeight()));
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