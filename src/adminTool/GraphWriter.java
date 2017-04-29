package adminTool;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Node;
import adminTool.elements.Street;
import adminTool.elements.UnprocessedStreet;

public class GraphWriter extends AbstractMapFileWriter {
    private int nodeIdCount;

    private HashMap<Node, Integer> nodeIdMap;

    private List<WeightedEdge> edges;
    private final List<Street> processedStreets;
    private Collection<UnprocessedStreet> unprocessedStreets;

    public GraphWriter(final Collection<UnprocessedStreet> unprocessedStreets, final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.unprocessedStreets = unprocessedStreets;
        processedStreets = new ArrayList<>();
    }

    public Collection<Street> getStreets() {
        return processedStreets;
    }

    @Override
    public void write() throws IOException {
        final int oneways = createGraph();

        unprocessedStreets = null;
        nodeIdMap = null;

        writeGraph(oneways);

        edges = null;
    }

    private HashMap<Node, Integer> createNodeCountMap() {
        // Step one: count the appearance of every node in the given streets and
        final HashMap<Node, Integer> nodeMap = new HashMap<>();
        for (final UnprocessedStreet s : unprocessedStreets) {
            for (final Node n : s) {
                // Node not yet generated
                if (!nodeMap.containsKey(n)) {
                    nodeMap.put(n, 0);
                }

                incrementCount(nodeMap, n);
            }
        }
        return nodeMap;
    }

    private int createGraph() {
        final HashMap<Node, Integer> nodeCountMap = createNodeCountMap();

        edges = new ArrayList<>();
        nodeIdMap = new HashMap<>();

        for (final UnprocessedStreet street : unprocessedStreets) {
            if (street.isOneway()) {
                processStreet(street, nodeCountMap);
            }
        }
        final int oneways = edges.size();

        for (final UnprocessedStreet street : unprocessedStreets) {
            if (!street.isOneway()) {
                processStreet(street, nodeCountMap);
            }
        }

        final int edgeCount = edges.size();
        for (int i = 0; i < oneways; i++) {
            processedStreets.get(i).setID(0x80000000 | i);
        }
        for (int i = oneways; i < edgeCount; i++) {
            processedStreets.get(i).setID(i);
        }

        return oneways;
    }

    private void processStreet(final UnprocessedStreet street, final HashMap<Node, Integer> nodeCountMap) {
        // Step two: get the cuttings of the street at intersections (and dead
        // ends)
        final List<Integer> intersections = getStreetCuts(street, nodeCountMap);

        // Step three: processing the cutting of the streets.

        final Point2D[] degrees = street.getDegrees();
        final Node[] nodes = street.getNodes();

        int lastCut = 0;
        for (int i = 0; i < intersections.size(); i++) {
            final int currentCut = intersections.get(i);

            final int id1 = generateID(nodes[lastCut]);
            final int id2 = generateID(nodes[currentCut]);

            final Node[] streetNodes = new Node[currentCut - lastCut + 1];

            double weight = 0;

            for (int j = lastCut; j < currentCut; j++) {
                weight += getWeight(degrees[j].getY(), degrees[j].getX(), degrees[j + 1].getY(), degrees[j + 1].getX());
            }

            for (int j = lastCut; j <= currentCut; j++) {
                streetNodes[j - lastCut] = nodes[j];
            }

            edges.add(new WeightedEdge(id1, id2, (int) weight));
            processedStreets.add(new Street(streetNodes, street.getType(), street.getName()));

            lastCut = currentCut;
        }
    }

    private void writeGraph(final int oneways) throws IOException {
        putNextEntry("graph");

        dataOutput.writeInt(nodeIdCount);
        dataOutput.writeInt(edges.size());
        dataOutput.writeInt(oneways);

        for (final WeightedEdge edge : edges) {
            dataOutput.writeInt(edge.node1);
            dataOutput.writeInt(edge.node2);
            dataOutput.writeInt(edge.weight);
        }

        closeEntry();
    }

    /*
     * Cuts the given street in a list of streets.
     */
    private List<Integer> getStreetCuts(final UnprocessedStreet s, final HashMap<Node, Integer> nodeCountMap) {
        final List<Integer> intersections = new ArrayList<>();

        // calculating the indexes of the intersections
        final Node[] nodes = s.getNodes();

        for (int i = 1; i < nodes.length - 1; i++) {
            final Node node = nodes[i];

            if (nodeCountMap.get(node) > 1) {
                intersections.add(i);
            }
        }
        intersections.add(nodes.length - 1);

        return intersections;
    }

    private void incrementCount(final HashMap<Node, Integer> hm, final Node n) {

        int value = hm.get(n);
        value++;
        hm.put(n, value);

    }

    /*
     * Generates and returns the unique id for the given node. If the id is already created, its id is returned.
     */
    private int generateID(final Node parsedNode) {

        if (nodeIdMap.get(parsedNode) != null) {
            return nodeIdMap.get(parsedNode);
        }

        else {
            nodeIdMap.put(parsedNode, nodeIdCount);
            return nodeIdCount++;
        }
    }

    private static int getWeight(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double earthRadius = 6371;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLng = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double dist = (earthRadius * c);
        return (int) Math.round(dist * 1000); // in meter

    }

    private class WeightedEdge implements Comparable<WeightedEdge> {
        private final int node1;
        private final int node2;
        private final int weight;

        public WeightedEdge(final int node1, final int node2, final int weight) {
            this.node1 = node1;
            this.node2 = node2;
            this.weight = weight;
        }

        @Override
        public int compareTo(final WeightedEdge o) {
            return weight - o.weight;
        }
    }
}
