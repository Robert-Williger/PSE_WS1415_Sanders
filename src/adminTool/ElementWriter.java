package adminTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.PointLabel;
import adminTool.elements.Street;
import adminTool.metrics.IDistanceMap;
import adminTool.metrics.PixelToCoordDistanceMap;
import adminTool.util.ElementAdapter;
import util.IntList;
import adminTool.elements.LineLabel;

public class ElementWriter extends AbstractMapFileWriter {
    private LinkedHashMap<String, Integer> stringMap;

    private Sorting<MultiElement> areas;
    private Sorting<Street> streets;
    private Sorting<Building> buildings;
    private Sorting<LineLabel> lineLabels;
    private Sorting<POI> pois;
    private Sorting<PointLabel> pointLabels;

    private IPointAccess points;
    private IntConversion conversion;

    public ElementWriter(final Sorting<MultiElement> areas, final Sorting<Street> streets,
            final Sorting<Building> buildings, final Sorting<LineLabel> lineLabels, final Sorting<POI> pois,
            final Sorting<PointLabel> pointLabels, final IPointAccess pointAccess, final IntConversion conversion,
            final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.areas = areas;
        this.streets = streets;
        this.buildings = buildings;
        this.lineLabels = lineLabels;
        this.pois = pois;
        this.pointLabels = pointLabels;
        this.points = pointAccess;
        this.conversion = conversion;
    }

    public void write() {
        stringMap = createStringMap();

        try {
            writeElementHeaders(new String[] { "street", "building", "area", "lineLabel" },
                    new String[] { "poi", "pointLabel" });
            writeStreets();
            writeAreas();
            writeBuildings();
            writeLineLabels();
            writePOIs();
            writePointLabels();
            writeStrings();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        stringMap = null;
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
        for (final LineLabel label : lineLabels.elements) {
            final String name = label.getName();
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }
        for (final PointLabel label : pointLabels.elements) {
            final String name = label.getName();
            if (!stringMap.containsKey(name)) {
                stringMap.put(name, ++id);
            }
        }

        return stringMap;
    }

    private void writeElementHeaders(final String[] collectiveElements, final String[] pointElements)
            throws IOException {
        putNextEntry("pointElements");

        dataOutput.writeInt(pointElements.length);
        for (final String element : pointElements)
            dataOutput.writeUTF(element);
        closeEntry();

        putNextEntry("collectiveElements");

        dataOutput.writeInt(collectiveElements.length);
        for (final String element : collectiveElements)
            dataOutput.writeUTF(element);
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
        putNextEntry("streetData");

        final int[] addresses = new int[streets.elements.length];

        int address = 0;
        int index = -1;
        for (final Street street : streets.elements) {
            addresses[++index] = address;

            dataOutput.writeInt(street.getId());
            ++address;

            dataOutput.writeInt(stringMap.get(street.getName()));
            ++address;

            address += writeMultiElements(street, false);
        }

        closeEntry();

        writeAddresses("street", addresses);
        writeDistribution("street", streets.distribution);
        writeAttributes("street", new String[] { "id", "name" });

        streets = null;
    }

    private void writeBuildings() throws IOException {
        putNextEntry("buildingData");

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

            address += writeMultiElements(building, true);
        }

        closeEntry();

        writeAddresses("building", addresses);
        writeDistribution("building", buildings.distribution);
        writeAttributes("building", new String[] { "street", "number", "name" });

        buildings = null;
    }

    private void writeAreas() throws IOException {
        putNextEntry("areaData");

        final int[] addresses = new int[areas.elements.length];

        int address = 0;
        int index = -1;
        for (final MultiElement area : areas.elements) {
            addresses[++index] = address;

            address += writeMultiElements(area, true);
        }

        closeEntry();

        writeAddresses("area", addresses);
        writeDistribution("area", areas.distribution);
        writeAttributes("area", new String[] {});
        areas = null;
    }

    private void writeLineLabels() throws IOException {
        putNextEntry("lineLabelData");

        final int[] addresses = new int[lineLabels.elements.length];

        int address = 0;
        int index = -1;
        for (final LineLabel label : lineLabels.elements) {
            addresses[++index] = address;

            dataOutput.writeInt(stringMap.get(label.getName()));
            ++address;

            dataOutput.writeInt(label.getZoom());
            ++address;

            address += writeMultiElements(label, false);
        }

        closeEntry();

        writeAddresses("lineLabel", addresses);
        writeDistribution("lineLabel", lineLabels.distribution);
        writeAttributes("lineLabel", new String[] { "name", "zoom" });
        lineLabels = null;
    }

    private void writePOIs() throws IOException {
        putNextEntry("poiData");

        for (final POI poi : pois.elements) {
            writePoint(poi.getPoint());
        }

        closeEntry();

        writeDistribution("poi", pois.distribution);
        writeAttributes("poi", new String[] {});
        pois = null;
    }

    private void writePointLabels() throws IOException {
        putNextEntry("pointLabelData");

        for (final PointLabel label : pointLabels.elements) {
            dataOutput.writeInt(stringMap.get(label.getName()));
            writePoint(label.getPoint());
        }

        closeEntry();

        writeDistribution("pointLabel", pointLabels.distribution);
        writeAttributes("pointLabel", new String[] { "name" });
        pointLabels = null;
    }

    private void writePoint(final int point) throws IOException {
        dataOutput.writeInt(conversion.convert(points.getX(point)));
        dataOutput.writeInt(conversion.convert(points.getY(point)));
    }

    private int writeMultiElements(MultiElement element, boolean polygon) throws IOException {
        ArrayList<MultiElement> elements = new ArrayList<>();
        IntList zooms = new IntList();
        elements.add(element);
        zooms.add(Integer.MAX_VALUE);

        final ElementAdapter adapter = new ElementAdapter(points);
        adapter.setMultiElement(element);
        for (int zoom = 19; zoom >= 6; --zoom) {
            IDistanceMap map = new PixelToCoordDistanceMap(zoom);
            VisvalingamWyatt simplifier = new VisvalingamWyatt(map.map(1) * map.map(1));
            IntList indices = polygon ? simplifier.simplifyPolygon(adapter) : simplifier.simplifyMultiline(adapter);
            if (indices.size() > element.size() / 2)
                continue;

            for (int i = 0; i < indices.size(); ++i) {
                indices.set(i, element.getPoint(indices.get(i)));
            }
            element = new MultiElement(indices, element.getType());
            adapter.setMultiElement(element);

            elements.add(element);
            zooms.add(zoom);
        }

        int ret = 0;

        int address = 2 * elements.size();
        for (int i = elements.size() - 1; i >= 0; --i) {
            dataOutput.writeInt(zooms.get(i));
            dataOutput.writeInt(address);
            ret += 2;
            address += (2 * elements.get(i).size() + 1);
        }
        for (int i = elements.size() - 1; i >= 0; --i) {
            ret += writeMultiElement(elements.get(i));
        }

        return ret;
    }

    private int writeMultiElement(MultiElement element) throws IOException {
        dataOutput.writeInt(element.size());
        int ret = 1;

        for (int i = 0; i < element.size(); ++i) {
            final int node = element.getPoint(i);
            dataOutput.writeInt(conversion.convert(points.getX(node)));
            dataOutput.writeInt(conversion.convert(points.getY(node)));
            ret += 2;
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

    private void writeAttributes(final String name, final String[] attributes) throws IOException {
        putNextEntry(name + "Attributes");

        dataOutput.writeInt(attributes.length);
        for (final String attribute : attributes) {
            dataOutput.writeUTF(attribute);
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