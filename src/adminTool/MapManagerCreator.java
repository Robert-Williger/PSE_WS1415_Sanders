package adminTool;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import util.Arrays;
import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import adminTool.elements.POI;
import adminTool.elements.ReferencedPoint;
import adminTool.elements.ReferencedRectangle;
import adminTool.elements.ReferencedTile;
import adminTool.elements.Street;
import adminTool.elements.StreetNode;
import adminTool.elements.Typeable;
import adminTool.elements.Way;
import model.map.PixelConverter;

public class MapManagerCreator extends AbstractMapCreator {

    private static final int MIN_TILES = 1;
    private static final int TILE_LENGTH = 256;
    private static final int MAX_ZOOM_STEPS = 13;

    private static final int POI_WIDTH = 20;
    private static final int WAY_WIDTH = 20;
    private static final int MIN_POI_DISTANCE = 40;
    private static final int MAX_BUILDING_STREET_DISTANCE = 4000;

    private static final int BUILDING_MIN_ZOOMSTEP = 14;
    private static final int POI_MIN_ZOOMSTEP = 16;

    private static final int AREA_THRESHHOLD = 2;
    private static final int WAY_THRESHHOLD = 2;
    private static final double AREA_SHRINK_FACTOR = 0.9;
    private static final double WAY_SHRINK_FACTOR = 0.9;

    private Collection<Building> buildings;
    private Collection<ReferencedRectangle> referencedLabels;
    private Collection<ReferencedPoint> referencedPOIs;

    private Rectangle bounds;

    private int minZoomStep;
    private int maxZoomStep;
    private int coordTileLength;

    private StreetNodeFinder finder;
    private TypeSorter<POI> poiSorter;
    private TypeSorter<Way> waySorter;
    private TypeSorter<Street> streetSorter;
    private TypeSorter<Area> terrainSorter;
    private TypeSorter<Label> labelSorter;

    // TODO make simplifications relative to last simplifications
    private Node[][] areaNodes;
    private int[][] areaSimplifications;
    private Node[][] wayNodes;
    private int[][] waySimplifications;

    private PixelConverter converter;
    private ReferencedTile[][] tiles;

    public MapManagerCreator(final Collection<Building> buildings, final Collection<Street> streets,
            final Collection<POI> pois, final Collection<Way> ways, final Collection<Area> terrain,
            final Collection<Label> labels, final Rectangle boundingBox, final File file) {
        super(file);

        this.buildings = buildings;
        this.bounds = boundingBox;

        poiSorter = new TypeSorter<POI>(pois);
        waySorter = new TypeSorter<Way>(ways);
        streetSorter = new TypeSorter<Street>(streets);
        terrainSorter = new TypeSorter<Area>(terrain);
        labelSorter = new TypeSorter<Label>(labels);
    }

