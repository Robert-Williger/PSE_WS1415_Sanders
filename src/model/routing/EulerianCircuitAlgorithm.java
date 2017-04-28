package model.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

public class EulerianCircuitAlgorithm {

    private List<Map<Integer, Integer>> availableNodes;
    private int                         availableEdges;
    private IUndirectedGraph            undirectedGraph;

    public List<Integer> getEulerianCurcuit(final IUndirectedGraph undirectedGraph) {
        return getEulerianCurcuit(undirectedGraph, 0);
    }

    public List<Integer> getEulerianCurcuit(final IUndirectedGraph undirectedGraph, final int startNode) {
        this.undirectedGraph = undirectedGraph;
        initialize();

        final LinkedNode head = new LinkedNode(startNode);
        head.next = head;

        LinkedNode current = head;
        while (availableEdges > 0) {
            int toNode;
            while ((toNode = getNextNode(current.node)) == -1) {
                current = current.next;
            }
            findCycle(current, toNode);
        }

        return createFinalPath(head);
    }

    private void initialize() {
        availableNodes = new ArrayList<>(undirectedGraph.getNodes());
        for (int node = 0; node < undirectedGraph.getNodes(); node++) {
            final Map<Integer, Integer> adjacentNodes = new HashMap<>();
            for (final OfInt iterator = undirectedGraph.getAdjacentNodes(node); iterator.hasNext();) {

                final int other = iterator.nextInt();
                Integer occurances = adjacentNodes.get(other);
                if (occurances == null) {
                    adjacentNodes.put(other, 1);
                } else {
                    adjacentNodes.put(other, occurances + 1);
                }

                if (other < node) {
                    ++availableEdges;
                }
            }
            availableNodes.add(adjacentNodes);
        }
    }

    private void findCycle(final LinkedNode start, final int nextNode) {
        final LinkedNode oldNext = start.next;

        int target = start.node;
        int currentNode = nextNode;
        LinkedNode currentItem = start;

        while (currentNode != target) {
            currentItem = (currentItem.next = new LinkedNode(currentNode));
            currentNode = getNextNode(currentNode);
        }
        currentItem = (currentItem.next = new LinkedNode(currentNode));

        currentItem.next = oldNext;
    }

    private List<Integer> createFinalPath(final LinkedNode head) {
        final List<Integer> ret = new LinkedList<>();
        LinkedNode current = head;

        do {
            ret.add(current.node);
            current = current.next;
        } while (current != head);

        return ret;
    }

    private int getNextNode(final int fromNode) {
        final Map<Integer, Integer> fromNodes = availableNodes.get(fromNode);

        if (!fromNodes.isEmpty()) {
            for (final Map.Entry<Integer, Integer> entry : fromNodes.entrySet()) {
                final int toNode = entry.getKey();
                final int occurances = entry.getValue();
                final Map<Integer, Integer> toNodes = availableNodes.get(toNode);

                if (occurances == 1) {
                    fromNodes.remove(toNode);
                    toNodes.remove(fromNode);
                } else {
                    fromNodes.put(toNode, occurances - 1);
                    toNodes.put(fromNode, occurances - 1);
                }

                --availableEdges;

                return toNode;
            }
        }

        return -1;
    }

    private class LinkedNode {

        private LinkedNode next;
        private int        node;

        public LinkedNode(final int node) {
            this.node = node;
        }
    }

}
