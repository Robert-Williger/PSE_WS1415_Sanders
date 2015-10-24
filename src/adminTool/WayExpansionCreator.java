package adminTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adminTool.elements.IWay;
import adminTool.elements.LinkedWay;
import adminTool.elements.Node;
import adminTool.elements.UnprocessedStreet;
import adminTool.elements.Way;

public class WayExpansionCreator extends AbstractMapCreator {

    private UnprocessedStreet[] streets;
    private Way[] ways;

    public WayExpansionCreator(final File file, final Collection<UnprocessedStreet> streets, final Collection<Way> ways) {
        super(file);
        this.streets = streets.toArray(new UnprocessedStreet[streets.size()]);
        this.ways = ways.toArray(new Way[ways.size()]);
    }

    @Override
    public void create() {
        HashMap<Node, Collection<Integer>> nodeMap = new HashMap<Node, Collection<Integer>>();

        for (int i = 0; i < streets.length; i++) {
            final Node[] nodes = streets[i].getNodes();
            fillNodeMap(nodeMap, nodes[0], i);
            fillNodeMap(nodeMap, nodes[nodes.length - 1], i);
        }

        Map<Integer, Collection<Integer>> streetExpansions = createExpansions(streets);
        Map<Integer, Collection<Integer>> wayExpansions = createExpansions(ways);
    }

    private Map<Integer, Collection<Integer>> createExpansions(final Way[] ways) {
        HashMap<Node, Collection<Integer>> nodeMap = new HashMap<Node, Collection<Integer>>();

        for (int i = 0; i < ways.length; i++) {
            final Node[] nodes = ways[i].getNodes();
            fillNodeMap(nodeMap, nodes[0], i);
            fillNodeMap(nodeMap, nodes[nodes.length - 1], i);
        }

        Map<Integer, Collection<Integer>> wayExpansions = new HashMap<Integer, Collection<Integer>>();

        for (final Collection<Integer> collection : nodeMap.values()) {
            if (collection.size() > 1) {
                HashMap<String, Integer> map = new HashMap<String, Integer>();
                for (final int id : collection) {
                    final UnprocessedStreet street = streets[id];
                    Integer matching = map.get(street.getName());
                    if (matching == null) {
                        map.put(street.getName(), id);
                    } else {
                        // TODO consider case of street forking
                        Collection<Integer> firstExpansion = wayExpansions.get(matching);
                        if (firstExpansion == null) {
                            firstExpansion = wayExpansions.get(id);
                            if (firstExpansion == null) {
                                firstExpansion = new LinkedList<Integer>();
                                fillExpansions(wayExpansions, firstExpansion, id);
                                fillExpansions(wayExpansions, firstExpansion, matching);
                            } else {
                                fillExpansions(wayExpansions, firstExpansion, id);
                            }
                        } else {
                            Collection<Integer> secondExpansion = wayExpansions.get(matching);
                            if (secondExpansion == null) {
                                fillExpansions(wayExpansions, firstExpansion, matching);
                            } else {
                                firstExpansion.addAll(secondExpansion);
                                for (final int other : secondExpansion) {
                                    wayExpansions.put(other, firstExpansion);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<Integer, IWay> wayMap = new HashMap<Integer, IWay>();
        Map<Integer, LinkedWay> firstNodeMap = new HashMap<Integer, LinkedWay>();
        
        for (final Entry<Node, Collection<Integer>> entry : nodeMap.entrySet()) {
            // check dead end
            if (entry.getValue().size() > 1) {
                final Node crossing = entry.getKey();
                HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
                for (final int id : entry.getValue()) {
                    final UnprocessedStreet street = streets[id];
                    // TODO check way type?!
                    List<Integer> matching = map.get(street.getName());
                    if (matching == null) {
                        matching = new ArrayList<Integer>(3);
                        map.put(street.getName(), matching);
                    }
                    matching.add(id);
                }
                for (final List<Integer> list : map.values()) {
                    // Merge, if street not forked
                    if (list.size() == 2) {
                        final LinkedWay mergedWay = new LinkedWay();
                        Way way = ways[list.get(0)];
                        boolean forward = way.getNodes()[0] == crossing;
                        mergedWay.addFirst(way, forward);

                        way = ways[list.get(1)];
                        forward = way.getNodes()[0] != crossing;
                        mergedWay.addFirst(way, forward);
                    }
                }
            }
        }

        return wayExpansions;
    }

    private void fillExpansions(final Map<Integer, Collection<Integer>> wayExpansions,
            final Collection<Integer> wayExpansion, final int id) {
        wayExpansion.add(id);
        wayExpansions.put(id, wayExpansion);
    }

    private void fillNodeMap(final HashMap<Node, Collection<Integer>> nodeMap, final Node node, final int index) {
        Collection<Integer> streets = nodeMap.get(node);
        if (streets == null) {
            streets = new LinkedList<Integer>();
            nodeMap.put(node, streets);
        }
        streets.add(index);
    }

}
