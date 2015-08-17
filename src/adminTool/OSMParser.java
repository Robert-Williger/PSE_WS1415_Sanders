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
import crosby.binary.file.BlockReaderAdapter;

public class OSMParser implements IOSMParser {

    private final Collection<model.elements.Way> wayList;
    private final Collection<UnprocessedStreet> streetList;
    private final Collection<Area> areaList;
    private final Collection<POI> poiList;
    private final Collection<Building> buildingList;
    private final Rectangle bBox;

    public OSMParser() {
        wayList = new HashSet<model.elements.Way>();
        streetList = new HashSet<UnprocessedStreet>();
        areaList = new HashSet<Area>();
        poiList = new HashSet<POI>();
        buildingList = new HashSet<Building>();
        bBox = new Rectangle();
    }

    @Override
    public void read(final File file) throws Exception {
        final InputStream input = new FileInputStream(file);
        final BlockReaderAdapter brad = new Parser();
        new BlockInputStream(input, brad).process();
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

    private class Parser extends BinaryParser {
        private HashMap<Integer, Node> nodeMap;
        private HashMap<Integer, Byte> areaMap;
        private HashMap<Integer, List<Node>> wayMap;

        private final int SHIFT = 1 << 28;
        private int xOffset;
        private int yOffset;

        public Parser() {
            nodeMap = new HashMap<Integer, Node>();
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

                    if (key.equals("amenity") || key.equals("tourism")) {
                        final int type = getAmenityType(value);
                        if (type >= 0) {
                            final int poiX = (int) (getXCoord(parseLon(x)) * SHIFT) - xOffset;
                            final int poiY = (int) Math.abs((getYCoord(parseLat(y)) * SHIFT) - yOffset);
                            poiList.add(new POI(poiX, poiY, type));
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

                    if (key.equals("amenity") || key.equals("tourism")) {
                        final int type = getAmenityType(value);
                        if (type >= 0) {
                            final int poiX = (int) (getXCoord(parseLon(x)) * SHIFT) - xOffset;
                            final int poiY = (int) Math.abs((getYCoord(parseLat(y)) * SHIFT) - yOffset);
                            poiList.add(new POI(poiX, poiY, type));
                        }
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
            String buildingTag;
            String terrainTag;
            String amenityTag;

            boolean bicycle;
            boolean foot;
            boolean tunnel;
            boolean area;

            for (final Way w : ways) {
                // Tags
                nameTag = "";
                buildingTag = "";
                wayTag = "";
                terrainTag = "";
                addrStreetTag = "";
                addrNumberTag = "";
                amenityTag = "";
                foot = false;
                bicycle = false;
                tunnel = false;
                area = false;

                for (int i = 0; i < w.getKeysCount(); i++) {

                    final String key = getStringById(w.getKeys(i));
                    final String value = getStringById(w.getVals(i));

                    switch (key) {
                        case "leisure":
                            if (value.equals("track")) {
                                wayTag = "career";
                            }
                        case "amenity":
                        case "tourism":
                            if (getAmenityType(value) >= 0) {
                                amenityTag = value;
                            }
                        case "landuse":
                        case "natural":
                        case "man_made":
                            if (terrainTag.isEmpty() && getAreaType(value, true) >= 0) {
                                terrainTag = value;
                            }
                            break;
                        case "name":
                            nameTag = value;
                            break;
                        case "highway":
                            if (value.equals("pedestrian")) {
                                terrainTag = value;
                            }
                        case "railway":
                            wayTag = value;
                            break;
                        case "building":
                            buildingTag = value;
                            break;
                        case "waterway":
                            if (value.equals("riverbank")) {
                                terrainTag = "water";
                            } else {
                                wayTag = value;
                            }
                            break;
                        case "addr:street":
                            addrStreetTag = value;
                            break;
                        case "addr:housenumber":
                            addrNumberTag = value;
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
                                area = true;
                            }
                            break;
                        case "barrier":
                            wayTag = value;
                            break;
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
                    int type;

                    if (!buildingTag.isEmpty()) {
                        buildingList.add(new Building(nodes, (addrStreetTag + " " + addrNumberTag), null));
                    } else {
                        type = getAreaType(terrainTag, area);

                        if (type >= 0) {
                            areaMap.put((int) w.getId(), (byte) type);
                            if (nodes.get(0).equals(nodes.get(nodes.size() - 1))) {
                                areaList.add(new Area(nodes, type));
                            }
                        }

                        if (!area) {
                            type = getStreetType(wayTag);
                            if (type >= 0) {
                                final List<Point2D.Double> degrees = new LinkedList<Point2D.Double>();
                                for (final Node node : nodes) {
                                    final double lat = parseLat(node.getY());
                                    final double lon = parseLon(node.getX());

                                    degrees.add(new Point2D.Double(lat, lon));
                                }

                                if (wayTag.equals("path")) {
                                    if (bicycle) {
                                        type = getStreetType("cycleway");
                                    } else if (foot) {
                                        type = getStreetType("footway");
                                    }
                                }

                                streetList.add(new UnprocessedStreet(degrees, nodes, type, nameTag));
                            } else {
                                type = getWayType(wayTag);

                                if (type >= 0) {
                                    if (!tunnel || type != 2) {
                                        wayList.add(new model.elements.Way(nodes, type, nameTag));
                                    }
                                }
                            }
                        }
                    }

                    type = getAmenityType(amenityTag);
                    if (type >= 0) {
                        final Point center = calculateCenter(new Area(nodes, 0).getPolygon());
                        final int poiX = (int) (getXCoord(parseLon(center.x)) * SHIFT) - xOffset;
                        final int poiY = (int) Math.abs((getYCoord(parseLat(center.y)) * SHIFT) - yOffset);
                        poiList.add(new POI(poiX, poiY, type));
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

                if (relationType.equals("multipolygon")) {
                    int areaType = -1;

                    for (int i = 0; i < relation.getKeysCount(); i++) {
                        final String key = getStringById(relation.getKeys(i));
                        final String value = getStringById(relation.getVals(i));

                        if (key.equals("waterway") && value.equals("riverbank")) {
                            areaType = 6;
                            break;
                        } else if (key.equals("leisure") || key.equals("natural") || key.equals("landuse")
                                || key.equals("highway") || key.equals("amenity") || key.equals("tourism")) {
                            areaType = getAreaType(value, true);
                            if (areaType != -1) {
                                break;
                            }
                        } else if (key.equals("building")) {
                            areaType = Integer.MAX_VALUE;
                            break;
                        }
                    }

                    if (areaType == -1) {
                        long id = 0;
                        for (int j = 0; j < relation.getRolesSidCount(); j++) {
                            final String role = getStringById(relation.getRolesSid(j));
                            id += relation.getMemids(j);
                            if (role.equals("outer")) {
                                if (areaMap.containsKey((int) id)) {
                                    areaType = areaMap.get((int) id);
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
                                if (wayMap.containsKey((int) id)) {
                                    List<Node> my = new ArrayList<Node>(wayMap.get((int) id));

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

                                    if (my.get(0).equals(my.get(my.size() - 1)) && areaType != Integer.MAX_VALUE) {
                                        areaList.add(new Area(my, areaType));
                                    } else {
                                        map.put(my.get(my.size() - 1), my);
                                        map.put(my.get(0), my);
                                    }
                                }
                            }
                        }

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
                                buildingList.add(new Building(list, address + " " + housenumber, null));
                            }
                            for (final List<Node> list : maps.get(1).values()) {
                                buildingList.add(new Building(list, " ", null));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void complete() {
            for (final Node node : nodeMap.values()) {
                final double lat = parseLat(node.getY());
                final double lon = parseLon(node.getX());

                final int x = (int) (getXCoord(lon) * SHIFT) - xOffset;
                final int y = (int) Math.abs((getYCoord(lat) * SHIFT) - yOffset);

                if (x > bBox.width) {
                    bBox.setSize(x, bBox.height);
                }
                if (y > bBox.height) {
                    bBox.setSize(bBox.width, y);
                }

                node.setLocation(x, y);
            }

            nodeMap = null;
            areaMap = null;
            wayMap = null;
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
        if (value.equals("forest") || value.equals("woodland") || value.equals("scrub")) {
            return 0;
        }
        if (value.equals("wood")) {
            return 1;
        }
        if (value.equals("grass") || value.equals("meadow") || value.equals("orchard") || value.equals("grassland")
                || value.equals("village_green") || value.equals("vineyard") || value.equals("allotments")
                || value.equals("recreation_ground") || value.equals("garden")) {
            return 2;
        }
        if (value.equals("greenfield") || value.equals("landfill") || value.equals("brownfield")
                || value.equals("construction") || value.equals("paddock")) {
            return 3;
        }
        if (value.equals("residential") || value.equals("railway") || value.equals("garages") || value.equals("garage")
                || value.equals("traffic_island") || value.equals("road") || value.equals("plaza")) {
            return 4;
        }
        if (value.equals("water") || value.equals("reservoir") || value.equals("basin")) {
            return 5;
        }
        if (value.equals("industrial")) {
            return 6;
        }
        if (value.equals("park")) {
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
        if (value.equals("mud") || value.equals("scree") || value.equals("bare_rock") || value.equals("pier")) {
            return 11;
        }
        if (value.equals("quarry")) {
            return 12;
        }
        if (value.equals("cemetery")) {
            return 13;
        }
        if (value.equals("parking")) {
            return 14;
        }
        if (value.equals("pedestrian")) {
            return area ? 15 : -1;
        }
        if (value.equals("farmland") || value.equals("farmyard") || value.equals("farm")) {
            return 16;
        }
        if (value.equals("playground")) {
            return 17;
        }
        if (value.equals("pitch")) {
            return 18;
        }
        if (value.equals("sports_centre") || value.equals("stadium")) {
            return 19;
        }
        if (value.equals("track")) {
            return area ? 20 : -1;
        }
        if (value.equals("golf_course") || value.equals("miniature_golf")) {
            return 21;
        }
        if (value.equals("school") || value.equals("university") || value.equals("college")
                || value.equals("kindergarten")) {
            return 22;
        }
        if (value.equals("zoo")) {
            return 23;
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

    /*
     * Umwandlung lat/lon zu mercator-Koordianten
     */
    public double getXCoord(final double lon) {
        return lon / 180;
    }

    public double getYCoord(final double lat) {
        return Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360)) / Math.PI;
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

}
