package adminTool;

import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import model.elements.Node;
import model.elements.Street;

public class GraphCreator extends AbstractTSKCreator {

    private final File file;
    private DataOutputStream stream;

    // Maps Node to the amount of streets its part of
    private HashMap<Node, Integer> nodeCount;

    // ID's
    private int idCount;
    private HashMap<Node, Integer> idMap;

    private List<Long> edgesList;
    private List<Integer> weightsList;
    private final List<Street> processedStreets;
    private Collection<UnprocessedStreet> unprocessedStreets;

    public GraphCreator(final Collection<UnprocessedStreet> unprocessedStreets, final File file) {

        this.file = file;
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
        edgesList = new ArrayList<>();
        weightsList = new ArrayList<>();

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

            final List<Point2D.Double> degrees = street.getDegrees();
            final List<Node> nodeList = street.getNodes();

            int lastCut = 0;
            for (int i = 0; i < intersections.size(); i++) {
                final int currentCut = intersections.get(i);

                final int id1 = generateID(nodeList.get(lastCut));
                final int id2 = generateID(nodeList.get(currentCut));

                final List<Node> streetNodes;
                final long id;
                if (id1 > id2) {
                    id = ((long) id2 << 32) | id1;
                    streetNodes = new ArrayList<Node>();
                    for (int j = currentCut; j >= lastCut; j--) {
                        streetNodes.add(nodeList.get(j));
                    }
                } else {
                    id = ((long) id1 << 32) | id2;
                    streetNodes = nodeList.subList(lastCut, currentCut + 1);
                }

                final Street newStreet = new Street(streetNodes, street.getType(), street.getName(), id);

                processedStreets.add(newStreet);
                edgesList.add(newStreet.getID());

                double weight = 0;
                for (int j = lastCut; j < currentCut; j++) {
                    weight += getWeight(degrees.get(j).getY(), degrees.get(j).getX(), degrees.get(j + 1).getY(),
                            degrees.get(j + 1).getX());
                }

                weightsList.add((int) weight);

                lastCut = currentCut;
            }
        }

        unprocessedStreets = null;
        idMap = null;
        nodeCount = null;

        try {
            stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

            stream.writeInt(idCount);
            stream.writeInt(edgesList.size());

            for (final Long id : edgesList) {
                stream.writeLong(id);
            }

            for (final Integer weight : weightsList) {
                stream.writeInt(weight);
            }

            stream.close();
        } catch (final IOException e) {
        }

        edgesList = null;
        weightsList = null;
    }

    /*
     * Cuts the given street in a list of streets.
     */
    private List<Integer> getStreetCuts(final UnprocessedStreet s) {
        final List<Integer> intersections = new ArrayList<Integer>();

        // calculating the indexes of the intersections
        final List<Node> nodes = s.getNodes();

        for (int i = 1; i < s.getNodes().size() - 1; i++) {
            final Node node = nodes.get(i);

            if (nodeCount.get(node) > 1) {
                intersections.add(i);
            }
        }
        intersections.add(nodes.size() - 1);

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

    private static int getWeight(final double lat1, final double lng1, final double lat2, final double lng2) {

        final double earthRadius = 6371;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLng = Math.toRadians(lng2 - lng1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double dist = (earthRadius * c);

        return (int) (dist * 1000); // in meter
    }
}
