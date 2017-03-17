package adminTool.map;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import adminTool.elements.Street;
import adminTool.elements.Way;

//TODO compress
public class ElementWriter extends CompressedWriter {
    private LinkedHashMap<Node, Integer> nodeMap;
    private LinkedHashMap<String, Integer> stringMap;
    private HashMap<Street, Integer> streetMap;

    private Sorting<Area> areas;
    private Sorting<Street> streets;
    private Sorting<Way> ways;
    private Sorting<Building> buildings;

    int[] areaAddresses;
    int[] streetAddresses;
    int[] wayAddresses;
    int[] buildingAddresses;

    private DataOutput headerOutput;
    private DataOutput nodeOutput;
    private DataOutput stringOutput;
    private DataOutput areaOutput;
    private DataOutput streetOutput;
    private DataOutput wayOutput;
    private DataOutput buildingOutput;

    public ElementWriter(

    final Sorting<Area> areas, final Sorting<Street> streets, final Sorting<Way> ways,
            final Sorting<Building> buildings,

            final DataOutput elementHeaderOutput, final DataOutput nodeOutput, final DataOutput stringOutput,
            final DataOutput areaOutput, final DataOutput streetOutput, final DataOutput wayOutput,
            final DataOutput buildingOutput

    ) {
        this.areas = areas;
        this.streets = streets;
        this.ways = ways;
        this.buildings = buildings;

        this.nodeOutput = nodeOutput;
        this.stringOutput = stringOutput;
        this.areaOutput = areaOutput;
        this.streetOutput = streetOutput;
        this.wayOutput = wayOutput;
        this.headerOutput = elementHeaderOutput;
        this.buildingOutput = buildingOutput;

        areaAddresses = new int[areas.elements.length];
        streetAddresses = new int[streets.elements.length];
        wayAddresses = new int[ways.elements.length];
        buildingAddresses = new int[buildings.elements.length];
    }

    public void write() {
        createNodeMap();
        try {
            writeNodes();

            createNameAndNumberMap();

            writeStrings();

            writeStreets();

            writeWays();

            writeAreas();

            createStreetMap();

            writeBuildings();

            streetMap = null;
            stringMap = null;
            nodeMap = null;
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    private void createNodeMap() {
        nodeMap = new LinkedHashMap<>();

        int id = -1;
        id = putNodes(streets.elements, id);
        id = putNodes(ways.elements, id);
        id = putNodes(buildings.elements, id);
        putNodes(areas.elements, id);
    }

    private void createStreetMap() {
        streetMap = new HashMap<>();
        int id = -1;
        for (final Street street : streets.elements) {
            streetMap.put(street, ++id);
        }
    }

    private void createNameAndNumberMap() {
        stringMap = new LinkedHashMap<>();

        int id = -1;
        stringMap.put("", ++id);

        // TODO there are some strange street names atm...
        for (final Building building : buildings.elements) {
            final String name = building.getStreet();
            final String number = building.getHouseNumber();

            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
            if (!stringMap.containsKey(number)) {
                stringMap.put(number, ++id);
            }
        }
        for (final Street street : streets.elements) {
            final String name = street.getName().trim();
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }
        for (final Way street : ways.elements) {
            final String name = street.getName().trim();
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
        stringOutput.writeInt(stringMap.size());
        for (final Entry<String, Integer> entry : stringMap.entrySet()) {
            stringOutput.writeUTF(entry.getKey());
        }
    }

    private void writeStreets() throws IOException {
        int address = 0;
        int index = -1;
        for (final Street street : streets.elements) {
            streetAddresses[++index] = address;

            // streetOutput.writeInt(street.getID());
            // ++address;

            address += writeWay(street, streetOutput);
        }

        writeDistribution(streets.distribution, streetAddresses);
    }

    private void writeWays() throws IOException {
        int address = 0;
        int index = -1;
        for (final Way way : ways.elements) {
            wayAddresses[++index] = address;

            address += writeWay(way, wayOutput);
        }

        writeDistribution(ways.distribution, wayAddresses);
    }

    private void writeBuildings() throws IOException {
        int address = 0;
        int index = -1;
        for (final Building building : buildings.elements) {
            buildingAddresses[++index] = address;

            address += writeMultiElement(building, buildingOutput);
        }

        writeDistribution(buildings.distribution, buildingAddresses);
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

    private int writeWay(final Way way, final DataOutput output) throws IOException {
        int ret = writeMultiElement(way, output);
        output.writeInt(stringMap.get(way.getName().trim()));

        return ret + 1;
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