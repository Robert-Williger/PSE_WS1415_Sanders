import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import model.elements.Node;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;
import crosby.binary.file.BlockReaderAdapter;

public class TownTest {

    public TownTest() {
    }

    public void read(final File file) throws Exception {
        final InputStream input = new FileInputStream(file);
        final BlockReaderAdapter brad = new Parser();
        new BlockInputStream(input, brad).process();
    }

    private class Parser extends BinaryParser {
        private Map<Integer, Node> nodeMap;
        private Map<Integer, Node[]> wayMap;

        private List<List<Administration>> administrations;
        private List<Node[]> ways;
        private List<Integer> ids;

        public Parser() {
            nodeMap = new HashMap<Integer, Node>();
            wayMap = new HashMap<Integer, Node[]>();
            ways = new ArrayList<Node[]>();
            ids = new ArrayList<Integer>();
            administrations = new ArrayList<List<Administration>>(12);
            for (int i = 0; i < 12; i++) {
                administrations.add(new ArrayList<Administration>());
            }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            for (final crosby.binary.Osmformat.Node n : nodes) {
                final int x = (int) n.getLon();
                final int y = (int) n.getLat();

                nodeMap.put((int) n.getId(), new Node(x, y));
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

                final int x = (int) lastLon;
                final int y = (int) lastLat;

                nodeMap.put((int) lastId, new Node(x, y));
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            boolean way;
            boolean name;

            for (final Way w : ways) {
                // Tags
                way = false;
                name = false;
                for (int i = 0; i < w.getKeysCount(); i++) {

                    final String key = getStringById(w.getKeys(i));
                    final String value = getStringById(w.getVals(i));

                    switch (key) {
                        case "highway":
                            if (getStreetType(value) != -1) {
                                way = true;
                            }
                            break;
                        case "name":
                            name = true;
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
                    if (way && name) {
                        this.ids.add((int) w.getId());
                        this.ways.add(nodes);
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

                if (relationType.equals("boundary")) {
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

                    if (administrative) {
                        final List<List<Polygon>> polygons = new ArrayList<List<Polygon>>(2);
                        polygons.add(new ArrayList<Polygon>(1));
                        polygons.add(new ArrayList<Polygon>(0));

                        final List<HashMap<Node, List<Node>>> maps = processMultipolygon(relation);

                        for (final Entry<Node, List<Node>> entry : maps.get(0).entrySet()) {
                            appendPolygon(entry.getValue(), polygons.get(0));
                        }

                        for (final Entry<Node, List<Node>> entry : maps.get(1).entrySet()) {
                            appendPolygon(entry.getValue(), polygons.get(1));
                        }

                        if (!polygons.get(0).isEmpty()) {
                            if (level == -1) {
                                System.out.println(relation.getId());
                            } else {
                                administrations.get(level).add(
                                        new Administration(name, polygons.get(0), polygons.get(1)));
                            }
                        }
                    }
                }
            }
        }

        private List<HashMap<Node, List<Node>>> processMultipolygon(final Relation relation) {
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

        private void appendPolygon(final List<Node> my, final List<Polygon> polygons) {
            final int[] x = new int[my.size()];
            final int[] y = new int[x.length];
            int count = 0;
            for (final Node node : my) {
                x[count] = node.getX();
                y[count] = node.getY();
                ++count;
            }
            polygons.add(new Polygon(x, y, count));
        }

        @Override
        public void complete() {
            for (int index = 0; index < ways.size(); index++) {
                final Node[] nodes = ways.get(index);
                boolean found = false;
                for (int i = 0; i < 5; i++) {
                    for (final Administration administration : administrations.get(9 - i)) {
                        for (final Node node : nodes) {
                            if (administration.contains(node.getLocation())) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                }
            }

            nodeMap = null;
            wayMap = null;
            administrations = null;
        }

        @Override
        protected void parse(final HeaderBlock header) {
            // Bounding box not usefull, cause it is not exact
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

    }

    private static class Administration {
        private final String name;
        private final List<Polygon> inner;
        private final List<Polygon> outer;

        public Administration(final String name, final List<Polygon> outer, final List<Polygon> inner) {
            this.name = name;
            this.inner = inner;
            this.outer = outer;
        }

        public boolean contains(final Point point) {

            for (final Polygon innerP : inner) {
                if (innerP.contains(point)) {
                    return false;
                }
            }

            for (final Polygon outerP : outer) {
                if (outerP.contains(point)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static void main(String[] args) {
        try {
            new TownTest().read(new File("default.pbf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
