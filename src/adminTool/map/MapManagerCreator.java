package adminTool.map;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.Typeable;
import adminTool.elements.Way;

public class MapManagerCreator extends CompressedWriter {

    // number of tiles in lowest zoom step
    private static final int     MIN_TILES             = 1;

    private static final int     TILE_LENGTH_BITS      = 8;
    private static final int     TILE_LENGTH           = 1 << TILE_LENGTH_BITS;

    private static final int     MAX_ELEMENTS_PER_TILE = 4;

    private static final int     MAX_ZOOM_STEP         = 19;
    private static final int     MIN_ZOOM_STEP         = 0;
    private static final int     MAX_ZOOM_STEPS        = 13;

    private static final int     SCALE_FACTOR_BITS     = 21;
    private static final int     SCALE_FACTOR          = 1 << SCALE_FACTOR_BITS;

    private Rectangle            bounds;

    private String               path;

    private Collection<Street>   streets;
    private Collection<Area>     areas;
    private Collection<Way>      ways;
    private Collection<Building> buildings;

    public MapManagerCreator(final Collection<Building> buildings, final Collection<Street> streets,
            final Collection<POI> pois, final Collection<Way> ways, final Collection<Area> terrain,
            final Collection<Label> labels, final Rectangle boundingBox, final String filePath) {
        this.bounds = boundingBox;
        this.streets = streets;
        this.areas = terrain;
        this.ways = ways;
        this.buildings = buildings;
        this.path = filePath;
    }

    public void create() throws IOException {
        final int zoomOffset = (int) Math.ceil(log2(Math.min(bounds.getWidth(), bounds.getHeight()) / MIN_TILES));
        final int coordMapSize = (1 << zoomOffset);

        final int minZoomStep = Math.max(MIN_ZOOM_STEP, SCALE_FACTOR_BITS + TILE_LENGTH_BITS - zoomOffset);
        final int maxZoomStep = Math.min(MAX_ZOOM_STEP, minZoomStep + MAX_ZOOM_STEPS - 1);
        final int conversionFactor = SCALE_FACTOR;

        final DataOutputStream headerOutput = createOutputStream("header");

        writeHeader(headerOutput, minZoomStep, maxZoomStep, conversionFactor);

        TypeSorter<Area> areaSorter = new TypeSorter<>(areas, new Area[areas.size()]);
        Sorting<Area> areaSorting = areaSorter.sort();
        areaSorter = null;
        areas = null;

        TypeSorter<Way> waySorter = new TypeSorter<>(ways, new Way[ways.size()]);
        Sorting<Way> waySorting = waySorter.sort();
        waySorter = null;
        ways = null;

        TypeSorter<Street> streetSorter = new TypeSorter<>(streets, new Street[streets.size()]);
        Sorting<Street> streetSorting = streetSorter.sort();
        streetSorter = null;
        streets = null;

        TypeSorter<Building> buildingSorter = new TypeSorter<>(buildings, new Building[buildings.size()]);
        Sorting<Building> buildingSorting = buildingSorter.sort();
        buildingSorter = null;
        buildings = null;

        final DataOutputStream areaOutput = createOutputStream("areas");
        final DataOutputStream streetOutput = createOutputStream("streets");
        final DataOutputStream wayOutput = createOutputStream("ways");
        final DataOutputStream buildingOutput = createOutputStream("buildings");
        final DataOutputStream stringOutput = createOutputStream("strings");
        final DataOutputStream nodeOutput = createOutputStream("nodes");

        ElementWriter elementWriter = new ElementWriter(areaSorting, streetSorting, waySorting, buildingSorting,
                headerOutput, nodeOutput, stringOutput, areaOutput, streetOutput, wayOutput, buildingOutput);

        elementWriter.write();

        headerOutput.close();
        areaOutput.close();
        streetOutput.close();
        wayOutput.close();
        buildingOutput.close();
        stringOutput.close();
        nodeOutput.close();

        //
        //
        //

        DataOutputStream elementData = createOutputStream("areaData");
        DataOutputStream treeData = createOutputStream("areaTree");
        new AreaQuadtreeWriter(areaSorting.elements, elementWriter.areaAddresses, elementData, treeData,
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();
        elementData.close();
        treeData.close();

        elementData = createOutputStream("wayData");
        treeData = createOutputStream("wayTree");
        new WayQuadtreeWriter(waySorting.elements, elementWriter.wayAddresses, elementData, treeData,
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();
        elementData.close();
        treeData.close();

        elementData = createOutputStream("streetData");
        treeData = createOutputStream("streetTree");
        new WayQuadtreeWriter(streetSorting.elements, elementWriter.streetAddresses, elementData, treeData,
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();
        elementData.close();
        treeData.close();

        elementData = createOutputStream("buildingData");
        treeData = createOutputStream("buildingTree");
        new AreaQuadtreeWriter(buildingSorting.elements, elementWriter.buildingAddresses, elementData, treeData,
                MAX_ELEMENTS_PER_TILE, MAX_ZOOM_STEPS, coordMapSize).write();
        elementData.close();
        treeData.close();
    }

    private DataOutputStream createOutputStream(final String name) throws FileNotFoundException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path + "/" + name)));
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private void writeHeader(final DataOutput output, final int minZoomStep, final int maxZoomStep,
            final double conversionFactor) throws IOException {
        output.writeInt(bounds.width);
        output.writeInt(bounds.height);
        output.writeInt(minZoomStep);
        output.writeInt(maxZoomStep);
        output.writeDouble(conversionFactor);
        output.writeInt(TILE_LENGTH);
    }

    private static class TypeSorter<T extends Typeable> {

        private final Collection<T> source;
        private final Sorting<T>    sorting;

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