package adminTool;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.Node;
import adminTool.elements.Street;
import adminTool.elements.UnprocessedStreet;

public class GraphCreator extends AbstractMapCreator {

    // Maps Node to the amount of streets its part of
    private HashMap<Node, Integer> nodeCount;

    // ID's
    private int idCount;
    private HashMap<Node, Integer> idMap;

    private List<WeightedEdge> edgesList;
    private final List<Street> processedStreets;
    private Collection<UnprocessedStreet> unprocessedStreets;

    public GraphCreator(final Collection<UnprocessedStreet> unprocessedStreets, final File file) {
        super(file);
        this.unprocessedStreets = unprocessedStreets;

        processedStreets = new ArrayList<Street>();
    }

    public Collection<Street> getStreets() {

        return processedStreets;
    }

    @Override
    public void create() {

        idCount = 0;
        idMap = new HashMap<Node, Integer>();
        nodeCount = new HashMap<Node, Integer>();

        edgesList = new ArrayList<WeightedEdge>();

        // Step one: count the appearance of every node in the given streets and
        for (final UnprocessedStreet s : unprocessedStreets) {

            for (final Node n : s.getNodes()) {

                // Node not yet generated
                if (!nodeCount.containsKey(n)) {
                    nodeCount.put(n, 0);
                }

                incrementCount(nodeCount, n);

            }

        }

        // Step two: get the cuttings of the street at intersections (and dead
        // ends)
        for (final UnprocessedStreet street : unprocessedStreets) {

            final List<Integer> intersections = getStreetCuts(street);
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
                    weight += getWeight(degrees[j].getY(), degrees[j].getX(), degrees[j + 1].getY(),
                            degrees[j + 1].getX());
                }

                final WeightedEdge edge;
                final long id;
                if (id1 > id2) {
                    edge = new WeightedEdge(id2, id1, (int) weight);
                    id = ((long) id2 << 32) | id1;
                    for (int j = currentCut; j >= lastCut; j--) {
                        streetNodes[currentCut - j] = nodes[j];
                    }
                } else {
                    edge = new WeightedEdge(id1, id2, (int) weight);
                    id = ((long) id1 << 32) | id2;
                    for (int j = lastCut; j <= currentCut; j++) {
                        streetNodes[j - lastCut] = nodes[j];
                    }
                }

                final Street newStreet = new Street(streetNodes, street.getType(), street.getName(), id);

                processedStreets.add(newStreet);
                edgesList.add(edge);

                lastCut = currentCut;
            }
        }

        unprocessedStreets = null;
        idMap = null;
        nodeCount = null;

        Collections.sort(edgesList);

        try {
            createOutputStream(false);

            writeCompressedInt(idCount);
            writeCompressedInt(edgesList.size());

            int lastWeight = 0;
            for (final WeightedEdge edge : edgesList) {
                writeCompressedInt(edge.node1);
                writeCompressedInt(edge.node2 - edge.node1);
                writeCompressedInt(edge.weight - lastWeight);
                lastWeight = edge.weight;
            }

            stream.close();
        } catch (final IOException e) {
        }

        edgesList = null;
    }

    /*
     * Cuts the given street in a list of streets.
     */
    private List<Integer> getStreetCuts(final UnprocessedStreet s) {
        final List<Integer> intersections = new ArrayList<Integer>();

        // calculating the indexes of the intersections
        final Node[] nodes = s.getNodes();

        for (int i = 1; i < nodes.length - 1; i++) {
            final Node node = nodes[i];

            if (nodeCount.get(node) > 1) {
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
     * Generates and returns the unique id for the given node. If the id is
     * already created, its id is returned.
     */
    private int generateID(final Node parsedNode) {

        if (idMap.get(parsedNode) != null) {
            return idMap.get(parsedNode);
        }

        else {
            idMap.put(parsedNode, idCount);
            return idCount++;
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