    @Override
    public void create() {
        final int zoomOffset = (int) Math.ceil(log2(Math.min(bounds.getWidth(), bounds.getHeight()) / MIN_TILES));
        final int topTileLength = (1 << zoomOffset);
        minZoomStep = 29 - zoomOffset;
        final int conversionFactor = 1 << 21;

        maxZoomStep = Math.min(19, minZoomStep + MAX_ZOOM_STEPS - 1);
        coordTileLength = topTileLength >> (maxZoomStep - minZoomStep);

        final int xTiles = (int) Math.ceil(bounds.getWidth() / coordTileLength);
        final int yTiles = (int) Math.ceil(bounds.getHeight() / coordTileLength);

        converter = new PixelConverter(conversionFactor);

        try {
            createOutputStream(true);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

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
        labelSorter.start();

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

        LabelPartitioner labelPartitioner = new LabelPartitioner();
        labelPartitioner.start();

        try {
            buildingPartitioner.join();
            wayPartitioner.join();
            streetPartitioner.join();
            terrainPartitioner.join();
            poiPartitioner.join();
            labelPartitioner.join();

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
        // TODO null it
        // streetSorter = null;
        terrainSorter = null;

        buildings = null;

        areaSimplifications = new int[areaNodes.length][];
        waySimplifications = new int[wayNodes.length][];
        TileWriter writer = new TileWriter();

        int[][] nextAreaSimplifications = new int[areaNodes.length][];
        int[][] nextWaySimplifications = new int[wayNodes.length][];

        for (int zoom = maxZoomStep - 1; zoom >= minZoomStep; zoom--) {

            writer.start();

            final Set<Integer> removedAreas = new HashSet<Integer>();
            final List<Integer> simplifiedAreas = simplifyAreas(nextAreaSimplifications, new HashSet<Integer>(), zoom);

            final List<Integer> simplifiedWays = simplifyWays(nextWaySimplifications, zoom);

            final ReferencedTile[][] next = mergeUp(removedAreas, zoom);

            try {
                writer.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            int[][] swapAreaSimplifications = areaSimplifications;
            int[][] swapWaySimplifications = waySimplifications;

            areaSimplifications = nextAreaSimplifications;
            waySimplifications = nextWaySimplifications;

            nextAreaSimplifications = swapAreaSimplifications;
            nextWaySimplifications = swapWaySimplifications;

            tiles = next;
            writer = new TileWriter(simplifiedAreas, simplifiedWays);
        }

        referencedLabels = null;
        referencedPOIs = null;

        writer.run();

        try {
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<Street> getOrderedStreets() {
        return streetSorter.getSorting();
    }

    private List<Integer> simplifyAreas(final int[][] simplifications, final Set<Integer> removedAreas, final int zoom) {
        final List<Integer> simplifiedAreas = new LinkedList<Integer>();

        final VisvalingamWhyatt simplificator = new VisvalingamWhyatt(converter, AREA_THRESHHOLD);
        for (int i = 0; i < areaNodes.length; i++) {
            final Node[] nodes = areaNodes[i];
            final int[] areaSimplification = areaSimplifications[i];

            final int[] simplified;
            final int oldLength;

            if (areaSimplification != null) {
                oldLength = areaSimplification.length;
                if (oldLength != 0) {
                    simplified = simplificator.simplifyPolygon(Arrays.iterator(nodes), zoom);
                    // for (int j = 0; j < simplified.length; j++) {
                    // simplified[j] = areaSimplification[simplified[j]];
                    // }
                } else {
                    simplified = areaSimplification;
                }

            } else {
                oldLength = nodes.length;
                simplified = simplificator.simplifyPolygon(Arrays.iterator(nodes), zoom);
            }

            if (simplified.length == 0) {
                removedAreas.add(i);
                simplifications[i] = simplified;
            } else if ((double) simplified.length / oldLength <= AREA_SHRINK_FACTOR) {
                simplifiedAreas.add(i);
                simplifications[i] = simplified;
                final Node[] newNodes = new Node[simplified.length];
                for (int j = 0; j < simplified.length; j++) {
                    newNodes[j] = nodes[simplified[j]];
                }
                areaNodes[i] = newNodes;
            } else {
                simplifications[i] = areaSimplification;
            }
        }

        return simplifiedAreas;
    }

    private List<Integer> simplifyWays(final int[][] simplifications, final int zoom) {
        final List<Integer> ret = new LinkedList<Integer>();

        final VisvalingamWhyatt simplificator = new VisvalingamWhyatt(converter, WAY_THRESHHOLD);
        for (int i = 0; i < wayNodes.length; i++) {
            final Node[] nodes = wayNodes[i];
            final int[] waySimplification = waySimplifications[i];

            final int oldSize;
            final int[] simplified;

            if (waySimplification != null) {
                oldSize = waySimplification.length;
                simplified = simplificator.simplifyMultiline(Arrays.iterator(nodes), zoom);
                // for (int j = 0; j < simplified.length; j++) {
                // simplified[j] = waySimplification[simplified[j]];
                // }
            } else {
                oldSize = nodes.length;
                simplified = simplificator.simplifyMultiline(Arrays.iterator(nodes), zoom);
            }

            if ((double) simplified.length / oldSize <= WAY_SHRINK_FACTOR) {
                ret.add(i);
                simplifications[i] = simplified;
                final Node[] newNodes = new Node[simplified.length];
                for (int j = 0; j < simplified.length; j++) {
                    newNodes[j] = nodes[simplified[j]];
                }
                wayNodes[i] = newNodes;
            } else {
                simplifications[i] = waySimplification;
            }

        }

        return ret;
    }

    private ReferencedTile[][] mergeUp(final Set<Integer> removedAreas, final int zoom) {
        final ReferencedTile[][] array = new ReferencedTile[(tiles.length + 1) / 2][(tiles[0].length + 1) / 2];

        for (int i = 0; i < tiles.length; i += 2) {
            for (int j = 0; j < tiles[0].length; j += 2) {
                final ReferencedTile destination = new ReferencedTile();

                merge(getTile(i, j), destination, removedAreas, zoom);
                merge(getTile(i + 1, j), destination, removedAreas, zoom);
                merge(getTile(i, j + 1), destination, removedAreas, zoom);
                merge(getTile(i + 1, j + 1), destination, removedAreas, zoom);

                array[i / 2][j / 2] = destination;
            }
        }

        partitionPOIs(zoom, array);
        partitionLabels(zoom, array);

        return array;
    }

    private void merge(final ReferencedTile source, final ReferencedTile destination, final Set<Integer> removedAreas,
            final int zoom) {

        final Collection<Integer> areas = destination.getTerrain();
        for (final int area : source.getTerrain()) {
            if (!removedAreas.contains(area)) {
                areas.add(area);
            }
        }

        destination.getStreets().addAll(source.getStreets());
        destination.getWays().addAll(source.getWays());
        // destination.getLabels().addAll(source.getLabels());

        if (zoom >= BUILDING_MIN_ZOOMSTEP) {
            destination.getBuildings().addAll(source.getBuildings());
        }

    }

    private void partitionPOIs(final int zoom, final ReferencedTile[][] tiles) {
        if (zoom >= POI_MIN_ZOOMSTEP) {
            final int poiWidth = converter.getCoordDistance(POI_WIDTH, zoom);
            final Rectangle poiBounds = new Rectangle(poiWidth, poiWidth);

            for (final Iterator<ReferencedPoint> it = referencedPOIs.iterator(); it.hasNext();) {
                final ReferencedPoint poi = it.next();

                poiBounds.setLocation(poi.getX() - poiWidth / 2, poi.getY() - poiWidth / 2);
                final Rectangle poiTileBounds = locateRectangle(poiBounds, zoom);

                if (!hasPOIIntersection(poiTileBounds, poi, zoom, tiles)) {
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
    }

    private boolean hasPOIIntersection(final Rectangle poiTileBounds, final ReferencedPoint poi, final int zoom,
            final ReferencedTile[][] tiles) {
        for (int row = poiTileBounds.y; row <= poiTileBounds.height + poiTileBounds.y; row++) {
            for (int column = poiTileBounds.x; column <= poiTileBounds.width + poiTileBounds.x; column++) {
                final ReferencedTile tile = getTile(row, column, tiles);
                for (final ReferencedPoint other : tile.getPOIs()) {
                    final int distance = (int) Point.distance(other.getX(), other.getY(), poi.getX(), poi.getY());
                    if (converter.getPixelDistance(distance, zoom) < MIN_POI_DISTANCE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void partitionLabels(final int zoom, final ReferencedTile[][] tiles) {
        final Rectangle rectangle = new Rectangle();
        for (final Iterator<ReferencedRectangle> it = referencedLabels.iterator(); it.hasNext();) {
            final ReferencedRectangle label = it.next();
            final int coordWidth = converter.getCoordDistance(label.getWidth(), zoom);
            final int coordHeight = converter.getCoordDistance(label.getHeight(), zoom);
            rectangle.setBounds(label.getX() - coordWidth / 2, label.getY() - coordHeight / 2, coordWidth, coordHeight);

            final Rectangle poiTileBounds = locateRectangle(rectangle, zoom);
            for (int row = poiTileBounds.y; row <= poiTileBounds.height + poiTileBounds.y; row++) {
                for (int column = poiTileBounds.x; column <= poiTileBounds.width + poiTileBounds.x; column++) {
                    getTile(row, column, tiles).getLabels().add(label);
                }
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

        return ReferencedTile.EMPTY_TILE;
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private Path2D.Float createPath(final Iterator<Node> nodes) {
        final Path2D.Float path = new Path2D.Float();

        Node location = nodes.next();
        path.moveTo(location.getX(), location.getY());

        while (nodes.hasNext()) {
            location = nodes.next();
            path.lineTo(location.getX(), location.getY());
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

            referencedPOIs = new LinkedList<ReferencedPoint>();
            for (final POI poi : poiSorter.getSorting()) {
                referencedPOIs.add(new ReferencedPoint(poi.getX(), poi.getY()));
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
            areaNodes = new Node[terrainSorter.getSorting().size()][];

            try {
                terrainSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            int id = 0;
            for (final Area area : terrainSorter.getSorting()) {
                areaNodes[id] = area.getNodes();

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
            wayNodes = new Node[waySorter.getSorting().size()][];

            try {
                waySorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            final int maxWayWidth = converter.getCoordDistance(WAY_WIDTH, Math.min(15, maxZoomStep));

            int id = 0;
            for (final Way way : waySorter.getSorting()) {
                wayNodes[id] = way.getNodes();
                final Path2D.Float path = createPath(way.iterator());
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

            // TODO vergröbern
            final int maxWayWidth = converter.getCoordDistance(WAY_WIDTH, Math.min(15, maxZoomStep));

            int id = 0;
            for (final Street street : streetSorter.getSorting()) {
                final Path2D.Float path = createPath(street.iterator());
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

    private class LabelPartitioner extends Thread {
        private FontRenderContext context;
        private Font font;

        public LabelPartitioner() {
            super("Label partitioner");
            context = new FontRenderContext(new AffineTransform(), true, true);
            font = new Font("Arial", Font.PLAIN, 20);
        }

        @Override
        public void run() {
            try {
                labelSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            referencedLabels = new ArrayList<ReferencedRectangle>(labelSorter.getSorting().size());
            for (final Label label : labelSorter.getSorting()) {
                referencedLabels.add(getReferencedLabel(label));
            }
            partitionLabels(maxZoomStep, tiles);

        }

        private ReferencedRectangle getReferencedLabel(final Label label) {
            context.getTransform().rotate(label.getRotation());
            final Rectangle2D rect = font.getStringBounds(label.getName(), context);
            final ReferencedRectangle ret = new ReferencedRectangle(label.getX(), label.getY(), (int) rect.getWidth(),
                    (int) rect.getHeight());
            context.getTransform().rotate(-label.getRotation());

            return ret;
        }
    }

    private class StreetNodeFinder extends Thread {
        private HashMap<String, Collection<Street>> streetMap;

        private List<Building> success;
        private List<Building> failure;

        public StreetNodeFinder() {
            super("Streetnode finder");
        }

        public Collection<Building> getSuccess() {
            return success;
        }

        public Collection<Building> getFailure() {
            return failure;
        }

        @Override
        public void run() {
            createStreetMap();

            success = new ArrayList<Building>();
            failure = new ArrayList<Building>();

            for (final Building building : buildings) {

                final Polygon poly = building.getPolygon();
                final Collection<Street> streetList = streetMap.get(building.getStreet());

                StreetNode node = null;
                if (streetList != null) {
                    node = findStreetNode(streetList, calculateCenter(poly));
                }

                if (node != null) {
                    success.add(Building.create(building.getNodes(), node, building.getHouseNumber()));
                } else {
                    failure.add(building);
                }
            }

            streetMap = null;
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

        private StreetNode findStreetNode(final Collection<Street> streetList, final Point2D.Float center) {
            StreetNode ret = null;

            long minDist = MAX_BUILDING_STREET_DISTANCE;

            for (final Street street : streetList) {
                // skip if street is too far away

                Iterator<Node> iterator = street.iterator();

                Node node = iterator.next();

                int left = node.getX();
                int right = node.getX();
                int top = node.getY();
                int down = node.getY();

                while (iterator.hasNext()) {
                    node = iterator.next();

                    if (node.getX() > right) {
                        right = node.getX();
                    } else if (node.getX() < left) {
                        left = node.getX();
                    }

                    if (node.getY() > down) {
                        down = node.getY();
                    } else if (node.getY() < top) {
                        top = node.getY();
                    }
                }
                left -= minDist;
                right += minDist;
                top -= minDist;
                down += minDist;

                if (center.x < right && center.x > left && center.y < down && center.y > top) {
                    iterator = street.iterator();

                    Node lastNode = iterator.next();

                    float totalLength = 0;
                    final int maxLength = street.getLength();

                    while (iterator.hasNext()) {
                        final Node currentNode = iterator.next();

                        final long dx = currentNode.getX() - lastNode.getX();
                        final long dy = currentNode.getY() - lastNode.getY();
                        final long square = (dx * dx + dy * dy);
                        final float length = (float) Math.sqrt(square);
                        double s = ((center.x - lastNode.getX()) * dx + (center.y - lastNode.getY()) * dy)
                                / (double) square;

                        if (s < 0) {
                            s = 0;
                        } else if (s > 1) {
                            s = 1;
                        }

                        final double distX = lastNode.getX() + s * dx - center.x;
                        final double distY = lastNode.getY() + s * dy - center.y;

                        final long distance = (long) Math.sqrt(distX * distX + distY * distY);

                        if (distance < minDist) {
                            ret = new StreetNode((float) ((totalLength + s * length) / maxLength), street);
                            minDist = distance;
                        }

                        totalLength += length;
                        lastNode = currentNode;
                    }
                }
            }

            return ret;
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

                writeLabels();

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
                final String name = building.getStreet();
                final String number = building.getHouseNumber();

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
            writeCompressedInt(bounds.width);
            writeCompressedInt(bounds.height);
            writeCompressedInt(minZoomStep);
            writeCompressedInt(maxZoomStep);
            writeCompressedInt(tiles.length);
            stream.writeDouble(converter.getCoordDistance(1, 0));
            writeCompressedInt(TILE_LENGTH);
            writeCompressedInt(TILE_LENGTH);
        }

        private void writeNodes() throws IOException {
            writeCompressedInt(nodeMap.size());

            // TODO compress
            for (final Entry<Node, Integer> entry : nodeMap.entrySet()) {
                writePoint(entry.getKey());
            }
        }

        private void writePOIs() throws IOException {
            try {
                poiSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            writeDistribution(poiSorter.getTypeDistribution());

            // TODO compress
            for (final POI poi : poiSorter.getSorting()) {
                writePoint(poi);
            }
        }

        private void writeNames() throws IOException {
            writeCompressedInt(nameMap.size());
            for (final Entry<String, Integer> entry : nameMap.entrySet()) {
                stream.writeUTF(entry.getKey());
            }
        }

        private void writeNumbers() throws IOException {
            writeCompressedInt(numberMap.size());
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

            writeDistribution(streetSorter.getTypeDistribution());

            for (final Street street : streetSorter.getSorting()) {
                int node1 = (int) (street.getID() >> 32);
                int node2 = (int) (street.getID() & 0xFFFFFFFF);
                writeCompressedInt(node1);
                writeCompressedInt(node2 - node1);

                writeWay(street);
            }
        }

        private void writeWays() throws IOException {
            try {
                waySorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(waySorter.getTypeDistribution());
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

            writeCompressedInt(buildings.size());
            writeCompressedInt(finder.getSuccess().size());

            for (final Building building : finder.getSuccess()) {
                final StreetNode node = building.getStreetNode();

                writeMultiElement(building);
                assert streetMap.containsKey(node.getStreet()) : streetSorter.getSorting().contains(node.getStreet());
                stream.writeFloat(node.getOffset());
                writeCompressedInt(streetMap.get(node.getStreet()));
                writeCompressedInt(numberMap.get(building.getHouseNumber()));

            }

            for (final Building building : finder.getFailure()) {
                if (building.getStreetNode() == null) {
                    writeMultiElement(building);
                }
            }
        }

        private void writeLabels() throws IOException {
            try {
                labelSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(labelSorter.getTypeDistribution());
            for (final Label label : labelSorter.getSorting()) {
                writePoint(label);
                stream.writeUTF(label.getName());
            }
        }

        private void writeTerrain() throws IOException {
            try {
                terrainSorter.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            writeDistribution(terrainSorter.getTypeDistribution());
            for (final Area area : terrainSorter.getSorting()) {
                writeMultiElement(area);
            }
        }

        private int putNodes(final Collection<? extends MultiElement> elements, int nodeCount) {
            for (final MultiElement element : elements) {
                for (final Node node : element) {
                    if (!nodeMap.containsKey(node)) {
                        nodeMap.put(node, ++nodeCount);
                    }
                }
            }

            return nodeCount;
        }

        private void writePoint(final Node location) throws IOException {
            writeCompressedInt(location.getX());
            writeCompressedInt(location.getY());
        }

        private void writeMultiElement(final MultiElement element) throws IOException {
            writeCompressedInt(element.size());

            int last = 0;
            for (final Node node : element) {
                int current = nodeMap.get(node);
                writeCompressedInt(current - last);
                last = current;
            }
        }

        private void writeWay(final Way way) throws IOException {
            writeMultiElement(way);
            writeCompressedInt(nameMap.get(way.getName().trim()));
        }

        private void writeDistribution(final int[] distribution) throws IOException {
            int total = 0;

            writeCompressedInt(distribution.length);
            for (int type = 0; type < distribution.length; type++) {
                total += distribution[type];
                writeCompressedInt(total);
            }
        }
    }

    private class TileWriter extends Thread {

        final List<Integer> simplifiedAreas;
        final List<Integer> simplifiedWays;

        public TileWriter() {
            this(null, null);
        }

        public TileWriter(final List<Integer> simplifiedAreas, final List<Integer> simplifiedWays) {
            super("Tile writer");

            this.simplifiedAreas = simplifiedAreas;
            this.simplifiedWays = simplifiedWays;
        }

        @Override
        public void run() {
            try {
                if (simplifiedAreas != null) {
                    writeSimplifications();
                }
                writeTiles();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        private void writeSimplifications() throws IOException {
            writeSimplifications(simplifiedAreas, areaSimplifications);
            writeSimplifications(simplifiedWays, waySimplifications);
        }

        private void writeSimplifications(final List<Integer> simplifiedElements, final int[][] simplifications)
                throws IOException {

            final int size = simplifiedElements.size();
            final Iterator<Integer> elementsIt = simplifiedElements.iterator();

            writeCompressedInt(size);

            for (int i = 0; i < size; i++) {
                final int element = elementsIt.next();
                final int[] simplification = simplifications[element];

                writeCompressedInt(element);
                writeCompressedInt(simplification.length);

                int last = 0;
                for (final int index : simplification) {
                    writeCompressedInt(index - last);
                    last = index;
                }
            }
        }

        private void writeTiles() throws IOException {
            for (final ReferencedTile[] tileArray : tiles) {
                int leftOffset = 0;
                while (tileArray[leftOffset].getFlags() == 0 && leftOffset < tileArray.length) {
                    ++leftOffset;
                }
                int rightOffset = tileArray.length - 1;
                if (leftOffset != tileArray.length) {
                    while (tileArray[rightOffset].getFlags() == 0) {
                        --rightOffset;
                    }
                }
                writeCompressedInt(leftOffset);
                writeCompressedInt(rightOffset - leftOffset + 1);

                for (int column = leftOffset; column <= rightOffset; column++) {
                    final ReferencedTile tile = tileArray[column];

                    final byte flags = tile.getFlags();
                    stream.write(tile.getFlags());
                    if (flags != 0) {
                        if (!tile.getPOIs().isEmpty()) {
                            writeCompressedInt(tile.getPOIs().size());
                            int last = 0;
                            for (final ReferencedPoint poi : tile.getPOIs()) {
                                writeCompressedInt(poi.getID() - last);
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

                        if (!tile.getLabels().isEmpty()) {
                            writeCompressedInt(tile.getLabels().size());
                            int last = 0;
                            for (final ReferencedRectangle label : tile.getLabels()) {
                                writeCompressedInt(label.getID() - last);
                                last = label.getID();
                            }
                        }
                    }
                }
            }
        }

        private void writeInts(final Collection<Integer> collection) throws IOException {
            writeCompressedInt(collection.size());
            int last = 0;
            for (final Integer building : collection) {
                writeCompressedInt(building - last);
                last = building;
            }
        }
    }

    private class TypeSorter<T extends Typeable> extends Thread {

        private Collection<T> typeables;
        private int[] typeDistribution;

        public TypeSorter(final Collection<T> source) {
            this.typeables = source;
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
