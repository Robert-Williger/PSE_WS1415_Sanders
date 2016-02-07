package adminTool;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import util.Arrays;
import adminTool.elements.Boundary;
import adminTool.elements.UnprocessedStreet;
import adminTool.elements.Area;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.Node;
import adminTool.elements.POI;
import adminTool.elements.StreetNode;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;
import crosby.binary.file.BlockReaderAdapter;

public class BigOSMParser implements IOSMParser {

    private final Collection<adminTool.elements.Way> wayList;
    private final Collection<UnprocessedStreet> streetList;
    private final Collection<Area> areaList;
    private final Collection<POI> poiList;
    private final Collection<Building> buildingList;
    private final Collection<Label> labelList;
    private final List<List<Boundary>> boundaries;
    private final Rectangle bBox;

    public BigOSMParser() {
        // TODO lists instead of sets!
        labelList = new HashSet<Label>();
        wayList = new HashSet<adminTool.elements.Way>();
        streetList = new HashSet<UnprocessedStreet>();
        areaList = new HashSet<Area>();
        poiList = new HashSet<POI>();
        buildingList = new HashSet<Building>();
        bBox = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        boundaries = new ArrayList<List<Boundary>>(12);
        for (int i = 0; i < 12; i++) {
            boundaries.add(new ArrayList<Boundary>());
        }
    }

    @Override
    public void read(final File file) throws Exception {
        final InputStream input = new FileInputStream(file);
        final BlockReaderAdapter brad = new Parser();
        new BlockInputStream(input, brad).process();
    }

