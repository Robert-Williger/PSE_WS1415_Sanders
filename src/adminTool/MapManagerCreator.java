package adminTool;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import adminTool.configurations.IElementOrder;
import adminTool.configurations.PerformantConfiguration;
import model.elements.Area;
import model.elements.Building;
import model.elements.MultiElement;
import model.elements.Node;
import model.elements.POI;
import model.elements.Street;
import model.elements.StreetNode;
import model.elements.Typeable;
import model.elements.Way;
import model.map.PixelConverter;

public class MapManagerCreator extends AbstractMapCreator {

    private static final int MAX_ZOOM_STEPS = 11;

    // minimum amount of tiles in a row / column on lowest zoom level
    private static final int MIN_TILES = 4;
    private static final int TILE_LENGTH = 256;
    private static final int POI_WIDTH = 20;
    private static final int WAY_WIDTH = 30;
    private static final int MIN_POI_DISTANCE = 40;
    private static final int MAX_BUILDING_STREET_DISTANCE = 4000;
    private static final ReferencedTile EMPTY_TILE = new ReferencedTile();

    private final IElementOrder config;
    private Collection<Building> buildings;
    private Collection<ReferencedPOI> referencedPOIs;
    private File file;
    private DataOutputStream stream;

    private Rectangle boundingBox;

    private int minZoomStep;
    private int maxZoomStep;
    private int coordTileLength;

    private StreetNodeFinder finder;
    private TypeSorter<POI> poiSorter;
    private TypeSorter<Way> waySorter;
    private TypeSorter<Street> streetSorter;
    private TypeSorter<Area> terrainSorter;

    private PixelConverter converter;
    private ReferencedTile[][] tiles;

    public MapManagerCreator(final Collection<Building> buildings, final Collection<Street> streets,
            final Collection<POI> pois, final Collection<Way> ways, final Collection<Area> terrain,
            final Rectangle boundingBox, final File file) {

        config = new PerformantConfiguration();
        this.buildings = buildings;
        this.boundingBox = boundingBox;
        this.file = file;

        poiSorter = new TypeSorter<POI>(pois, config.getPOIOrder());
        waySorter = new TypeSorter<Way>(ways, config.getWayOrder());
        streetSorter = new TypeSorter<Street>(streets, config.getStreetOrder());
        terrainSorter = new TypeSorter<Area>(terrain, config.getTerrainOrder());
    }

