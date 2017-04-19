package adminTool.map;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    private final ZipOutputStream zipOutput;

    int[] areaAddresses;
    int[] streetAddresses;
    int[] wayAddresses;
    int[] buildingAddresses;
    int[] labelAddresses;

    public ElementWriter(

            final Sorting<Area> areas, final Sorting<Street> streets, final Sorting<Way> ways,
            final Sorting<Building> buildings, final Sorting<Label> labels,

            final ZipOutputStream zipOutput

    ) {
        this.areas = areas;
        this.streets = streets;
        this.ways = ways;
        this.buildings = buildings;
        this.labels = labels;

        this.zipOutput = zipOutput;

        areaAddresses = new int[areas.elements.length];
        streetAddresses = new int[streets.elements.length];
        wayAddresses = new int[ways.elements.length];
        buildingAddresses = new int[buildings.elements.length];
        labelAddresses = new int[labels.elements.length];
    }

    public void write() {
        final DataOutputStream dataOutput = new DataOutputStream(zipOutput);

        createNodeMap();
        try {
            writeNodes(dataOutput);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        createNameAndNumberMap();
        try {
            writeStrings(dataOutput);
            writeStreets(dataOutput);
            writeWays(dataOutput);
            writeAreas(dataOutput);
            writeBuildings(dataOutput);
            writeLabels(dataOutput);
            writeDistributions(dataOutput);
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

    private void writeNodes(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("nodes"));
        dataOutput.writeInt(nodeMap.size());

        for (final Entry<Node, Integer> entry : nodeMap.entrySet()) {
            writePoint(entry.getKey(), dataOutput);
        }

        zipOutput.closeEntry();
    }

    private void writeStrings(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("strings"));
        // dont store the (null,-1) entry...
        dataOutput.writeInt(stringMap.size() - 1);
        for (final Entry<String, Integer> entry : stringMap.entrySet()) {
            // TODO improve this
            final String name = entry.getKey();
            if (name != null) {
                dataOutput.writeUTF(entry.getKey());
            }
        }

        zipOutput.closeEntry();
    }

    private void writeStreets(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("streets"));

        int address = 0;
        int index = -1;
        for (final Street street : streets.elements) {
            streetAddresses[++index] = address;

            dataOutput.writeInt(street.getID());
            ++address;

            dataOutput.writeInt(stringMap.get(street.getName()));
            ++address;

            address += writeMultiElement(street, dataOutput);
        }

        zipOutput.closeEntry();
    }

    private void writeWays(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("ways"));

        int address = 0;
        int index = -1;
        for (final Way way : ways.elements) {
            wayAddresses[++index] = address;

            address += writeMultiElement(way, dataOutput);
        }

        zipOutput.closeEntry();
    }

    private void writeBuildings(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("buildings"));

        int address = 0;
        int index = -1;
        for (final Building building : buildings.elements) {
            buildingAddresses[++index] = address;

            // TODO improve this -> not store null explicit with -1.
            dataOutput.writeInt(stringMap.get(building.getStreet()));
            ++address;

            dataOutput.writeInt(stringMap.get(building.getHouseNumber()));
            ++address;

            dataOutput.writeInt(stringMap.get(building.getName()));
            ++address;

            address += writeMultiElement(building, dataOutput);
        }

        zipOutput.closeEntry();
    }

    private void writeLabels(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("labels"));

        int address = 0;
        int index = -1;
        for (final Label label : labels.elements) {
            labelAddresses[++index] = address;

            writePoint(label, dataOutput);
            address += 2;

            dataOutput.writeInt(stringMap.get(label.getName()));
            ++address;
        }

        zipOutput.closeEntry();
    }

    private void writeAreas(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("areas"));

        int addresses = 0;
        int index = -1;
        for (final Area area : areas.elements) {
            areaAddresses[++index] = addresses;

            addresses += writeMultiElement(area, dataOutput);
        }

        zipOutput.closeEntry();
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

    private void writeDistributions(final DataOutputStream dataOutput) throws IOException {
        zipOutput.putNextEntry(new ZipEntry("distributions"));

        writeDistribution(dataOutput, streets.distribution, streetAddresses);
        writeDistribution(dataOutput, ways.distribution, wayAddresses);
        writeDistribution(dataOutput, areas.distribution, areaAddresses);
        writeDistribution(dataOutput, buildings.distribution, buildingAddresses);
        writeDistribution(dataOutput, labels.distribution, labelAddresses);

        zipOutput.closeEntry();
    }

    private void writeDistribution(final DataOutput output, final int[] distribution, final int[] addresses)
            throws IOException {
        int total = 0;

        output.writeInt(distribution.length);
        for (int type = 0; type < distribution.length - 1; type++) {
            total += distribution[type];
            // TODO -/+ 1?
            output.writeInt(addresses[total]);
        }
        // TODO
        output.writeInt(addresses[addresses.length - 1] + 1);
    }
}