package adminTool;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.Way;

//TODO compress
public class ElementWriter extends AbstractMapFileWriter {
    private LinkedHashMap<Node, Integer> nodeMap;
    private LinkedHashMap<String, Integer> stringMap;

    private Sorting<Area> areas;
    private Sorting<Street> streets;
    private Sorting<Way> ways;
    private Sorting<Building> buildings;
    private Sorting<Label> labels;
    private Sorting<POI> pois;

    public ElementWriter(

            final Sorting<Area> areas, final Sorting<Street> streets, final Sorting<Way> ways,
            final Sorting<Building> buildings, final Sorting<Label> labels, final Sorting<POI> pois,
            final ZipOutputStream zipOutput

    ) {
        super(zipOutput);

        this.areas = areas;
        this.streets = streets;
        this.ways = ways;
        this.buildings = buildings;
        this.labels = labels;
        this.pois = pois;
    }

    public void write() {
        nodeMap = createNodeMap();
        try {
            writeNodes();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        stringMap = createStringMap();

        try {
            writeStreets();
            writeWays();
            writeAreas();
            writeBuildings();
            writeLabels();
            writePOIs();
            writeStrings();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        stringMap = null;
        nodeMap = null;
    }

    private LinkedHashMap<Node, Integer> createNodeMap() {
        final LinkedHashMap<Node, Integer> nodeMap = new LinkedHashMap<>();

        int id = -1;
        id = putNodes(nodeMap, streets.elements, id);
        id = putNodes(nodeMap, ways.elements, id);
        id = putNodes(nodeMap, buildings.elements, id);
        putNodes(nodeMap, areas.elements, id);

        return nodeMap;
    }

    private int putNodes(final LinkedHashMap<Node, Integer> nodeMap, final MultiElement[] elements, int nodeCount) {
        for (final MultiElement element : elements) {
            for (final Node node : element) {
                if (!nodeMap.containsKey(node)) {
                    nodeMap.put(node, ++nodeCount);
                }
            }
        }

        return nodeCount;
    }

    private LinkedHashMap<String, Integer> createStringMap() {
        final LinkedHashMap<String, Integer> stringMap = new LinkedHashMap<>();
        stringMap.put(null, -1);

        int id = -1;

        // TODO there are some strange street names atm...
        for (final Building building : buildings.elements) {
            final String street = building.getStreet();
            final String number = building.getHouseNumber();
            final String name = building.getName();

            if (!stringMap.containsKey(street)) {
                stringMap.put(street, ++id);
            }
            if (!stringMap.containsKey(number)) {
                stringMap.put(number, ++id);
            }
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }
        for (final Street street : streets.elements) {
            final String name = street.getName();
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }
        for (final Label label : labels.elements) {
            final String name = label.getName();
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }

        return stringMap;
    }

    private void writeNodes() throws IOException {
        putNextEntry("nodes");
        dataOutput.writeInt(nodeMap.size());

        for (final Entry<Node, Integer> entry : nodeMap.entrySet()) {
            writePoint(entry.getKey());
        }

        closeEntry();
    }

    private void writeStrings() throws IOException {
        putNextEntry("strings");

        // dont store the (null,-1) entry...
        stringMap.remove(null);
        dataOutput.writeInt(stringMap.size());

        for (final Entry<String, Integer> entry : stringMap.entrySet()) {
            dataOutput.writeUTF(entry.getKey());
        }

        closeEntry();
    }

    private void writeStreets() throws IOException {
        putNextEntry("streets");

        final int[] addresses = new int[streets.elements.length];

        int address = 0;
        int index = -1;
        for (final Street street : streets.elements) {
            addresses[++index] = address;

            dataOutput.writeInt(street.getID());
            ++address;

            dataOutput.writeInt(stringMap.get(street.getName()));
            ++address;

            address += writeMultiElement(street);
        }

        closeEntry();

        writeAddresses("street", addresses);
        writeDistribution("street", streets.distribution);
        streets = null;
    }

    private void writeWays() throws IOException {
        putNextEntry("ways");

        final int[] addresses = new int[ways.elements.length];

        int address = 0;
        int index = -1;
        for (final Way way : ways.elements) {
            addresses[++index] = address;

            address += writeMultiElement(way);
        }

        closeEntry();

        writeAddresses("way", addresses);
        writeDistribution("way", ways.distribution);
        ways = null;
    }

    private void writeBuildings() throws IOException {
        putNextEntry("buildings");

        final int[] addresses = new int[buildings.elements.length];

        int address = 0;
        int index = -1;
        for (final Building building : buildings.elements) {
            addresses[++index] = address;

            // TODO improve this -> not store null explicit with -1.
            dataOutput.writeInt(stringMap.get(building.getStreet()));
            ++address;

            dataOutput.writeInt(stringMap.get(building.getHouseNumber()));
            ++address;

            dataOutput.writeInt(stringMap.get(building.getName()));
            ++address;

            address += writeMultiElement(building);
        }

        closeEntry();

        writeAddresses("building", addresses);
        writeDistribution("building", buildings.distribution);
        buildings = null;
    }

    private void writeAreas() throws IOException {
        putNextEntry("areas");

        final int[] addresses = new int[areas.elements.length];

        int address = 0;
        int index = -1;
        for (final Area area : areas.elements) {
            addresses[++index] = address;

            address += writeMultiElement(area);
        }

        closeEntry();

        writeAddresses("area", addresses);
        writeDistribution("area", areas.distribution);
        areas = null;
    }

    private void writePOIs() throws IOException {
        putNextEntry("poi");

        for (final POI poi : pois.elements) {
            writePoint(poi);
        }

        closeEntry();

        writeDistribution("poi", pois.distribution);
        pois = null;
    }

    private void writeLabels() throws IOException {
        putNextEntry("labels");

        // final int[] addresses = new int[labels.elements.length];
        //
        // int address = 0;
        // int index = -1;
        for (final Label label : labels.elements) {
            // addresses[++index] = address;

            writePoint(label);
            // address += 2;

            dataOutput.writeInt(stringMap.get(label.getName()));
            // ++address;
        }

        closeEntry();

        // TODO not necessary while labels got same memory size atm..
        // writeAddresses("label", addresses);
        writeDistribution("label", labels.distribution);
        labels = null;
    }

    private void writePoint(final Node location) throws IOException {
        dataOutput.writeInt(location.getX());
        dataOutput.writeInt(location.getY());
    }

    private int writeMultiElement(final MultiElement element) throws IOException {
        dataOutput.writeInt(element.size());
        int ret = 1;

        for (final Node node : element) {
            dataOutput.writeInt(nodeMap.get(node));
            ++ret;
        }

        return ret;
    }

    private void writeAddresses(final String name, final int[] addresses) throws IOException {
        putNextEntry(name + "Addresses");

        for (final int address : addresses) {
            dataOutput.writeInt(address);
        }

        closeEntry();
    }

    private void writeDistribution(final String name, final int[] distribution) throws IOException {
        putNextEntry(name + "Distribution");

        int total = 0;

        for (int type = 0; type < distribution.length; type++) {
            total += distribution[type];
            dataOutput.writeInt(total);
        }

        closeEntry();

    }
}