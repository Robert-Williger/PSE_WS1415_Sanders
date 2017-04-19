package adminTool.map;

import java.awt.Rectangle;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.Typeable;
import adminTool.elements.Way;

public class MapManagerCreator extends CompressedWriter {

    // number of tiles in lowest zoom step
    private static final int MIN_TILES = 1;

    private static final int TILE_LENGTH_BITS = 8;
    private static final int TILE_LENGTH = 1 << TILE_LENGTH_BITS;

    private static final int MAX_ELEMENTS_PER_TILE = 4;

    private static final int MAX_ZOOM_STEP = 19;
    private static final int MIN_ZOOM_STEP = 0;
    private static final int MAX_ZOOM_STEPS = 15;

    private static final int SCALE_FACTOR_BITS = 21;

    private static final int MAX_WAY_PIXEL_WIDTH = 17;

    private Rectangle bounds;

    private final ZipOutputStream zipOutput;

    private Collection<Street> streets;
    private Collection<Area> areas;
    private Collection<Way> ways;
    private Collection<Building> buildings;
    private Collection<Label> labels;

    public MapManagerCreator(final Collection<Building> buildings, final Collection<Street> streets,
            final Collection<POI> pois, final Collection<Way> ways, final Collection<Area> terrain,
            final Collection<Label> labels, final Rectangle boundingBox, final ZipOutputStream zipOutput) {
        this.bounds = boundingBox;
        this.streets = streets;
        this.areas = terrain;
        this.ways = ways;
        this.buildings = buildings;
        this.labels = labels;
        this.zipOutput = zipOutput;
    }

    public void create() throws IOException {
        final int zoomOffset = (int) Math.ceil(log2(Math.min(bounds.getWidth(), bounds.getHeight()) / MIN_TILES));
        final int coordMapSize = (1 << zoomOffset);

        final int minZoomStep = Math.max(MIN_ZOOM_STEP, SCALE_FACTOR_BITS + TILE_LENGTH_BITS - zoomOffset);
        final int maxZoomStep = Math.min(MAX_ZOOM_STEP, minZoomStep + MAX_ZOOM_STEPS - 1);
        final int conversionBits = SCALE_FACTOR_BITS;

        final DataOutputStream dataOutput = new DataOutputStream(zipOutput);

        writeHeader(dataOutput, minZoomStep, maxZoomStep, conversionBits);

        final Sorting<Area> areaSorting = sort(areas, new Area[areas.size()]);
        areas = null;

        final Sorting<Way> waySorting = sort(ways, new Way[ways.size()]);
        ways = null;

        final Sorting<Street> streetSorting = sort(streets, new Street[streets.size()]);
        streets = null;

        final Sorting<Building> buildingSorting = sort(buildings, new Building[buildings.size()]);
        buildings = null;

        final Sorting<Label> labelSorting = sort(labels, new Label[labels.size()]);
        labels = null;

        ElementWriter elementWriter = new ElementWriter(areaSorting, streetSorting, waySorting, buildingSorting,
                labelSorting, zipOutput);

        elementWriter.write();

        //
        //
        //

        // TODO improve this
        final int[] maxWayCoordWidths = new int[MAX_ZOOM_STEPS];
        for (int zoom = minZoomStep; zoom < maxZoomStep; ++zoom) {
            maxWayCoordWidths[zoom - minZoomStep] = MAX_WAY_PIXEL_WIDTH << (conversionBits - (zoom + 3));
        }

        new AreaQuadtreeWriter(areaSorting.elements, elementWriter.areaAddresses, zipOutput, "area",
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();

        new WayQuadtreeWriter(waySorting.elements, elementWriter.wayAddresses, zipOutput, "way", MAX_ELEMENTS_PER_TILE,
                MAX_ZOOM_STEPS, coordMapSize, maxWayCoordWidths).write();

        new WayQuadtreeWriter(streetSorting.elements, elementWriter.streetAddresses, zipOutput, "street",
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize, maxWayCoordWidths).write();

        new AreaQuadtreeWriter(buildingSorting.elements, elementWriter.buildingAddresses, zipOutput, "building",
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();
    }

    private <T extends Typeable> Sorting<T> sort(final Collection<T> elements, final T[] data) {
        TypeSorter<T> sorter = new TypeSorter<>(elements, data);
        return sorter.sort();
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private void writeHeader(final DataOutput output, final int minZoomStep, final int maxZoomStep,
            final int conversionBits) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("header"));

        output.writeInt(conversionBits);
        output.writeInt(bounds.width);
        output.writeInt(bounds.height);
        output.writeInt(minZoomStep);
        output.writeInt(maxZoomStep);
        output.writeInt(TILE_LENGTH);

        zipOutput.closeEntry();
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