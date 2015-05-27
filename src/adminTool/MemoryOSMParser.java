package adminTool;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
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
import java.util.Iterator;
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

public class MemoryOSMParser implements IOSMParser {

    private final Collection<model.elements.Way> wayList;
    private final Collection<UnprocessedStreet> streetList;
    private final Collection<Area> areaList;
    private final Collection<POI> poiList;
    private final Collection<Building> buildingList;
    private final Rectangle bBox;

    private PrintWriter logger;
    private final long start;

    public MemoryOSMParser() {
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
        start = System.currentTimeMillis();
    }

    @Override
    public void read(final File osmFile) throws Exception {
        final File nodeFile = File.createTempFile("nodes", null, new File("."));
        final File setFile = File.createTempFile("sets", null, new File("."));
        NodeCreator nodeCreator = new NodeCreator(nodeFile, setFile);
        new BlockInputStream(new FileInputStream(osmFile), nodeCreator).process();
        final int size = nodeCreator.getSize();
        nodeCreator = null;

        final File wayFile = File.createTempFile("ways", null, new File("."));
        final File streetFile = File.createTempFile("streets", null, new File("."));
        final File areaFile = File.createTempFile("areas", null, new File("."));
        final File buildingFile = File.createTempFile("buildings", null, new File("."));
        final ElementCreator elementCreator = new ElementCreator(size, wayFile, streetFile, areaFile, buildingFile);
        new BlockInputStream(new FileInputStream(osmFile), elementCreator).process();
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

    private class NodeCreator extends BinaryParser {

        private int size;
        private DataOutputStream nodes;
        private DataOutputStream sets;
        private final HashSet<Long> areaSet;
        private final HashSet<Long> waySet;

        public NodeCreator(final File nodes, final File sets) {
            try {
                this.nodes = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nodes)));
                this.sets = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sets)));
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
            areaSet = new HashSet<Long>();
            waySet = new HashSet<Long>();
        }

        public int getSize() {
            return size;
        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            long lastLat = 0;
            long lastLon = 0;

            for (int i = 0; i < nodes.getIdCount(); i++) {
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);

                save((int) lastLon, (int) lastLat);
            }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            for (final crosby.binary.Osmformat.Node node : nodes) {
                save((int) node.getLon(), (int) node.getLat());
            }
        }

        private final void save(final int x, final int y) {
            try {
                nodes.writeInt(x);
                nodes.writeInt(y);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            ++size;
        }

        @Override
        protected void parseWays(final List<Way> ways) {

        }

        @Override
        protected void parseRelations(final List<Relation> rels) {
            for (final Relation relation : rels) {
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
                                areaSet.add(id);
                            }
                        }
                    }

                    if (areaType != -1) {
                        long id = 0;

                        for (int j = 0; j < relation.getRolesSidCount(); j++) {
                            final String role = getStringById(relation.getRolesSid(j));
                            id += relation.getMemids(j);

                            if ((role.equals("inner") || role.equals("outer"))) {
                                waySet.add(id);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void complete() {
            try {
                nodes.close();
                sets.writeInt(areaSet.size());
                for (final Long value : areaSet) {
                    sets.writeLong(value);
                }
                sets.writeInt(waySet.size());
                for (final Long value : waySet) {
                    sets.writeLong(value);
                }
                sets.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parse(final HeaderBlock header) {

        }

    }

    private class ElementCreator extends BinaryParser {

        private final IDGenerator generator;
        private DataOutputStream ways;
        private DataOutputStream streets;
        private DataOutputStream areas;
        private DataOutputStream buildings;
        private int wayCount;

        public ElementCreator(final int size, final File ways, final File streets, final File areas,
                final File buildings) {
            generator = new IDGenerator(size);

            try {
                this.ways = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ways)));
                this.streets = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(streets)));
                this.areas = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(areas)));
                this.buildings = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(buildings)));
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void complete() {
            try {
                ways.close();
                streets.close();
                areas.close();
                buildings.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {

        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            long lastId = 0;

            for (int i = 0; i < nodes.getIdCount(); i++) {
                generator.createID(lastId += nodes.getId(i));
            }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            for (final crosby.binary.Osmformat.Node node : nodes) {
                generator.createID(node.getId());
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            tunnel: for (final Way w : ways) {
                // Tags
                String name = "";
                String buildingTag = "";
                String waytype = "";
                String terrain = "";
                String address = "";
                String housenumber = "";

                if (++wayCount % 1000000 == 0) {
                    System.out.println(wayCount);
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

                final List<Long> refs = w.getRefsList();

                int count = 0;
                long lastRef = 0;
                for (final Long ref : refs) {
                    if (generator.getId(lastRef += ref) != 0) {
                        ++count;
                    }
                }

                if (count > 1) {
                    if (getStreetType(waytype) >= 0) {
                        try {
                            streets.writeUTF(name);
                            streets.writeByte(getStreetType(waytype));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        writeNodeList(streets, count, refs);
                    } else {
                        if (!buildingTag.isEmpty()) {
                            try {
                                buildings.writeUTF(address + " " + housenumber);
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                            writeNodeList(buildings, count, refs);
                        } else if (getAreaType(terrain) >= 0) {
                            try {
                                areas.writeByte(getAreaType(terrain));
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                            writeNodeList(areas, count, refs);
                        } else if (getWayType(waytype) >= 0) {
                            try {
                                this.ways.writeByte(getWayType(waytype));
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                            writeNodeList(this.ways, count, refs);
                        }
                    }
                }
            }
        }

        private void writeNodeList(final DataOutputStream stream, final int size, final List<Long> nodes) {
            long lastRef = 0;
            try {
                stream.writeShort(size);
                for (final Long ref : nodes) {
                    final int id = generator.getId(lastRef += ref);
                    if (id > 0) {
                        stream.writeInt(id - 1);
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parse(final HeaderBlock header) {

        }

    }

    private class Parser extends BinaryParser {
        private HashMap<Integer, Node> nodeMap;
        private HashMap<Integer, Byte> areaMap;
        private HashMap<Integer, List<Node>> wayMap;

        private final int SHIFT = 1 << 28;
        private int xOffset;
        private int yOffset;
        private int nodeCount;
        private int wayCount;
        private int relationCount;

        public Parser() {
            nodeMap = new HashMap<Integer, Node>(208000000);
            wayMap = new HashMap<Integer, List<Node>>();
            areaMap = new HashMap<Integer, Byte>();
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            for (final crosby.binary.Osmformat.Node n : nodes) {
                final int x = (int) n.getLon();
                final int y = (int) n.getLat();

                for (int i = 0; i < n.getKeysCount(); i++) {

                    final String key = getStringById(n.getKeys(i));
                    final String value = getStringById(n.getVals(i));

                    if (key.equals("amenity")) {
                        final int type = amenityToType(value);
                        if (type >= 0) {
                            poiList.add(new POI(x, y, type));
                        }
                    }
                }

                nodeMap.put((int) n.getId(), new Node(x, y));

                if (++nodeCount % 1000000 == 0) {
                    logger.println("[" + ((System.currentTimeMillis() - start) / 1000) + "]: Read " + nodeCount
                            + " nodes");
                    logger.flush();
                }
            }
        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;

            final Iterator<Integer> iterator = nodes.getKeysValsList().iterator();

            for (int i = 0; i < nodes.getIdCount(); i++) {
                lastId += nodes.getId(i);
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);

                final int x = (int) lastLon;
                final int y = (int) lastLat;

                int keyValId;

                while ((keyValId = iterator.next()) != 0) {
                    final String key = getStringById(keyValId);
                    final String value = getStringById(iterator.next());

                    if (key.equals("amenity")) {
                        final int type = amenityToType(value);
                        if (type >= 0) {
                            poiList.add(new POI(x, y, type));
                        }
                    }
                }

                assert !nodeMap.containsKey((int) lastId);
                nodeMap.put((int) lastId, new Node(x, y));

                if (++nodeCount % 1000000 == 0) {
                    logger.println("[" + ((System.currentTimeMillis() - start) / 1000) + "]: Read " + nodeCount
                            + " nodes");
                    logger.flush();
                }
            }

        }

        @Override
        protected void parseWays(final List<Way> ways) {

            tunnel: for (final Way w : ways) {
                // Tags
                String name = "";
                String buildingTag = "";
                String waytype = "";
                String terrain = "";
                String address = "";
                String housenumber = "";

                if (++wayCount % 100000 == 0) {
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
                    nodes.add(nodeMap.get((int) lastRef));
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
                        final UnprocessedStreet street = new UnprocessedStreet(degrees, nodes, getStreetType(waytype),
                                name);
                        // if (!name.isEmpty()) {
                        streetList.add(street);
                        // } else {
                        // streetMap.put(w.getId(), street);
                        // }
                    } else {
                        if (!buildingTag.isEmpty()) {
                            final Building building = new Building(nodes, (address + " " + housenumber), null);

                            // if (address.isEmpty() && !housenumber.isEmpty())
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
