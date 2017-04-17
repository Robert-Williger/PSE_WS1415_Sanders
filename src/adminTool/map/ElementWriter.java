package adminTool.map;

import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import adminTool.elements.Street;
import adminTool.elements.Way;

//TODO compress
public class ElementWriter extends CompressedWriter {
    private LinkedHashMap<Node, Integer> nodeMap;
    private LinkedHashMap<String, Integer> stringMap;

    private Sorting<Area> areas;
    private Sorting<Street> streets;
    private Sorting<Way> ways;
    private Sorting<Building> buildings;
    private Sorting<Label> labels;

    int[] areaAddresses;
    int[] streetAddresses;
    int[] wayAddresses;
    int[] buildingAddresses;
    int[] labelAddresses;

    private DataOutput headerOutput;
    private DataOutput nodeOutput;
    private DataOutput stringOutput;
    private DataOutput areaOutput;
    private DataOutput streetOutput;
    private DataOutput wayOutput;
    private DataOutput buildingOutput;
    private DataOutput labelOutput;

    public ElementWriter(

            final Sorting<Area> areas, final Sorting<Street> streets, final Sorting<Way> ways,
            final Sorting<Building> buildings, final Sorting<Label> labels,

            final DataOutput elementHeaderOutput, final DataOutput nodeOutput, final DataOutput stringOutput,
            final DataOutput areaOutput, final DataOutput streetOutput, final DataOutput wayOutput,
            final DataOutput buildingOutput, final DataOutput labelOutput

    ) {
        this.areas = areas;
        this.streets = streets;
        this.ways = ways;
        this.buildings = buildings;
        this.labels = labels;

        this.nodeOutput = nodeOutput;
        this.stringOutput = stringOutput;
        this.areaOutput = areaOutput;
        this.streetOutput = streetOutput;
        this.wayOutput = wayOutput;
        this.headerOutput = elementHeaderOutput;
        this.buildingOutput = buildingOutput;
        this.labelOutput = labelOutput;

        areaAddresses = new int[areas.elements.length];
        streetAddresses = new int[streets.elements.length];
        wayAddresses = new int[ways.elements.length];
        buildingAddresses = new int[buildings.elements.length];
        labelAddresses = new int[labels.elements.length];
    }

    public void write() {
        createNodeMap();
        try {
            writeNodes();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        createNameAndNumberMap();
        try {
            writeStrings();
            writeStreets();
            writeWays();
            writeAreas();
            writeBuildings();
            writeLabels();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        stringMap = null;
        nodeMap = null;

    }

    private void createNodeMap() {
        nodeMap = new LinkedHashMap<>();

        int id = -1;
        id = putNodes(streets.elements, id);
        id = putNodes(ways.elements, id);
        id = putNodes(buildings.elements, id);
        putNodes(areas.elements, id);
    }

    private void createNameAndNumberMap() {
        stringMap = new LinkedHashMap<>();
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
    }

    private void writeNodes() throws IOException {
        nodeOutput.writeInt(nodeMap.size());

        for (final Entry<Node, Integer> entry : nodeMap.entrySet()) {
            writePoint(entry.getKey(), nodeOutput);
        }
    }

    private void writeStrings() throws IOException {
        // dont store the (null,-1) entry...
        stringOutput.writeInt(stringMap.size() - 1);
        for (final Entry<String, Integer> entry : stringMap.entrySet()) {
            // TODO improve this
            final String name = entry.getKey();
            if (name != null) {
                if (entry.getKey().isEmpty()) {
                    stringOutput.writeUTF("Unbekannte Straße");
                } else {
                    stringOutput.writeUTF(entry.getKey());
                }
            }
        }
    }

    private void writeStreets() throws IOException {
        int address = 0;
        int index = -1;
        for (final Street street : streets.elements) {
            streetAddresses[++index] = address;

            streetOutput.writeInt(street.getID());
            ++address;

            streetOutput.writeInt(stringMap.get(street.getName()));
            ++address;

            address += writeMultiElement(street, streetOutput);
        }

        writeDistribution(streets.distribution, streetAddresses);
    }

    private void writeWays() throws IOException {
        int address = 0;
        int index = -1;
        for (final Way way : ways.elements) {
            wayAddresses[++index] = address;

            address += writeMultiElement(way, wayOutput);
        }

        writeDistribution(ways.distribution, wayAddresses);
    }

    private void writeBuildings() throws IOException {
        int address = 0;
        int index = -1;
        for (final Building building : buildings.elements) {
            buildingAddresses[++index] = address;

            // TODO improve this -> not store null explicit with -1.
            buildingOutput.writeInt(stringMap.get(building.getStreet()));
            ++address;

            buildingOutput.writeInt(stringMap.get(building.getHouseNumber()));
            ++address;

            buildingOutput.writeInt(stringMap.get(building.getName()));
            ++address;

            address += writeMultiElement(building, buildingOutput);
        }

        writeDistribution(buildings.distribution, buildingAddresses);
    }

    private void writeLabels() throws IOException {
        int address = 0;
        int index = -1;
        for (final Label label : labels.elements) {
            labelAddresses[++index] = address;

            writePoint(label, labelOutput);
            address += 2;

            labelOutput.writeInt(stringMap.get(label.getName()));
            ++address;
        }

        writeDistribution(labels.distribution, labelAddresses);
    }

    private void writeAreas() throws IOException {
        int addresses = 0;
        int index = -1;
        for (final Area area : areas.elements) {
            areaAddresses[++index] = addresses;

            addresses += writeMultiElement(area, areaOutput);
        }

        writeDistribution(areas.distribution, areaAddresses);
    }

    private int putNodes(final MultiElement[] elements, int nodeCount) {
        for (final MultiElement element : elements) {
            for (final Node node : element) {
                if (!nodeMap.containsKey(node)) {
                    nodeMap.put(node, ++nodeCount);
                }
            }
        }

        return nodeCount;
    }

    private void writePoint(final Node location, final DataOutput output) throws IOException {
        output.writeInt(location.getX());
        output.writeInt(location.getY());
    }

    private int writeMultiElement(final MultiElement element, final DataOutput output) throws IOException {
        output.writeInt(element.size());
        int ret = 1;

        for (final Node node : element) {
            output.writeInt(nodeMap.get(node));
            ++ret;
        }

        return ret;
    }

    private void writeDistribution(final int[] distribution, final int[] addresses) throws IOException {
        int total = 0;

        headerOutput.writeInt(distribution.length);
        for (int type = 0; type < distribution.length - 1; type++) {
            total += distribution[type];
            // TODO -/+ 1?
            headerOutput.writeInt(addresses[total]);
        }
        // TODO
        headerOutput.writeInt(addresses[addresses.length - 1] + 1);
    }
}