    @Override
    public Collection<adminTool.elements.Way> getWays() {
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

    @Override
    public Collection<Label> getLabels() {
        return labelList;
    }

    @Override
    public List<List<Boundary>> getBoundaries() {
        return boundaries;
    }

    private class Parser extends BinaryParser {
        private Map<Integer, Node> nodeMap;
        private Map<Integer, Byte> areaMap;
        private Map<Integer, Node[]> wayMap;

        private final int SHIFT = 1 << 29;

        public Parser() {
            nodeMap = new HashMap<Integer, Node>();
            wayMap = new HashMap<Integer, Node[]>();
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

                    if (key.equals("amenity") || key.equals("tourism")) {
                        final int type = getAmenityType(value);
                        if (type >= 0) {
                            poiList.add(new POI(x, y, type));
                        }
                    }
                }

                nodeMap.put((int) n.getId(), new Node(x, y));
            }

        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;

            String tag;

            final Iterator<Integer> iterator = nodes.getKeysValsList().iterator();

            for (int i = 0; i < nodes.getIdCount(); i++) {
                tag = "";

                lastId += nodes.getId(i);
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);

                final int x = (int) lastLon;
                final int y = (int) lastLat;
                int keyValId;

                while ((keyValId = iterator.next()) != 0) {
                    final String key = getStringById(keyValId);
                    final String value = getStringById(iterator.next());

                    switch (key) {
                        case "amenity":
                        case "tourism:":
                            int type = getAmenityType(value);
                            if (type >= 0) {
                                poiList.add(new POI(x, y, type));
                            }
                            break;
                        case "place":
                            if (tag.isEmpty()) {
                                tag = value;
                            } else {
                                type = getPlaceType(value);
                                if (type != -1) {
                                    labelList.add(Label.create(tag, type, x, y));
                                }
                            }
                            break;
                        case "name":
                            if (tag.isEmpty()) {
                                tag = value;
                            } else {
                                type = getPlaceType(tag);
                                if (type != -1) {
                                    labelList.add(Label.create(value, type, x, y));
                                }
                            }
                            break;
                    }
                }

                nodeMap.put((int) lastId, new Node(x, y));
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            String nameTag;
            String addrStreetTag;
            String addrNumberTag;

            String wayTag;

            String onewayTag;

            int terrainType;
            int amenityType;

            boolean buildingTag;
            boolean areaTag;
            boolean bicycle;
            boolean foot;
            boolean tunnel;

            for (final Way w : ways) {
                // Tags
                nameTag = "";
                buildingTag = false;
                wayTag = "";
                terrainType = -1;
                addrStreetTag = "";
                addrNumberTag = "";
                amenityType = -1;
                onewayTag = "";
                foot = false;
                bicycle = false;
                tunnel = false;
                areaTag = false;

                for (int i = 0; i < w.getKeysCount(); i++) {

                    final String key = getStringById(w.getKeys(i));
                    final String value = getStringById(w.getVals(i));

                    switch (key) {
                        case "leisure":
                            if (value.equals("track")) {
                                wayTag = "career";
                                break;
                            }
                        case "amenity":
                        case "tourism":
                            amenityType = getAmenityType(value);
                        case "landuse":
                        case "natural":
                        case "man_made":
                            if (terrainType == -1) {
                                terrainType = getAreaType(value, true);
                            }
                            break;
                        case "name":
                            nameTag = value.trim();
                            break;
                        case "highway":
                            if (value.equals("pedestrian")) {
                                terrainType = getAreaType("pedestrian", true);
                            }
                        case "railway":
                            wayTag = value;
                            break;
                        case "building":
                            buildingTag = true;
                            break;
                        case "waterway":
                            if (value.equals("riverbank")) {
                                terrainType = getAreaType("water", true);
                            } else {
                                wayTag = value;
                            }
                            break;
                        case "addr:street":
                            addrStreetTag = value.trim();
                            break;
                        case "addr:housenumber":
                            addrNumberTag = value.trim();
                            break;
                        case "bicycle":
                            if (value.equals("yes") || value.equals("designated")) {
                                bicycle = true;
                            }
                            break;
                        case "foot":
                            if (value.equals("yes") || value.equals("designated")) {
                                foot = true;
                            }
                            break;
                        case "tunnel":
                            tunnel = true;
                            break;
                        case "area":
                            if (value.equals("yes")) {
                                areaTag = true;
                            }
                            break;
                        case "barrier":
                            wayTag = value;
                            break;
                        case "oneway":
                            if (onewayTag.isEmpty()) {
                                onewayTag = value;
                            }
                            break;
                        case "oneway:bicycle":
                            if (value.equals("no")) {
                                onewayTag = "bicycle";
                            }
                            break;
                    }
                }

                final Node[] nodes = new Node[w.getRefsList().size()];
                long lastRef = 0;
                int count = 0;
                for (final Long ref : w.getRefsList()) {
                    lastRef += ref;
                    nodes[count++] = nodeMap.get((int) lastRef);
                }

                if (nodes.length > 1) {
                    wayMap.put((int) w.getId(), nodes);
                    int type;

                    if (buildingTag) {
                        buildingList.add(new RevalidateableBuilding(nodes, addrStreetTag, addrNumberTag));
                    } else {
                        if (terrainType >= 0) {
                            areaMap.put((int) w.getId(), (byte) terrainType);
                            // TODO equals instead of == ?
                            if (nodes[0] == nodes[nodes.length - 1]) {
                                areaList.add(new RevalidateableArea(nodes, terrainType));
                            }
                        }

                        if (!areaTag) {
                            // TODO ways with area tag also valid...
                            type = getStreetType(wayTag);
                            if (type >= 0) {
                                boolean oneway = false;
                                switch (onewayTag) {
                                    case "-1":
                                        Arrays.reverse(nodes); // fall throug
                                    case "yes":
                                        oneway = true;
                                        break;
                                }
                                final Point2D[] degrees = new Point2D[nodes.length];
                                for (int i = 0; i < nodes.length; i++) {
                                    final Node node = nodes[i];
                                    final float lat = (float) parseLat(node.getY());
                                    final float lon = (float) parseLon(node.getX());

                                    degrees[i] = new Point2D.Float(lon, lat);
                                }

                                if (wayTag.equals("path")) {
                                    if (bicycle) {
                                        type = getStreetType("cycleway");
                                    } else if (foot) {
                                        type = getStreetType("footway");
                                    }
                                }

                                streetList.add(new UnprocessedStreet(degrees, nodes, type, nameTag, oneway));
                            } else {
                                type = getWayType(wayTag);

                                if (type >= 0) {
                                    if (!tunnel || type != 2) {
                                        wayList.add(new adminTool.elements.Way(nodes, type, nameTag));
                                    }
                                }
                            }
                        }
                    }

                    if (amenityType >= 0) {
                        final Point center = calculateCenter(new Area(nodes, 0).getPolygon());
                        poiList.add(new POI(center.x, center.y, amenityType));
                    }

                }
            }
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

                switch (relationType) {
                    case "multipolygon":
                        parseMultipolygon(relation);
                        break;
                    case "boundary":
                        parseBoundary(relation);
                        break;
                }
            }
        }

        private void parseMultipolygon(final Relation relation) {
            int areaType = getRelationAreaType(relation);

            if (areaType != -1) {
                final List<HashMap<Node, List<Node>>> maps = createMultipolygonMaps(relation);

                if (areaType == Integer.MAX_VALUE) {
                    String housenumber = "";
                    String address = "";

                    for (int k = 0; k < relation.getKeysCount(); k++) {
                        switch (getStringById(relation.getKeys(k))) {
                            case "addr:street":
                                address = getStringById(relation.getVals(k));
                                break;
                            case "addr:housenumber":
                                housenumber = getStringById(relation.getVals(k));
                                break;
                        }
                    }

                    for (final List<Node> list : maps.get(0).values()) {
                        buildingList.add(new RevalidateableBuilding(list.toArray(new Node[list.size()]), address,
                                housenumber));
                    }
                    for (final List<Node> list : maps.get(1).values()) {
                        buildingList.add(new RevalidateableBuilding(list.toArray(new Node[list.size()]), "", ""));
                    }
                } else {
                    for (final List<Node> list : maps.get(0).values()) {
                        // if (list.get(0) == list.get(list.size() - 1)) {
                        areaList.add(new RevalidateableArea(list.toArray(new Node[list.size()]), areaType));
                        // }
                    }
                    for (final List<Node> list : maps.get(1).values()) {
                        // if (list.get(0) == list.get(list.size() - 1)) {
                        areaList.add(new RevalidateableArea(list.toArray(new Node[list.size()]), areaType));
                        // }
                    }
                }
            }
        }

        private void parseBoundary(final Relation relation) {
            boolean administrative = false;
            String name = "";
            int level = -1;

            for (int i = 0; i < relation.getKeysCount(); i++) {
                switch (getStringById(relation.getKeys(i))) {
                    case "boundary":
                        if (getStringById(relation.getVals(i)).equals("administrative")) {
                            administrative = true;
                        }
                        break;
                    case "name":
                        name = getStringById(relation.getVals(i));
                        break;
                    case "admin_level":
                        level = Integer.parseInt(getStringById(relation.getVals(i)));
                        break;
                }
            }

            if (administrative && level != -1) {
                final List<HashMap<Node, List<Node>>> maps = createMultipolygonMaps(relation);

                if (!maps.get(0).isEmpty()) {
                    final Node[][] outer = new Node[maps.get(0).size()][];
                    final Node[][] inner = new Node[maps.get(1).size()][];

                    int count = -1;
                    for (final Entry<Node, List<Node>> entry : maps.get(0).entrySet()) {
                        outer[++count] = entry.getValue().toArray(new Node[entry.getValue().size()]);
                    }

                    count = -1;
                    for (final Entry<Node, List<Node>> entry : maps.get(1).entrySet()) {
                        inner[++count] = entry.getValue().toArray(new Node[entry.getValue().size()]);
                    }

                    boundaries.get(level).add(new Boundary(name, outer, inner));
                }
            }
        }

        private List<HashMap<Node, List<Node>>> createMultipolygonMaps(final Relation relation) {
            long id = 0;

            final List<HashMap<Node, List<Node>>> maps = new ArrayList<HashMap<Node, List<Node>>>(2);
            maps.add(new HashMap<Node, List<Node>>());
            maps.add(new HashMap<Node, List<Node>>());

            for (int j = 0; j < relation.getRolesSidCount(); j++) {
                final String role = getStringById(relation.getRolesSid(j));
                id += relation.getMemids(j);

                if ((role.equals("inner") || role.equals("outer"))) {
                    if (wayMap.containsKey((int) id)) {
                        // TODO save as small parts of ways may
                        // reduce disc space
                        List<Node> my = new ArrayList<Node>(wayMap.get((int) id).length);
                        for (final Node node : wayMap.get((int) id)) {
                            my.add(node);
                        }

                        int index = role.equals("inner") ? 1 : 0;
                        final HashMap<Node, List<Node>> map = maps.get(index);
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
                                for (final ListIterator<Node> it = other.listIterator(other.size()); it.hasPrevious();) {
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

                        map.put(my.get(my.size() - 1), my);
                        map.put(my.get(0), my);
                    }
                }
            }

            return maps;
        }

        private int getRelationAreaType(final Relation relation) {

            for (int i = 0; i < relation.getKeysCount(); i++) {
                final String key = getStringById(relation.getKeys(i));
                final String value = getStringById(relation.getVals(i));

                if (key.equals("waterway") && value.equals("riverbank")) {
                    return getAreaType("water", true);
                } else if (key.equals("leisure") || key.equals("natural") || key.equals("landuse")
                        || key.equals("highway") || key.equals("amenity") || key.equals("tourism")) {
                    int type = getAreaType(value, true);
                    if (type != -1) {
                        return type;
                    }
                } else if (key.equals("building")) {
                    return Integer.MAX_VALUE;
                }
            }

            long id = 0;
            for (int j = 0; j < relation.getRolesSidCount(); j++) {
                final String role = getStringById(relation.getRolesSid(j));
                id += relation.getMemids(j);
                if (role.equals("outer")) {
                    Byte type = areaMap.get((int) id);
                    if (type != null) {
                        return type;
                    }
                }
            }

            return -1;
        }

        @Override
        public void complete() {
            calculateBounds(wayList);
            calculateBounds(streetList);
            calculateBounds(areaList);
            calculateBounds(buildingList);

            bBox.setBounds(bBox.x, bBox.y, bBox.width - bBox.x, bBox.height - bBox.y);

            updateNodes(nodeMap.values());
            updateNodes(poiList);
            updateNodes(labelList);

            for (final Area area : areaList) {
                ((RevalidateableArea) area).revalidate();
            }
            for (final Building building : buildingList) {
                ((RevalidateableBuilding) building).revalidate();
            }

            nodeMap = null;
            areaMap = null;
            wayMap = null;
        }

        private void updateNodes(final Collection<? extends Node> nodes) {
            for (final Node node : nodes) {
                node.setLocation(translateX(node.getX()) - bBox.x, translateY(node.getY()) - bBox.y);
            }
        }

        private void calculateBounds(final Collection<? extends MultiElement> collection) {
            for (final MultiElement element : collection) {
                for (final Node node : element) {

                    final int nodeX = translateX(node.getX());
                    final int nodeY = translateY(node.getY());

                    if (nodeX < bBox.x) {
                        bBox.x = nodeX;
                    } else if (nodeX > bBox.width) {
                        bBox.width = nodeX;
                    }
                    if (nodeY < bBox.y) {
                        bBox.y = nodeY;
                    } else if (nodeY > bBox.height) {
                        bBox.height = nodeY;
                    }
                }
            }
        }

        @Override
        protected void parse(final HeaderBlock header) {
            // Bounding box not usefull, cause it is not exact [for mercator
            // projection]
        }

        private final int translateX(final int x) {
            return (int) (getXCoord(parseLon(x)) * SHIFT);
        }

        private int translateY(final int y) {
            return (int) (getYCoord(parseLat(y)) * SHIFT);
        }
    }

    private int getStreetType(final String value) {

        if (value.equals("unclassified") || value.equals("residential") || value.equals("living_street")
                || value.equals("pedestrian")) {
            return 0;
        }
        if (value.equals("service")) {
            return 1;
        }
        if (value.equals("secondary") || value.equals("secondary_link")) {
            return 2;
        }
        if (value.equals("tertiary") || value.equals("tertiary_link")) {
            return 3;
        }
        if (value.equals("road")) {
            return 4;
        }
        if (value.equals("track")) {
            return 5;
        }
        if (value.equals("footway")) {
            return 6;
        }
        if (value.equals("cycleway")) {
            return 7;
        }
        if (value.equals("bridleway")) {
            return 8;
        }
        if (value.equals("path")) {
            return 9;
        }

        return -1;
    }

    private int getWayType(final String value) {

        if (value.equals("river")) {
            return 10;
        }
        if (value.equals("stream") || value.equals("canal")) {
            return 11;
        }
        if (value.equals("rail")) {
            return 12;
        }
        if (value.equals("tram") || value.equals("light_rail")) {
            return 13;
        }
        if (value.equals("primary")) {
            return 14;
        }
        if (value.equals("motorway")) {
            return 15;
        }
        if (value.equals("trunk")) {
            return 16;
        }
        if (value.equals("primary_link")) {
            return 17;
        }
        if (value.equals("motorway_link")) {
            return 18;
        }
        if (value.equals("trunk_link")) {
            return 19;
        }
        if (value.equals("career")) {
            return 20;
        }
        if (value.equals("steps")) {
            return 21;
        }
        if (value.equals("wall") || value.equals("fence") || value.equals("retaining_wall")
                || value.equals("city_wall")) {
            return 22;
        }
        if (value.equals("hedge")) {
            return 23;
        }

        return -1;

    }

    private int getAreaType(final String value, final boolean area) {
        if (value.equals("forest") || value.equals("woodland")) {
            return 0;
        }
        if (value.equals("wood")) {
            return 1;
        }
        if (value.equals("scrub")) {
            return 2;
        }
        if (value.equals("grass") || value.equals("meadow") || value.equals("orchard") || value.equals("grassland")
                || value.equals("village_green") || value.equals("vineyard") || value.equals("allotments")
                || value.equals("recreation_ground") || value.equals("garden")) {
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
        if (value.equals("park")) {
            return 8;
        }
        if (value.equals("retail") || value.equals("commercial")) {
            return 9;
        }
        if (value.equals("heath") || value.equals("fell")) {
            return 10;
        }
        if (value.equals("sand") || value.equals("beach")) {
            return 11;
        }
        if (value.equals("mud") || value.equals("scree") || value.equals("bare_rock") || value.equals("pier")) {
            return 12;
        }
        if (value.equals("quarry")) {
            return 13;
        }
        if (value.equals("cemetery")) {
            return 14;
        }
        if (value.equals("parking")) {
            return 15;
        }
        if (value.equals("pedestrian")) {
            return area ? 16 : -1;
        }
        if (value.equals("farmland") || value.equals("farmyard") || value.equals("farm")) {
            return 17;
        }
        if (value.equals("playground")) {
            return 18;
        }
        if (value.equals("pitch")) {
            return 19;
        }
        if (value.equals("sports_centre") || value.equals("stadium")) {
            return 20;
        }
        if (value.equals("track")) {
            return area ? 21 : -1;
        }
        if (value.equals("golf_course") || value.equals("miniature_golf")) {
            return 22;
        }
        if (value.equals("school") || value.equals("university") || value.equals("college")
                || value.equals("kindergarten")) {
            return 23;
        }
        if (value.equals("zoo")) {
            return 24;
        }

        return -1;

    }

    private int getAmenityType(final String amenity) {

        if (amenity.equals("viewpoint")) {
            return 0;
        }
        if (amenity.equals("school")) {
            return 1;
        }
        if (amenity.equals("library")) {
            return 2;
        }
        if (amenity.equals("hospital")) {
            return 3;
        }
        if (amenity.equals("bank")) {
            return 4;
        }
        if (amenity.equals("cinema")) {
            return 5;
        }
        if (amenity.equals("museum")) {
            return 6;
        }
        if (amenity.equals("theatre")) {
            return 7;
        }
        if (amenity.equals("courthouse")) {
            return 8;
        }
        if (amenity.equals("playground")) {
            return 9;
        }
        if (amenity.equals("restaurant")) {
            return 10;
        }
        if (amenity.equals("cafe")) {
            return 11;
        }
        if (amenity.equals("bar")) {
            return 12;
        }
        if (amenity.equals("parking")) {
            return 13;
        }
        if (amenity.equals("fuel")) {
            return 14;
        }

        return -1;
    }

    private int getPlaceType(final String place) {
        switch (place) {
        // case "continent":
        // return 0;
        // case "country":
        // return 1;
            case "state":
                return 2;
                // case "county":
                // return 3;
            case "city":
                return 4;
            case "town":
                return 5;
            case "village":
                return 6;
            case "neighbourhood":
                return 7;
            case "suburb":
                return 8;

                // case "hamlet":
                // return 9;
                //
                // case "region":
                // return 10;
                // case "province":
                // return 11;
                // case "district":
                // return 12;
                // case "municipality":
                // return 13;
                // case "borough":
                // return 14;
                // case "quarter":
                // return 15;
                // case "city_block":
                // return 16;
                // case "plot":
                // return 17;
                // case "isolated_dwelling":
                // return 18;

        }

        return -1;
    }

    /*
     * Umwandlung lat/lon zu mercator-Koordianten
     */
    public double getXCoord(final double lon) {
        return (lon / 180 + 1) / 2;
    }

    public double getYCoord(final double lat) {
        return (1 - Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360)) / Math.PI) / 2;
    }

    private Point calculateCenter(final Polygon poly) {
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

        return new Point((int) x, (int) y);
    }

    private static class RevalidateableArea extends Area {

        public RevalidateableArea(final Node[] nodes, final int type) {
            super(nodes, type);
        }

        public void revalidate() {
            polygon = null;
        }
    }

    private static class RevalidateableBuilding extends Building {

        private final String street;
        private final String number;

        public RevalidateableBuilding(final Node[] nodes, final String street, final String number) {
            super(nodes);
            this.street = street;
            this.number = number;
        }

        @Override
        public String getAddress() {
            return getStreet() + " " + getHouseNumber();
        }

        @Override
        public String getStreet() {
            return street;
        }

        @Override
        public String getHouseNumber() {
            return number;
        }

        @Override
        public StreetNode getStreetNode() {
            return null;
        }

        public void revalidate() {
            polygon = null;
        }

    }
}