    @Override
    public void create() {
        final int zoomOffset = (int) Math.ceil(log2(Math.min(boundingBox.getWidth(), boundingBox.getHeight())
                / MIN_TILES));
        final int topTileLength = (1 << zoomOffset);
        minZoomStep = 29 - zoomOffset;
        final int conversionFactor = 1 << 21;

        maxZoomStep = Math.min(19, minZoomStep + MAX_ZOOM_STEPS - 1);
        coordTileLength = topTileLength >> (maxZoomStep - minZoomStep);

        final int xTiles = (int) Math.ceil(boundingBox.getWidth() / coordTileLength);
        final int yTiles = (int) Math.ceil(boundingBox.getHeight() / coordTileLength);

        boundingBox = null;
        converter = new PixelConverter(conversionFactor);

        try {
            stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        file = null;

        tiles = new ReferencedTile[yTiles][xTiles];

        for (int i = 0; i < yTiles; i++) {
            for (int j = 0; j < xTiles; j++) {
                tiles[i][j] = new ReferencedTile();
            }
        }

        finder = new StreetNodeFinder();
        finder.start();

        poiSorter.start();
        waySorter.start();
        streetSorter.start();
        terrainSorter.start();

        ElementWriter elementWriter = new ElementWriter();
        elementWriter.start();

        BuildingPartitioner buildingPartitioner = new BuildingPartitioner();
        buildingPartitioner.start();

        WayPartitioner wayPartitioner = new WayPartitioner();
        wayPartitioner.start();

        StreetPartitioner streetPartitioner = new StreetPartitioner();
        streetPartitioner.start();

        TerrainPartitioner terrainPartitioner = new TerrainPartitioner();
        terrainPartitioner.start();

        POIPartitioner poiPartitioner = new POIPartitioner();
        poiPartitioner.start();

        try {
            buildingPartitioner.join();
            wayPartitioner.join();
            streetPartitioner.join();
            terrainPartitioner.join();
            poiPartitioner.join();

            elementWriter.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        // unterste Zoomstufe fertig

        elementWriter = null;
        finder = null;

        buildingPartitioner = null;
        wayPartitioner = null;
        streetPartitioner = null;
        terrainPartitioner = null;
        poiPartitioner = null;

        poiSorter = null;
        waySorter = null;
        streetSorter = null;
        terrainSorter = null;

        buildings = null;

        for (int zoom = maxZoomStep - 1; zoom >= minZoomStep; zoom--) {
            final TileWriter writer = new TileWriter();
            writer.start();

            final ReferencedTile[][] next = mergeUp(zoom);

            try {
                writer.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            tiles = next;
        }

        final TileWriter writer = new TileWriter();
        writer.run();

        try {
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private ReferencedTile[][] mergeUp(final int zoom) {
        final ReferencedTile[][] array = new ReferencedTile[(tiles.length + 1) / 2][(tiles[0].length + 1) / 2];

        for (int i = 0; i < tiles.length; i += 2) {
            for (int j = 0; j < tiles[0].length; j += 2) {
                final ReferencedTile[] mergeTiles = new ReferencedTile[4];

                mergeTiles[0] = getTile(i, j);
                mergeTiles[1] = getTile(i + 1, j);
                mergeTiles[2] = getTile(i, j + 1);
                mergeTiles[3] = getTile(i + 1, j + 1);

                array[i / 2][j / 2] = new ReferencedTile(config, mergeTiles);
            }
        }

        partitionPOIs(zoom, array);

        return array;
    }

    private void partitionPOIs(final int zoom, final ReferencedTile[][] tiles) {
        final int poiWidth = converter.getCoordDistance(POI_WIDTH, zoom);

        for (final Iterator<ReferencedPOI> it = referencedPOIs.iterator(); it.hasNext();) {
            final ReferencedPOI poi = it.next();
            final Rectangle poiBounds = new Rectangle(poi.getX() - poiWidth / 2, poi.getY() - poiWidth / 2, poiWidth,
                    poiWidth);
            final Point poiLocation = poiBounds.getLocation();
            final Rectangle poiTileBounds = locateRectangle(poiBounds, zoom);

            boolean intersection = false;
            for (int row = poiTileBounds.y; row <= poiTileBounds.height + poiTileBounds.y; row++) {
                for (int column = poiTileBounds.x; column <= poiTileBounds.width + poiTileBounds.x; column++) {
                    final ReferencedTile tile = getTile(row, column, tiles);
                    for (final ReferencedPOI other : tile.getPOIs()) {
                        final Point otherPoint = new Point(other.getX() - poiWidth / 2, other.getY() - poiWidth / 2);
                        final int distance = (int) poiLocation.distance(otherPoint);
                        if (converter.getPixelDistance(distance, zoom) < MIN_POI_DISTANCE) {
                            intersection = true;
                            break;
                        }
                    }
                }
            }
            if (!intersection) {
                for (int row = poiTileBounds.y; row <= poiTileBounds.height + poiTileBounds.y; row++) {
                    for (int column = poiTileBounds.x; column <= poiTileBounds.width + poiTileBounds.x; column++) {
                        getTile(row, column, tiles).getPOIs().add(poi);
                    }
                }
            } else {
                it.remove();
            }
        }
    }

    private ReferencedTile getTile(final int row, final int column) {
        return getTile(row, column, tiles);
    }

    private ReferencedTile getTile(final int row, final int column, final ReferencedTile[][] tiles) {
        if (row >= 0 && row < tiles.length) {
            if (column >= 0 && column < tiles[row].length) {
                return tiles[row][column];
            }
        }

        return EMPTY_TILE;
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private Path2D.Float createPath(final List<Node> nodes) {
        final Path2D.Float path = new Path2D.Float();
        final Iterator<Node> iter = nodes.iterator();

        Point location = iter.next().getLocation();
        path.moveTo(location.x, location.y);

        while (iter.hasNext()) {
            location = iter.next().getLocation();
            path.lineTo(location.x, location.y);
        }

        return path;
    }

    private Rectangle locateRectangle(final Rectangle rect) {
        return locateRectangle(rect, maxZoomStep);
    }

    private Rectangle locateRectangle(final Rectangle rect, final int zoom) {
        final Rectangle ret = new Rectangle();
        ret.setLocation(locatePoint(rect.getLocation(), zoom));
        final Point secondPoint = locatePoint((int) rect.getMaxX(), (int) rect.getMaxY(), zoom);
        ret.setSize(secondPoint.x - ret.x, secondPoint.y - ret.y);

        return ret;
    }

    private Point locatePoint(final int x, final int y, final int zoom) {
        final int coordLength = coordTileLength << (maxZoomStep - zoom);
        return new Point(x / coordLength, y / coordLength);
    }

    private Point locatePoint(final Point point, final int zoom) {
        return locatePoint(point.x, point.y, zoom);
    }

    private String getStreetName(final String address) {
        return address.substring(0, address.lastIndexOf(' ')).trim();
    }

    private String getHouseNumber(final String address) {
        return address.substring(address.lastIndexOf(' '), address.length()).trim();
    }

    private Point2D.Float calculateCenter(final Polygon poly) {
        float x = 0f;
        float y = 0f;
        int totalPoints = poly.npoints - 1;
        for (int i = 0; i < totalPoints; i++) {
            x += poly.xpoints[i];
            y += poly.ypoints[i];
        }

        if (poly.xpoints[0] != poly.xpoints[totalPoints] || poly.ypoints[0] != poly.ypoints[totalPoints]) {
            x += poly.xpoints[totalPoints];
            y += poly.ypoints[totalPoints];
            totalPoints++;
        }

        x = x / totalPoints;
        y = y / totalPoints;

        return new Point.Float(x, y);
    }

    private class POIPartitioner extends Thread {

        public POIPartitioner() {
            super("POI partitioner");
        }

        @Override
        public void run() {
            try {
                poiSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            referencedPOIs = new LinkedList<ReferencedPOI>();
            for (final POI poi : poiSorter.getSorting()) {
                referencedPOIs.add(new ReferencedPOI(poi.getX(), poi.getY()));
            }
            partitionPOIs(maxZoomStep, tiles);
        }
    }

    private class TerrainPartitioner extends Thread {

        public TerrainPartitioner() {
            super("Terrain partitioner");
        }

        @Override
        public void run() {
            try {
                terrainSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            int id = 0;
            for (final Area area : terrainSorter.getSorting()) {
                final Polygon poly = area.getPolygon();
                final Rectangle areaTileBounds = locateRectangle(poly.getBounds());
                final Rectangle tileRect = new Rectangle(coordTileLength, coordTileLength);

                for (int i = areaTileBounds.y; i <= areaTileBounds.height + areaTileBounds.y; i++) {
                    for (int j = areaTileBounds.x; j <= areaTileBounds.width + areaTileBounds.x; j++) {
                        tileRect.setLocation(j * coordTileLength, i * coordTileLength);
                        if (poly.intersects(tileRect)) {
                            getTile(i, j).getTerrain().add(id);
                        }
                    }
                }

                ++id;
            }
        }
    }

    private class WayPartitioner extends Thread {

        public WayPartitioner() {
            super("Way partitioner");
        }

        @Override
        public void run() {
            try {
                waySorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            final int maxWayWidth = converter.getCoordDistance(WAY_WIDTH, maxZoomStep);

            int id = 0;
            for (final Way way : waySorter.getSorting()) {
                final Path2D.Float path = createPath(way.getNodes());
                final Stroke stroke = new BasicStroke(maxWayWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

                final Shape wayShape = stroke.createStrokedShape(path);

                final Rectangle wayTileBounds = locateRectangle(wayShape.getBounds());
                final Rectangle tileRect = new Rectangle(coordTileLength, coordTileLength);

                for (int i = wayTileBounds.y; i <= wayTileBounds.height + wayTileBounds.y; i++) {
                    for (int j = wayTileBounds.x; j <= wayTileBounds.width + wayTileBounds.x; j++) {
                        tileRect.setLocation(j * coordTileLength, i * coordTileLength);
                        if (wayShape.intersects(tileRect)) {
                            getTile(i, j).getWays().add(id);
                        }
                    }
                }

                ++id;
            }
        }

    }

    private class StreetPartitioner extends Thread {

        public StreetPartitioner() {
            super("Street partitioner");
        }

        @Override
        public void run() {
            try {
                streetSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            final int maxWayWidth = converter.getCoordDistance(WAY_WIDTH, maxZoomStep);

            int id = 0;
            for (final Street street : streetSorter.getSorting()) {
                final Path2D.Float path = createPath(street.getNodes());
                final Stroke stroke = new BasicStroke(maxWayWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

                final Shape streetShape = stroke.createStrokedShape(path);

                final Rectangle streetTileBounds = locateRectangle(streetShape.getBounds());
                final Rectangle tileRect = new Rectangle(coordTileLength, coordTileLength);

                for (int i = streetTileBounds.y; i <= streetTileBounds.height + streetTileBounds.y; i++) {
                    for (int j = streetTileBounds.x; j <= streetTileBounds.width + streetTileBounds.x; j++) {
                        tileRect.setLocation(j * coordTileLength, i * coordTileLength);
                        if (streetShape.intersects(tileRect)) {
                            getTile(i, j).getStreets().add(id);
                        }
                    }
                }

                ++id;
            }
        }
    }

    private class BuildingPartitioner extends Thread {

        public BuildingPartitioner() {
            super("Building partitioner");
        }

        @Override
        public void run() {
            try {
                finder.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            partition(finder.getSuccess(), 0);
            partition(finder.getFailure(), finder.getSuccess().size());
        }

        private final void partition(final Collection<Building> buildings, int id) {
            for (final Building building : buildings) {
                final Polygon poly = building.getPolygon();
                final Rectangle buildingTileBounds = locateRectangle(poly.getBounds());
                final Rectangle tileRect = new Rectangle(coordTileLength, coordTileLength);

                for (int i = buildingTileBounds.y; i <= buildingTileBounds.height + buildingTileBounds.y; i++) {
                    for (int j = buildingTileBounds.x; j <= buildingTileBounds.width + buildingTileBounds.x; j++) {
                        tileRect.setLocation(j * coordTileLength, i * coordTileLength);
                        if (poly.intersects(tileRect)) {
                            getTile(i, j).getBuildings().add(id);
                        }
                    }
                }

                ++id;
            }
        }

    }

    private class StreetNodeFinder extends Thread {
        private HashMap<String, Collection<Street>> streetMap;
        private List<Building> buildings;

        private List<Building> success;
        private List<Building> failure;

        private final int threads = 4;

        public StreetNodeFinder() {
            super("Streetnode finder - Master");
        }

        public Collection<Building> getSuccess() {
            return success;
        }

        public Collection<Building> getFailure() {
            return failure;
        }

        @Override
        public void run() {
            // TODO List in parser --> no copy!
            createBuildingList();
            createStreetMap();

            final int buildingTotal = buildings.size();
            final int buildingsPerThread = buildingTotal / threads;
            final Worker[] workers = new Worker[threads];
            for (int i = 0; i < threads - 1; i++) {
                workers[i] = new Worker(buildingsPerThread * i, buildingsPerThread * (i + 1));
            }
            workers[threads - 1] = new Worker(buildingsPerThread * (threads - 1), buildingTotal);

            for (final Worker worker : workers) {
                try {
                    worker.join();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

            success = new ArrayList<Building>();
            failure = new ArrayList<Building>();

            for (final Building building : buildings) {
                (building.getStreetNode() != null ? success : failure).add(building);
            }

            buildings = null;
            streetMap = null;
        }

        private void createBuildingList() {
            buildings = new ArrayList<Building>(MapManagerCreator.this.buildings);
        }

        private void createStreetMap() {
            streetMap = new HashMap<String, Collection<Street>>();

            for (final Street street : streetSorter.getSorting()) {
                final String streetName = street.getName();
                if (!streetName.isEmpty()) {
                    Collection<Street> streetList = streetMap.get(street.getName());
                    if (streetList == null) {
                        streetList = new ArrayList<Street>();
                        streetMap.put(street.getName(), streetList);
                    }
                    streetList.add(street);
                }
            }
        }

        private class Worker extends Thread {

            private final int from;
            private final int to;

            public Worker(final int from, final int to) {
                super("Streetnode finder - Worker");
                this.from = from;
                this.to = to;
                start();
            }

            @Override
            public void run() {
                findStreetNodes(from, to);
            }

            private void findStreetNodes(final int from, final int to) {
                for (int i = from; i < to; i++) {
                    final Building building = buildings.get(i);

                    final Polygon poly = building.getPolygon();
                    final String address = getStreetName(building.getAddress());

                    final Collection<Street> streetList = streetMap.get(address);

                    if (streetList != null) {
                        building.setStreetNode(findStreetNode(streetList, calculateCenter(poly)));
                    }
                }
            }

            private StreetNode findStreetNode(final Collection<Street> streetList, final Point2D.Float center) {
                StreetNode ret = null;

                long minDist = Long.MAX_VALUE;

                for (final Street street : streetList) {
                    final List<Node> nodes = street.getNodes();
                    final Iterator<Node> iterator = nodes.iterator();
                    float totalLength = 0;
                    final int maxLength = street.getLength();

                    Point lastPoint = iterator.next().getLocation();
                    while (iterator.hasNext()) {
                        final Point currentPoint = iterator.next().getLocation();

                        final long dx = currentPoint.x - lastPoint.x;
                        final long dy = currentPoint.y - lastPoint.y;
                        final long square = (dx * dx + dy * dy);
                        final float length = (float) Math.sqrt(square);
                        double s = ((center.x - lastPoint.x) * dx + (center.y - lastPoint.y) * dy) / (double) square;

                        if (s < 0) {
                            s = 0;
                        } else if (s > 1) {
                            s = 1;
                        }

                        final double distX = lastPoint.x + s * dx - center.x;
                        final double distY = lastPoint.y + s * dy - center.y;

                        final long distance = (long) Math.sqrt(distX * distX + distY * distY);

                        if (distance < minDist) {
                            ret = new StreetNode((float) ((totalLength + s * length) / maxLength), street);
                            minDist = distance;
                        }

                        totalLength += length;
                        lastPoint = currentPoint;
                    }
                }

                return minDist <= MAX_BUILDING_STREET_DISTANCE ? ret : null;
            }
        }
    }

    private class ElementWriter extends Thread {
        private LinkedHashMap<Node, Integer> nodeMap;
        private LinkedHashMap<String, Integer> nameMap;
        private LinkedHashMap<String, Integer> numberMap;
        private HashMap<Street, Integer> streetMap;

        public ElementWriter() {
            super("Element writer");
        }

        @Override
        public void run() {
            try {
                writeHeader();

                createNodeMap();

                writeNodes();

                createNameAndNumberMap();

                writeNames();
                writeNumbers();

                writeStreets();

                writeWays();
                writeTerrain();

                createStreetMap();

                writePOIs();

                writeBuildings();

                streetMap = null;
                numberMap = null;
                nameMap = null;
                nodeMap = null;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        private void createNodeMap() {
            nodeMap = new LinkedHashMap<Node, Integer>();

            int id = -1;
            id = putNodes(streetSorter.getSorting(), id);
            id = putNodes(waySorter.getSorting(), id);
            id = putNodes(buildings, id);
            putNodes(terrainSorter.getSorting(), id);
        }

        private void createStreetMap() {
            streetMap = new HashMap<Street, Integer>();
            int id = -1;
            for (final Street street : streetSorter.getSorting()) {
                streetMap.put(street, ++id);
            }
        }

        private void createNameAndNumberMap() {
            nameMap = new LinkedHashMap<String, Integer>();
            numberMap = new LinkedHashMap<String, Integer>();

            int id = -1;
            int numberID = -1;
            nameMap.put("", ++id);

            for (final Building building : buildings) {
                final String address = building.getAddress();
                final String name = getStreetName(address);
                final String number = getHouseNumber(address);

                if (!nameMap.containsKey(name)) {
                    nameMap.put(name, ++id);
                }
                if (!numberMap.containsKey(number)) {
                    numberMap.put(number, ++numberID);
                }
            }
            for (final Street street : streetSorter.getSorting()) {
                final String name = street.getName().trim();
                if (!nameMap.containsKey(name)) {
                    nameMap.put(name, ++id);
                }
            }
            for (final Way street : waySorter.getSorting()) {
                final String name = street.getName().trim();
                if (!nameMap.containsKey(name)) {
                    nameMap.put(name, ++id);
                }
            }
        }

        private void writeHeader() throws IOException {
            writeInt(minZoomStep);
            writeInt(maxZoomStep);
            writeInt(tiles.length);
            writeInt(tiles[0].length);
            stream.writeDouble(converter.getCoordDistance(1, 0));
            writeInt(TILE_LENGTH);
            writeInt(TILE_LENGTH);
        }

        private void writeNodes() throws IOException {
            writeInt(nodeMap.size());

            // TODO compress
            for (final Entry<Node, Integer> entry : nodeMap.entrySet()) {
                writePoint(entry.getKey().getLocation());
            }
        }

        private void writePOIs() throws IOException {
            try {
                poiSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            writeDistribution(poiSorter.getTypeDistribution(), config.getPOIOrder());

            // TODO compress
            for (final POI poi : poiSorter.getSorting()) {
                writePoint(poi.getLocation());
            }
        }

        private void writeNames() throws IOException {
            writeInt(nameMap.size());
            for (final Entry<String, Integer> entry : nameMap.entrySet()) {
                stream.writeUTF(entry.getKey());
            }
        }

        private void writeNumbers() throws IOException {
            writeInt(numberMap.size());
            for (final Entry<String, Integer> entry : numberMap.entrySet()) {
                stream.writeUTF(entry.getKey());
            }
        }

        private void writeStreets() throws IOException {
            try {
                streetSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(streetSorter.getTypeDistribution(), config.getStreetOrder());

            for (final Street street : streetSorter.getSorting()) {
                int node1 = (int) (street.getID() >> 32);
                int node2 = (int) (street.getID() & 0xFFFFFFFF);
                writeInt(node1);
                writeInt(node2 - node1);

                writeWay(street);
            }
        }

        private void writeWays() throws IOException {
            try {
                waySorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(waySorter.getTypeDistribution(), config.getWayOrder());
            for (final Way way : waySorter.getSorting()) {
                writeWay(way);
            }
        }

        private void writeBuildings() throws IOException {
            try {
                finder.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeInt(buildings.size());
            writeInt(finder.getSuccess().size());

            for (final Building building : finder.getSuccess()) {
                final StreetNode node = building.getStreetNode();

                writeMultiElement(building);
                assert streetMap.containsKey(node.getStreet()) : streetSorter.getSorting().contains(node.getStreet());
                writeInt(streetMap.get(node.getStreet()));
                writeInt(numberMap.get(getHouseNumber(building.getAddress()).trim()));
                stream.writeFloat(node.getOffset());
            }

            for (final Building building : finder.getFailure()) {
                if (building.getStreetNode() == null) {
                    writeMultiElement(building);
                }
            }
        }

        private void writeTerrain() throws IOException {
            try {
                terrainSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(terrainSorter.getTypeDistribution(), config.getTerrainOrder());
            for (final Area area : terrainSorter.getSorting()) {
                writeMultiElement(area);
            }
        }

        private int putNodes(final Collection<? extends MultiElement> elements, int nodeCount) {
            for (final MultiElement element : elements) {
                for (final Node node : element.getNodes()) {
                    if (!nodeMap.containsKey(node)) {
                        nodeMap.put(node, ++nodeCount);
                    }
                }
            }

            return nodeCount;
        }

        private void writePoint(final Point location) throws IOException {
            writeInt(location.x);
            writeInt(location.y);
        }

        private void writeMultiElement(final MultiElement element) throws IOException {
            final Collection<Node> nodes = element.getNodes();
            writeInt(nodes.size());

            int last = 0;
            for (final Node node : nodes) {
                int current = nodeMap.get(node);
                writeInt(current - last);
                last = current;
            }
        }

        private void writeWay(final Way way) throws IOException {
            writeMultiElement(way);
            writeInt(nameMap.get(way.getName().trim()));
        }

        private void writeDistribution(final int[] distribution, final int[] typeOrder) throws IOException {
            int total = 0;
            // TODO type order
            writeInt(distribution.length);
            for (int type = 0; type < distribution.length; type++) {
                total += distribution[type];
                writeInt(total);
            }
            for (int type = 0; type < distribution.length; type++) {
                writeInt(type);
            }
        }
    }

    private class TileWriter extends Thread {

        public TileWriter() {
            super("Tile writer");
        }

        @Override
        public void run() {
            try {
                writeTiles();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        private void writeTiles() throws IOException {
            for (final ReferencedTile[] tile2 : tiles) {
                for (int column = 0; column < tiles[0].length; column++) {
                    final ReferencedTile tile = tile2[column];

                    final byte flags = tile.getFlags();
                    stream.write(tile.getFlags());
                    if (flags != 0) {
                        if (!tile.getPOIs().isEmpty()) {
                            writeInt(tile.getPOIs().size());
                            int last = 0;
                            for (final ReferencedPOI poi : tile.getPOIs()) {
                                writeInt(poi.getID() - last);
                                last = poi.getID();
                            }
                        }
                        if (!tile.getStreets().isEmpty()) {
                            writeInts(tile.getStreets());
                        }
                        if (!tile.getWays().isEmpty()) {
                            writeInts(tile.getWays());
                        }
                        if (!tile.getBuildings().isEmpty()) {
                            writeInts(tile.getBuildings());
                        }
                        if (!tile.getTerrain().isEmpty()) {
                            writeInts(tile.getTerrain());
                        }
                    }
                }
            }
        }

        private void writeInts(final Collection<Integer> collection) throws IOException {
            writeInt(collection.size());
            int last = 0;
            for (final Integer building : collection) {
                writeInt(building - last);
                last = building;
            }
        }
    }

    private void writeInt(final int value) throws IOException {
        int temp = value >>> 28;

        if (temp == 0) {
            temp = (value >> 21) & 0x7F;
            if (temp == 0) {
                temp = (value >> 14) & 0x7F;
                if (temp == 0) {
                    temp = (value >> 7) & 0x7F;
                    if (temp != 0) {
                        stream.write(temp);
                    }
                } else {
                    stream.write(temp);
                    stream.write((value >> 7 & 0x7F));
                }
            } else {
                stream.write(temp);
                stream.write((value >> 14) & 0x7F);
                stream.write((value >> 7) & 0x7F);
            }
        } else {
            stream.write(temp);
            stream.write((value >> 21) & 0x7F);
            stream.write((value >> 14) & 0x7F);
            stream.write((value >> 7) & 0x7F);
        }
        stream.write((value & 0x7F) | 0x80);
    }

    private class TypeSorter<T extends Typeable> extends Thread {

        private Collection<T> typeables;
        private int[] typeDistribution;
        private int[] typeOrder;

        public TypeSorter(final Collection<T> source, final int[] typeOrder) {
            this.typeables = source;
            this.typeOrder = typeOrder;
            // TODO type order
        }

        @Override
        public void run() {
            int maxValue = 0;
            for (final Typeable t : typeables) {
                if (t.getType() > maxValue) {
                    maxValue = t.getType();
                }
            }

            List<List<T>> typeLists = new ArrayList<List<T>>(maxValue + 1);
            typeDistribution = new int[maxValue + 1];

            for (int i = 0; i < maxValue + 1; i++) {
                typeLists.add(new LinkedList<T>());
            }

            for (final T t : typeables) {
                typeLists.get(t.getType()).add(t);
            }

            final List<T> typeables = new ArrayList<T>(this.typeables.size());

            for (int i = 0; i < typeLists.size(); i++) {
                final List<T> list = typeLists.get(i);
                typeDistribution[i] = list.size();
                typeables.addAll(list);
            }
            // for (final int type : typeOrder) {
            // final List<T> list = typeLists.get(type);
            // typeDistribution[type] = list.size();
            // typeables.addAll(list);
            // }

            typeLists = null;
            this.typeables = typeables;
        }

        // If not finished yet, original collection will be returned
        public Collection<T> getSorting() {
            return typeables;
        }

        public int[] getTypeDistribution() {
            return typeDistribution;
        }

    }
}
