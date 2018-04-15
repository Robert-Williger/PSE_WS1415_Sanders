package adminTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Street;
import adminTool.elements.Way;
import util.IntList;

public class GraphWriter extends AbstractMapFileWriter {
    private int nodeIdCount;

    private HashMap<Integer, Integer> nodeIdMap;

    private List<WeightedEdge> edges;
    private final List<Street> streets;
    private Collection<Way> ways;
    private final NodeAccess nodes;

    public GraphWriter(final Collection<Way> ways, final NodeAccess nodes, final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.ways = ways;
        this.nodes = nodes;
        streets = new ArrayList<>();
    }

    public List<Street> getStreets() {
        return streets;
    }

    @Override
    public void write() throws IOException {
        final int onewayCount = createGraph();

        ways = null;
        nodeIdMap = null;

        writeGraph(onewayCount);

        edges = null;
    }

    private int createGraph() {
        final int[] nodeCounts = new int[nodes.size()];
        countNodes(nodeCounts);

        edges = new ArrayList<>();
        nodeIdMap = new HashMap<>();

        for (final Way way : ways) {
            if (way.isOneway())
                processWay(way, nodeCounts, 0x80000000); // msb set
        }
        final int onewayCount = edges.size();
        for (final Way way : ways) {
            if (!way.isOneway())
                processWay(way, nodeCounts, 0x00000000); // msb not set
        }

        return onewayCount;
    }

    private void countNodes(final int[] nodeCounts) {
        // Step one: count the appearance of every node in the given ways
        for (final Way s : ways) {
            for (int i = 0; i < s.size(); ++i) {
                ++nodeCounts[s.getNode(i)];
            }
        }
    }

    private void processWay(final Way way, final int[] nodeCounts, final int mask) {
        // Step two: get the cuttings of the way at intersections (and dead ends)
        final IntList intersections = getWayCuts(way, nodeCounts);

        // Step three: processing the cutting of the way

        int lastCut = 0;
        for (int i = 0; i < intersections.size(); i++) {
            final int currentCut = intersections.get(i);

            final int id1 = generateID(way.getNode(lastCut));
            final int id2 = generateID(way.getNode(currentCut));

            final int[] indices = new int[currentCut - lastCut + 1];

            double weight = 0;
            for (int j = lastCut; j < currentCut; j++) {
                weight += getWeight(nodes.getLat(way.getNode(j)), nodes.getLon(way.getNode(j)),
                        nodes.getLat(way.getNode(j + 1)), nodes.getLon(way.getNode(j + 1)));
            }

            for (int j = lastCut; j <= currentCut; j++) {
                indices[j - lastCut] = way.getNode(j);
            }

            edges.add(new WeightedEdge(id1, id2, (int) weight));
            final int id = mask | streets.size();
            streets.add(new Street(indices, way.getType(), way.getName(), id));

            lastCut = currentCut;
        }
    }

    /*
     * Cuts the given ways in a list of streets.
     */
    private IntList getWayCuts(final Way s, final int[] nodeCounts) {
        final IntList intersections = new IntList();

        // calculating the indexes of the intersections

        for (int i = 1; i < s.size() - 1; i++) {
            if (nodeCounts[s.getNode(i)] > 1) {
                intersections.add(i);
            }
        }
        intersections.add(s.size() - 1);

        return intersections;
    }

    /*
     * Generates and returns the unique id for the given node. If the id is already created, its id is returned.
     */
    private int generateID(final int node) {
        Integer ret = nodeIdMap.get(node);
        if (ret != null) {
            return ret;
        }
        nodeIdMap.put(node, nodeIdCount);
        return nodeIdCount++;
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
