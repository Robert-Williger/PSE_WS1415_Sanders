package adminTool;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adminTool.elements.Boundary;
import adminTool.elements.Building;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.Way;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.file.BlockInputStream;
import util.IntList;

public class OSMParser implements IOSMParser {

    private final Collection<Way> wayList;
    private final Collection<MultiElement> areaList;
    private final Collection<POI> poiList;
    private final Collection<Building> buildingList;
    private final Collection<Label> labelList;
    private final List<List<Boundary>> boundaries;
    private NodeAccess nodes;

    public OSMParser() {
        // TODO lists instead of sets!
        labelList = new ArrayList<>();
        wayList = new ArrayList<>();
        areaList = new ArrayList<>();
        poiList = new ArrayList<>();
        buildingList = new ArrayList<>();

        boundaries = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            boundaries.add(new ArrayList<Boundary>());
        }
    }

    @Override
    public void read(final File file) throws Exception {
        new BlockInputStream(new FileInputStream(file), new Preprocessor()).process();
        new BlockInputStream(new FileInputStream(file), new Parser()).process();
    }

    @Override
    public Collection<Way> getWays() {
        return wayList;
    }

    @Override
    public Collection<MultiElement> getTerrain() {
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
    public Collection<Label> getLabels() {
        return labelList;
    }

    @Override
    public List<List<Boundary>> getBoundaries() {
        return boundaries;
    }

    @Override
    public NodeAccess getNodes() {
        return nodes;
    }

    private class Preprocessor extends BinaryParser {
        private int nodeCount;

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            nodeCount += nodes.size();
        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            nodeCount += nodes.getIdCount();
        }

        @Override
        protected void parseWays(final List<crosby.binary.Osmformat.Way> ways) {
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {
        }

        @Override
        public void complete() {
            nodes = new NodeAccess(nodeCount);
        }

        @Override
        protected void parse(final HeaderBlock header) {
        }
    }

    private class Parser extends BinaryParser {
        private int nodeId;
        private Map<Long, Integer> nodeMap;
        private Map<Integer, Byte> areaMap;
        private Map<Integer, int[]> wayMap;

        public Parser() {
            nodeMap = new HashMap<>();
            wayMap = new HashMap<>();
            areaMap = new HashMap<>();
            nodeId = 0;
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodeList) {
            for (final crosby.binary.Osmformat.Node n : nodeList) {
                int id = nodeId++;
                nodes.set(id, parseLat(n.getLat()), parseLon(n.getLon()));
                nodeMap.put(n.getId(), id);
                for (int i = 0; i < n.getKeysCount(); i++) {
                    final String key = getStringById(n.getKeys(i));
                    final String value = getStringById(n.getVals(i));

                    if (key.equals("amenity") || key.equals("tourism")) {
                        final int type = getAmenityType(value);
                        if (type >= 0) {
                            poiList.add(new POI(id, type));
                        }
                    }
                }
            }
        }

        @Override
        protected void parseDense(final DenseNodes dNodes) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;

            String tag;

            final Iterator<Integer> iterator = dNodes.getKeysValsList().iterator();

            for (int i = 0; i < dNodes.getIdCount(); i++) {
                tag = "";

                lastId += dNodes.getId(i);
                lastLat += dNodes.getLat(i);
                lastLon += dNodes.getLon(i);
                int id = nodeId++;
                nodes.set(id, parseLat(lastLat), parseLon(lastLon));
                nodeMap.put(lastId, id);

                int keyValId;
                while ((keyValId = iterator.next()) != 0) {
                    final String key = getStringById(keyValId);
                    final String value = getStringById(iterator.next());

                    switch (key) {
                        case "amenity":
                        case "tourism:":
                            int type = getAmenityType(value);
                            if (type >= 0) {
                                poiList.add(new POI(id, type));
                            }
                            break;
                        case "place":
                            if (tag.isEmpty()) {
                                tag = value;
                            } else {
                                type = getPlaceType(value);
                                if (type != -1) {
                                    labelList.add(Label.create(id, type, tag));
                                }
                            }
                            break;
                        case "name":
                            if (tag.isEmpty()) {
                                tag = value;
                            } else {
                                type = getPlaceType(tag);
                                if (type != -1) {
                                    labelList.add(Label.create(id, type, value));
                                }
                            }
                            break;
                    }
                }
            }
        }

        @Override
        protected void parseWays(final List<crosby.binary.Osmformat.Way> ways) {
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

            for (final crosby.binary.Osmformat.Way w : ways) {
                // Tags
                nameTag = null;
                buildingTag = false;
                wayTag = "";
                terrainType = -1;
                addrStreetTag = null;
                addrNumberTag = null;
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

                final int[] nodes = new int[w.getRefsList().size()];
                long lastRef = 0;
                int count = 0;
                for (final Long ref : w.getRefsList()) {
                    lastRef += ref;
                    nodes[count++] = nodeMap.get(lastRef);
                }

                if (nodes.length > 1) {
                    wayMap.put((int) w.getId(), nodes);
                    int type;

                    if (buildingTag) {
                        // TODO check behaviour
                        buildingList.add(Building.create(nodes, addrStreetTag, addrNumberTag, nameTag));
                    } else {
                        if (terrainType >= 0) {
                            areaMap.put((int) w.getId(), (byte) terrainType);
                            // TODO equals instead of == ?
                            if (nodes[0] == nodes[nodes.length - 1]) {
                                areaList.add(new MultiElement(nodes, terrainType));
                            }
                        }

                        if (!areaTag) {
                            // TODO ways with area tag also valid...
                            type = getWayType(wayTag, bicycle, foot);
                            if (type >= 0 && (!tunnel || type != 2)) {
                                boolean oneway = onewayTag.equals("-1") || onewayTag.equals("yes");
                                wayList.add(new Way(nodes, type, nameTag, oneway));
                            }
                        }
                    }

                    if (amenityType >= 0) {
                        /*
                         * final Point center = calculateCenter(nodes); poiList.add(new NodePOI(center.x, center.y,
                         * amenityType));
                         */
                        // TODO fix this
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
                final List<HashMap<Integer, IntList>> maps = createMultipolygonMaps(relation);

                if (areaType == Integer.MAX_VALUE) {
                    String housenumber = null;
                    String address = null;
                    String name = null;

                    for (int k = 0; k < relation.getKeysCount(); k++) {
                        switch (getStringById(relation.getKeys(k))) {
                            case "addr:street":
                                address = getStringById(relation.getVals(k));
                                break;
                            case "addr:housenumber":
                                housenumber = getStringById(relation.getVals(k));
                                break;
                            case "name":
                                name = getStringById(relation.getVals(k));
                                break;
                        }
                    }

                    for (final IntList list : maps.get(0).values()) {
                        buildingList.add(Building.create(list.toArray(), address, housenumber, name));
                    }
                    for (final IntList list : maps.get(1).values()) {
                        buildingList.add(Building.create(list.toArray()));
                    }
                } else {
                    for (final IntList list : maps.get(0).values()) {
                        // if (list.get(0) == list.get(list.size() - 1)) {
                        areaList.add(new MultiElement(list.toArray(), areaType));
                        // }
                    }
                    for (final IntList list : maps.get(1).values()) {
                        // if (list.get(0) == list.get(list.size() - 1)) {
                        areaList.add(new MultiElement(list.toArray(), areaType));
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
                final List<HashMap<Integer, IntList>> maps = createMultipolygonMaps(relation);

                if (!maps.get(0).isEmpty()) {
                    final int[][] outer = new int[maps.get(0).size()][];
                    final int[][] inner = new int[maps.get(1).size()][];

                    int count = -1;
                    for (final Entry<Integer, IntList> entry : maps.get(0).entrySet()) {
                        outer[++count] = entry.getValue().toArray();
                    }

                    count = -1;
                    for (final Entry<Integer, IntList> entry : maps.get(1).entrySet()) {
                        inner[++count] = entry.getValue().toArray();
                    }

                    boundaries.get(level).add(new Boundary(name, outer, inner));
                }
            }
        }

        private List<HashMap<Integer, IntList>> createMultipolygonMaps(final Relation relation) {
            long id = 0;

            final List<HashMap<Integer, IntList>> maps = new ArrayList<>(2);
            maps.add(new HashMap<Integer, IntList>());
            maps.add(new HashMap<Integer, IntList>());

            for (int j = 0; j < relation.getRolesSidCount(); j++) {
                final String role = getStringById(relation.getRolesSid(j));
                id += relation.getMemids(j);

                if ((role.equals("inner") || role.equals("outer"))) {
                    if (wayMap.containsKey((int) id)) {
                        // TODO save as small parts of ways may
                        // reduce disc space
                        IntList my = new IntList(wayMap.get((int) id).length);
                        for (final int node : wayMap.get((int) id)) {
                            my.add(node);
                        }

                        int index = role.equals("inner") ? 1 : 0;
                        final HashMap<Integer, IntList> map = maps.get(index);
                        final int myFirst = my.get(0);
                        final int myLast = my.get(my.size() - 1);

                        IntList other = map.get(myLast);
                        if (other != null) {
                            map.remove(other.get(0));
                            map.remove(other.get(other.size() - 1));

                            if (other.get(0) == myLast) {
                                for (int i = 1; i < other.size(); ++i) {
                                    my.add(other.get(i));
                                }
                            } else {
                                for (int i = other.size() - 2; i >= 0; --i) {
                                    my.add(other.get(i));
                                }
                            }
                        }

                        other = map.get(myFirst);
                        if (other != null) {
                            map.remove(other.get(0));
                            map.remove(other.get(other.size() - 1));

                            if (other.get(0) == myFirst) {
                                my.reverse();
                                for (int i = 1; i < other.size(); ++i) {
                                    my.add(other.get(i));
                                }
                            } else {
                                for (int i = 1; i < my.size(); ++i) {
                                    other.add(my.get(i));
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
            nodeMap = null;
            areaMap = null;
            wayMap = null;
        }

        @Override
        protected void parse(final HeaderBlock header) {
            // Bounding box not usefull, cause it is not exact [for mercator
            // projection]
        }

        /*
         * private Point calculateCenter(final int[] nodes) { float x = 0f; float y = 0f; int totalPoints = nodes.length
         * - 1; for (int i = 0; i < totalPoints; i++) { x += lons[i]; y += nodes[i].getLat(); }
         * 
         * if (nodes[0].getLon() != nodes[totalPoints].getLon() || nodes[0].getLat() != nodes[totalPoints].getLat()) { x
         * += nodes[totalPoints].getLon(); y += nodes[totalPoints].getLat(); ++totalPoints; }
         * 
         * x = x / totalPoints; y = y / totalPoints;
         * 
         * return new Point((int) x, (int) y); }
         */
    }

    private int getWayType(final String value, final boolean bicycle, final boolean foot) {
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
            if (bicycle) {
                return 7; // cycleway
            }
            if (foot) {
                return 6; // footway
            }
            return 9;
        }
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
}
