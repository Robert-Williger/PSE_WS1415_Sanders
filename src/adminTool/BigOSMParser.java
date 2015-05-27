package adminTool;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import model.elements.Area;
import model.elements.Building;
import model.elements.Node;
import model.elements.POI;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBBox;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;

public class BigOSMParser implements IOSMParser {
    private final Collection<model.elements.Way> wayList;
    private final Collection<UnprocessedStreet> streetList;
    private final Collection<Area> areaList;
    private final Collection<POI> poiList;
    private final Collection<Building> buildingList;
    private final Rectangle bBox;

    private PrintWriter logger;

    public BigOSMParser() {
        wayList = new ArrayList<model.elements.Way>();
        streetList = new ArrayList<UnprocessedStreet>();
        areaList = new ArrayList<Area>();
        poiList = new ArrayList<POI>();
        buildingList = new ArrayList<Building>();
        bBox = new Rectangle();

        try {
            logger = new PrintWriter(new File("Bericht.log"));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(final File osmFile) throws Exception {
        // final File partitionFile = File.createTempFile("partitions", null,
        // new File("."));
        // Partitioner partitioner = new Partitioner(partitionFile);
        // new BlockInputStream(new FileInputStream(osmFile),
        // partitioner).process();
        final int partitions = 8;// partitioner.getPartitions();
        // partitioner = null;

        System.out.println("finished partitioning");

        final File mappingsFile = new File("mappings3877323018597122262.tmp");// File.createTempFile("mappings",
                                                                              // null,
                                                                              // new
                                                                              // File("."));

        // DataInputStream mapIn = new DataInputStream(new
        // BufferedInputStream(new FileInputStream(partitionFile)));
        // DataOutputStream mapOut = new DataOutputStream(new
        // BufferedOutputStream(new FileOutputStream(mappingsFile)));

        for (int i = 0; i < partitions; i++) {
            // new BlockInputStream(new FileInputStream(osmFile), new
            // NodeMapper(mapIn, mapOut)).process();
            System.out.println("Finished mapping (" + (i + 1) + " / " + partitions + ")");
        }
        // mapOut.close();
        // mapIn.close();
        // mapIn = null;
        // mapOut = null;

        final Parser parser = new Parser(mappingsFile);
        new BlockInputStream(new FileInputStream(osmFile), parser).process();

    }

    @Override
    public Collection<model.elements.Way> getWays() {
        return wayList;
    }

    @Override
    public Collection<Area> getTerrain() {
        return areaList;
    }

    @Override
    public Collection<POI> getPOIs() {
        return poiList;
    }

    @Override
    public Collection<Building> getBuildings() {
        return buildingList;
    }

    @Override
    public Rectangle getBoundingBox() {
        return bBox;
    }

    @Override
    public Collection<UnprocessedStreet> getStreets() {
        return streetList;
    }

    private class Partitioner extends BinaryParser {
        private final int nodeLimit = 30_000_000;

        private DataOutputStream stream;
        private HashSet<Integer> nodeSet;
        private int wayBlocks;
        private int partitions;

        private int nodeCount;

        public Partitioner(final File file) {
            try {
                stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
            nodeSet = new HashSet<Integer>(nodeLimit);
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {

        }

        @Override
        protected void parseDense(final DenseNodes nodes) {

            for (int i = 0; i < nodes.getIdCount(); i++) {
                if (++nodeCount % 1000000 == 0) {
                    System.out.println(nodeCount);
                }
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            if (ways.size() > 0) {
                for (final Way w : ways) {
                    long lastRef = 0;
                    for (final Long ref : w.getRefsList()) {
                        lastRef += ref;
                        nodeSet.add((int) lastRef);
                    }
                }
                ++wayBlocks;

                if (nodeSet.size() > nodeLimit) {
                    save();
                }
            }
        }

        private void save() {
            try {
                stream.writeInt(wayBlocks);
                stream.writeInt(nodeSet.size());
                for (final Integer id : nodeSet) {
                    stream.writeInt(id);
                }
                wayBlocks = 0;
                ++partitions;
            } catch (final IOException e) {
                e.printStackTrace();
            }
            nodeSet.clear();
        }

        @Override
        public void complete() {
            save();
            try {
                stream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            nodeSet = null;
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {

        }

        @Override
        protected void parse(final HeaderBlock header) {
        }

        public int getPartitions() {
            return partitions;
        }
    }

    private class NodeMapper extends BinaryParser {
        private DataOutputStream stream;
        private HashSet<Integer> nodeSet;
        private int nodeCount;
        private int nodes;

        public NodeMapper(final DataInputStream in, final DataOutputStream out) {
            int wayBlocks = 0;
            int size = 0;
            try {
                wayBlocks = in.readInt();
                size = in.readInt();
                nodeSet = new HashSet<Integer>(size);
                for (int i = 0; i < size; i++) {
                    nodeSet.add(in.readInt());
                }

                stream = out;
                stream.writeInt(wayBlocks);
                stream.writeInt(size);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            for (final crosby.binary.Osmformat.Node n : nodes) {
                final int id = (int) n.getId();
                if (nodeSet.remove(id)) {
                    save(id, (int) n.getLon(), (int) n.getLat());
                }

                if (++nodeCount % 1000000 == 0) {
                    System.out.println(nodeCount + ", " + this.nodes);
                }
            }

        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;

            for (int i = 0; i < nodes.getIdCount(); i++) {
                lastId += nodes.getId(i);
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);

                if (++nodeCount % 1000000 == 0) {
                    System.out.println(nodeCount + ", " + this.nodes);
                }

                final int id = (int) lastId;
                if (nodeSet.contains(id)) {
                    save(id, (int) lastLat, (int) lastLon);
                }
            }
        }

        private void save(final int id, final int x, final int y) {
            try {
                stream.writeInt(id);
                stream.writeInt(x);
                stream.writeInt(y);
                ++nodes;

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void complete() {
            nodeSet = null;
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {

        }

        @Override
        protected void parseWays(final List<Way> ways) {

        }

        @Override
        protected void parse(final HeaderBlock header) {

        }
    }

    private class Parser extends BinaryParser {
        private final long start;

        private HashMap<Integer, Node> nodeMap;
        private HashMap<Integer, Byte> areaMap;
        private HashMap<Integer, List<Node>> wayMap;

        private DataInputStream stream;

        private final int SHIFT = 1 << 28;
        private int xOffset;
        private int yOffset;
        private int wayCount;
        private int relationCount;

        private int currentWayBlocks;
        private int maxWayBlocks;

        private int nodeCount;

        public Parser(final File file) {
            try {
                stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
            nodeMap = new HashMap<Integer, Node>(30000000);
            wayMap = new HashMap<Integer, List<Node>>();
            areaMap = new HashMap<Integer, Byte>();
            start = System.currentTimeMillis();
        }

        private void readNextMap() {
            try {
                for (final Node node : nodeMap.values()) {
                    final double lat = parseLat(node.getY());
                    final double lon = parseLon(node.getX());

                    final int x = (int) (getXCoord(lon) * SHIFT) - xOffset;
                    final int y = (int) Math.abs((getYCoord(lat) * SHIFT) - yOffset);

                    node.setLocation(x, y);
                }

                currentWayBlocks = 0;
                maxWayBlocks = stream.readInt();
                final int size = stream.readInt();
                nodeMap.clear();
                for (int i = 0; i < size; i++) {
                    nodeMap.put(stream.readInt(), new Node(stream.readInt(), stream.readInt()));
                    if (i % 1000000 == 0) {
                        System.out.println(i);
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {

        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            for (int i = 0; i < nodes.getIdCount(); i++) {
                if (++nodeCount % 1000000 == 0) {
                    System.out.println(nodeCount);
                }
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {

            if (ways.size() > 0) {
                if (currentWayBlocks == maxWayBlocks) {
                    readNextMap();
                }

                tunnel: for (final Way w : ways) {
                    // Tags
                    String name = "";
                    String buildingTag = "";
                    String waytype = "";
                    String terrain = "";
                    String address = "";
                    String housenumber = "";

                    if (++wayCount % 1000000 == 0) {
                        logger.println("[" + ((System.currentTimeMillis() - start) / 1000) + "]: Read " + wayCount
                                + " ways");
                        logger.flush();
                    }

                    for (int i = 0; i < w.getKeysCount(); i++) {

                        final String key = getStringById(w.getKeys(i));
                        final String value = getStringById(w.getVals(i));

                        switch (key) {
                            case "leisure":
                                if (value.equals("track")) {
                                    waytype = "career";
                                    break;
                                }
                            case "landuse":
                            case "natural":
                            case "amenity":
                                if (terrain.isEmpty() && getAreaType(value) > 0) {
                                    terrain = value;
                                }
                                break;
                            case "name":
                                name = value;
                                break;
                            case "highway":
                            case "railway":
                                waytype = value;
                                break;
                            case "building":
                                buildingTag = value;
                                break;
                            case "waterway":
                                if (value.equals("riverbank")) {
                                    terrain = "water";
                                } else {
                                    waytype = value;
                                }
                                break;
                            case "addr:street":
                                address = value;
                                break;
                            case "addr:housenumber":
                                housenumber = value;
                                break;
                            case "tunnel":
                                continue tunnel;
                        }
                    }

                    final ArrayList<Node> nodes = new ArrayList<Node>(w.getRefsList().size());

                    long lastRef = 0;
                    for (final Long ref : w.getRefsList()) {
                        lastRef += ref;
                        final Node node = nodeMap.get((int) lastRef);
                        if (node != null) {
                            nodes.add(node);
                        }
                    }

                    if (nodes.size() > 1) {
                        wayMap.put((int) w.getId(), nodes);

                        if (getStreetType(waytype) >= 0) {
                            final List<Point2D.Double> degrees = new LinkedList<Point2D.Double>();
                            for (final Node node : nodes) {
                                final double lat = parseLat(node.getY());
                                final double lon = parseLon(node.getX());

                                degrees.add(new Point2D.Double(lat, lon));
                            }
                            final UnprocessedStreet street = new UnprocessedStreet(degrees, nodes,
                                    getStreetType(waytype), name);
                            // if (!name.isEmpty()) {
                            streetList.add(street);
                            // } else {
                            // streetMap.put(w.getId(), street);
                            // }
                        } else {
                            if (!buildingTag.isEmpty()) {
                                final Building building = new Building(nodes, (address + " " + housenumber), null);

                                // if (address.isEmpty() &&
                                // !housenumber.isEmpty())
                                // {
                                // buildingMap.put(w.getId(), building);
                                // } else {
                                buildingList.add(building);
                                // }
                            } else if (getAreaType(terrain) >= 0) {
                                final Area area = new Area(nodes, getAreaType(terrain));
                                areaMap.put((int) w.getId(), (byte) area.getType());
                                if (area.getNodes().get(0).equals(area.getNodes().get(area.getNodes().size() - 1))) {
                                    areaList.add(area);
                                }
                            } else if (getWayType(waytype) >= 0) {
                                wayList.add(new model.elements.Way(nodes, getWayType(waytype), ""));
                            }
                        }
                    }
                }

                ++currentWayBlocks;
            }
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {

            for (final Relation relation : rels) {

                if (++relationCount % 100000 == 0) {
                    logger.println("[" + ((System.currentTimeMillis() - start) / 1000) + "]: Read " + relationCount
                            + " relations");
                    logger.flush();
                }

                String relationType = "";

                for (int i = 0; i < relation.getKeysCount(); i++) {
                    final String key = getStringById(relation.getKeys(i));
                    final String value = getStringById(relation.getVals(i));
                    if (key.equals("type")) {
                        relationType = value;
                        break;
                    }
                }

                if (relationType.equals("multipolygon")) {
                    int areaType = -1;
                    for (int i = 0; i < relation.getKeysCount(); i++) {
                        final String key = getStringById(relation.getKeys(i));
                        final String value = getStringById(relation.getVals(i));

                        if (key.equals("waterway") && value.equals("riverbank")) {
                            areaType = 6;
                            break;
                        } else if (key.equals("leisure") || key.equals("natural") || key.equals("landuse")) {
                            areaType = getAreaType(value);
                            if (areaType != -1) {
                                break;
                            }
                        }
                    }

                    if (areaType == -1) {
                        long id = 0;
                        for (int j = 0; j < relation.getRolesSidCount(); j++) {
                            final String role = getStringById(relation.getRolesSid(j));
                            id += relation.getMemids(j);
                            if (role.equals("outer")) {
                                if (areaMap.containsKey(id)) {
                                    final int type = areaMap.get(id);
                                    if (type != -1) {
                                        areaType = type;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (areaType != -1) {
                        long id = 0;
                        final List<HashMap<Node, List<Node>>> maps = new ArrayList<HashMap<Node, List<Node>>>();
                        maps.add(new HashMap<Node, List<Node>>());
                        maps.add(new HashMap<Node, List<Node>>());

                        for (int j = 0; j < relation.getRolesSidCount(); j++) {
                            final String role = getStringById(relation.getRolesSid(j));
                            id += relation.getMemids(j);

                            if ((role.equals("inner") || role.equals("outer"))) {
                                if (wayMap.containsKey(id)) {
                                    List<Node> my = new ArrayList<Node>(wayMap.get(id));

                                    final HashMap<Node, List<Node>> map = maps.get(role.equals("inner") ? 1 : 0);
                                    final Node myFirst = my.get(0);
                                    final Node myLast = my.get(my.size() - 1);

                                    if (map.containsKey(myLast)) {
                                        final List<Node> other = map.get(myLast);
                                        map.remove(other.get(0));
                                        map.remove(other.get(other.size() - 1));

                                        if (other.get(0).equals(myLast)) {
                                            for (final ListIterator<Node> it = other.listIterator(1); it.hasNext();) {
                                                my.add(it.next());
                                            }
                                        } else {
                                            for (final ListIterator<Node> it = other.listIterator(other.size()); it
                                                    .hasPrevious();) {
                                                my.add(it.previous());
                                            }
                                        }
                                    }

                                    if (map.containsKey(myFirst)) {
                                        final List<Node> other = map.get(myFirst);
                                        map.remove(other.get(0));
                                        map.remove(other.get(other.size() - 1));

                                        if (other.get(0).equals(myFirst)) {
                                            Collections.reverse(my);
                                            for (final ListIterator<Node> it = other.listIterator(1); it.hasNext();) {
                                                my.add(it.next());
                                            }
                                        } else {
                                            for (final ListIterator<Node> it = my.listIterator(1); it.hasNext();) {
                                                other.add(it.next());
                                            }
                                            my = other;
                                        }
                                    }

                                    if (my.get(0).equals(my.get(my.size() - 1))) {
                                        areaList.add(new Area(my, areaType));
                                    } else {
                                        map.put(my.get(my.size() - 1), my);
                                        map.put(my.get(0), my);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // else if (relationType.equals("associatedStreet") ||
            // relationType.equals("street")) {
            // String name = "";
            // for (int i = 0; i < relation.getKeysCount(); i++) {
            // final String key = getStringById(relation.getKeys(i));
            // final String value = getStringById(relation.getVals(i));
            // if (key.equals("name")) {
            // name = value;
            // break;
            // }
            // }
            // if (!name.isEmpty()) {
            // long id = 0;
            // for (int j = 0; j < relation.getRolesSidCount(); j++) {
            // final String role = getStringById(relation.getRolesSid(j));
            // id += relation.getMemids(j);
            // if (role.equals("street")) {
            // final UnprocessedStreet street = streetMap.get(id);
            // if (street != null) {
            // streetList.add(new UnprocessedStreet(street.getNodes(),
            // street.getType(), name));
            // streetMap.remove(id);
            // }
            // } else if (role.equals("house") || role.equals("address")) {
            // final Building building = buildingMap.get(id);
            // if (building != null) {
            // buildingMap.remove(id);
            // buildingList.add(new Building(building.getNodes(), name +
            // building.getAddress(),
            // null));
            // }
            // }
            // }
            // }
            // } else if (relationType.equals("route")) {
            // String type = "";
            // String name = "";
            // for (int i = 0; i < relation.getKeysCount(); i++) {
            // final String key = getStringById(relation.getKeys(i));
            // final String value = getStringById(relation.getVals(i));
            //
            // if (key.equals("route")) {
            // type = value;
            // } else if (key.equals("name")) {
            // name = value;
            // }
            // }
            // if (type.equals("hiking") || type.equals("foot") ||
            // type.equals("bicycle") || type.equals("mtb")
            // || type.equals("fitness_trail") || type.equals("inline_skates")
            // || type.equals("road")) {
            //
            // if (name.length() <= 30 && name.matches("[ 'öÖäÄüÜßa-zA-Z]+")) {
            // long id = 0;
            //
            // for (int j = 0; j < relation.getRolesSidCount(); j++) {
            // id += relation.getMemids(j);
            // final UnprocessedStreet street = streetMap.get(id);
            // if (street != null) {
            // streetList.add(new UnprocessedStreet(street.getNodes(),
            // street.getType(), name));
            // streetMap.remove(id);
            // }
            // }
            // }
            // }
            // }
            // }
        }

        @Override
        public void complete() {
            // streetList.addAll(streetMap.values());
            // buildingList.addAll(buildingMap.values());

            for (final Node node : nodeMap.values()) {
                final double lat = parseLat(node.getY());
                final double lon = parseLon(node.getX());

                final int x = (int) (getXCoord(lon) * SHIFT) - xOffset;
                final int y = (int) Math.abs((getYCoord(lat) * SHIFT) - yOffset);

                node.setLocation(x, y);
            }
            for (final POI poi : poiList) {
                final double lat = parseLat(poi.getY());
                final double lon = parseLon(poi.getX());

                final int x = (int) (getXCoord(lon) * SHIFT) - xOffset;
                final int y = (int) Math.abs((getYCoord(lat) * SHIFT) - yOffset);

                poi.setLocation(x, y);
            }

            logger.println("Created " + nodeMap.size() + " nodes");
            logger.println("Created " + streetList.size() + " streets");
            logger.println("Created " + wayList.size() + " ways");
            logger.println("Created " + buildingList.size() + " buildings");
            logger.println("Created " + areaList.size() + " areas");
            logger.println("Created " + poiList.size() + " pois");
            logger.println("Ended successfully work!");
            logger.close();

            nodeMap = null;
            // streetMap = null;
            // buildingMap = null;
            areaMap = null;
            wayMap = null;

            // assert false;
        }

        @Override
        protected void parse(final HeaderBlock header) {

            // LON=x
            // LAT=y

            final HeaderBBox hb = header.getBbox();

            final int lonLeft = (int) (getXCoord(.000000001 * hb.getLeft()) * SHIFT);
            final int lonRight = (int) (getXCoord(.000000001 * hb.getRight()) * SHIFT);
            final int latTop = (int) (getYCoord(.000000001 * hb.getTop()) * SHIFT);
            final int latBottom = (int) (getYCoord(.000000001 * hb.getBottom()) * SHIFT);

            xOffset = lonLeft;
            yOffset = latTop;

            final double xDif = Math.abs(lonLeft - lonRight);

            final double yDif = Math.abs(latTop - latBottom);

            bBox.setSize((int) xDif, (int) yDif);

        }
    }

    private int getWayType(final String value) {

        // Fluesse (keine multipolygone)
        if (value.equals("river")) {
            return 1;
        }

        if (value.equals("stream") || value.equals("canal")) {
            return 2;
        }
        // Bahnlinien
        if ((value.equals("rail") || value.equals("light_rail"))) {
            return 3;
        }

        // nicht befahrbare Straßen
        if (value.equals("primary")) {
            return 4;
        }

        if (value.equals("motorway")) {
            return 5;
        }
        if (value.equals("trunk")) {
            return 6;
        }

        if (value.equals("primary_link")) {
            return 7;
        }
        if (value.equals("motorway_link")) {
            return 8;
        }
        if (value.equals("trunk_link")) {
            return 9;
        }
        if (value.equals("career")) {
            return 10;
        }

        return -1;

    }

    private int getStreetType(final String value) {

        if (value.equals("secondary") || value.equals("secondary_link")) {
            return 1;
        }
        if (value.equals("tertiary") || value.equals("tertiary_link")) {
            return 2;
        }
        if (value.equals("unclassified")) {
            return 3;
        }
        if (value.equals("residential")) {
            return 4;
        }
        if (value.equals("service")) {
            return 5;
        }
        if (value.equals("living_street")) {
            return 6;
        }
        if (value.equals("pedestrian")) {
            return 7;
        }
        if (value.equals("track")) {
            return 8;
        }
        if (value.equals("road")) {
            return 9;
        }
        if (value.equals("footway")) {
            return 10;
        }
        if (value.equals("cycleway")) {
            return 11;
        }
        if (value.equals("bridleway")) {
            return 12;
        }
        if (value.equals("path")) {
            return 13;
        }
        return -1;

    }

    private int getAreaType(final String value) {
        if (value.equals("forest") || value.equals("wood") || value.equals("woodland") || value.equals("scrub")) {
            return 1;
        }
        if (value.equals("grass") || value.equals("meadow") || value.equals("orchard") || value.equals("grassland")
                || value.equals("village_green") || value.equals("vineyard") || value.equals("allotments")
                || value.equals("recreation_ground") || value.equals("garden")) {
            return 2;
        }
        if (value.equals("farmland") || value.equals("farmyard") || value.equals("farm")) {
            return 3;
        }
        if (value.equals("greenfield") || value.equals("landfill") || value.equals("brownfield")
                || value.equals("construction") || value.equals("paddock")) {
            return 4;
        }
        if (value.equals("residential") || value.equals("railway") || value.equals("garages") || value.equals("garage")
                || value.equals("traffic_island") || value.equals("road") || value.equals("plaza")) {
            return 5;
        }
        if (value.equals("water") || value.equals("reservoir") || value.equals("basin")) {
            return 6;
        }
        if (value.equals("industrial")) {
            return 7;
        }
        if (value.equals("retail") || value.equals("commercial")) {
            return 8;
        }
        if (value.equals("heath") || value.equals("fell")) {
            return 9;
        }
        if (value.equals("sand") || value.equals("beach")) {
            return 10;
        }
        if (value.equals("mud") || value.equals("scree") || value.equals("bare_rock")) {
            return 11;
        }
        if (value.equals("quarry")) {
            return 12;
        }
        if (value.equals("sports_centre") || value.equals("stadium")) {
            return 13;
        }
        if (value.equals("parking")) {
            return 14;
        }
        if (value.equals("golf_course") || value.equals("miniature_golf")) {
            return 15;
        }
        if (value.equals("park")) {
            return 16;
        }
        if (value.equals("playground")) {
            return 17;
        }
        if (value.equals("pitch")) {
            return 18;
        }
        if (value.equals("cemetery")) {
            return 19;
        }

        return -1;

    }

    /*
     * Umwandlung lat/lon zu mercator-Koordianten
     */
    public double getXCoord(final double lon) {
        return lon * Math.PI / 180;
    }

    public double getYCoord(final double lat) {
        return Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360));
    }

    protected static int amenityToType(final String amenity) {

        if (amenity.equals("restaurant")) {
            return 1;
        }

        if (amenity.equals("bank")) {
            return 2;
        }

        if (amenity.equals("cafe")) {
            return 3;
        }
        if (amenity.equals("hospital")) {
            return 4;
        }
        if (amenity.equals("school")) {
            return 5;
        }
        if (amenity.equals("parking")) {
            return 6;
        }
        if (amenity.equals("fuel")) {
            return 7;
        }
        return -1;
    }

}
