package model.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

public class BlossomAlgorithm implements IPerfectMatchingFinder {

    private static final double EPSILON = 0.0000001;

    private IUndirectedGraph undirectedGraph;

    // Set of currently unmatched nodes, excluding the root of the current tree
    private Set<Integer> unmatchedNodes;

    // Set of nodes currently matched and not contained in the current tree
    private Set<Integer> matchedNonTreeNodes;

    private Set<Integer> oddBlossoms;

    // Current tree
    private int root;
    private int[] parent;
    private Set<Integer> evenNodes;
    private Set<Integer> oddNodes;

    // Current matching
    private int[] match;

    // Values of the dual part
    private List<Double> y;

    private List<Stack<Integer>> nodeToPseudoNode;
    private List<Collection<Integer>> nodeToNodes;

    // Edges of the blossom ordered as cycle
    private List<List<Long>> blossomEdges;

    private Set<Integer> blossoms;

    private boolean finished;
    private boolean debug;// = true;

    @Override
    public Set<Long> calculatePerfectMatching(final IUndirectedGraph undirectedGraph) {
        initialize(undirectedGraph);
        execute();
        return createFinalMatching();
    }

    private void initialize(final IUndirectedGraph undirectedGraph) {
        final int nodes = undirectedGraph.getNodes();

        this.undirectedGraph = undirectedGraph;

        unmatchedNodes = new HashSet<Integer>(nodes);
        matchedNonTreeNodes = new HashSet<Integer>();
        oddBlossoms = new HashSet<Integer>();

        evenNodes = new HashSet<Integer>();
        oddNodes = new HashSet<Integer>();

        match = new int[nodes];
        parent = new int[nodes];
        y = new ArrayList<Double>(nodes);

        nodeToPseudoNode = new ArrayList<Stack<Integer>>(nodes);
        nodeToNodes = new ArrayList<Collection<Integer>>(nodes);

        blossomEdges = new ArrayList<List<Long>>();

        blossoms = new LinkedHashSet<Integer>();

        for (int i = 0; i < nodes; i++) {
            match[i] = -1;
            parent[i] = -1;
            unmatchedNodes.add(i);
            y.add(0.0);

            final Stack<Integer> stack = new Stack<Integer>();
            stack.push(i);
            nodeToPseudoNode.add(stack);

            final List<Integer> list = new ArrayList<Integer>();
            list.add(i);
            nodeToNodes.add(list);
        }
    }

    private void execute() {
        createNewTree(0);
        while (!finished) {
            performDualUpdate();
            performPrimalUpdate();
        }
    }

    private void performDualUpdate() {
        double update = Integer.MAX_VALUE;

        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {

                // Looking for augmentations
                for (final int unmatchedNode : unmatchedNodes) {
                    final double weight = getReducedWeight(node, unmatchedNode);

                    if (lessThan(weight, update)) {
                        update = weight;
                    }

                }

                // Looking for grows
                for (final int matchedNode : matchedNonTreeNodes) {
                    for (final int connectionNode : getNodes(getPseudoNode(matchedNode))) {
                        final double weight = getReducedWeight(node, connectionNode);

                        if (lessThan(weight, update)) {
                            update = weight;
                        }
                    }
                }

                // Looking for shrinkings
                for (final int evenPseudoNode : evenNodes) {
                    if (evenPseudoNode != pseudoNode) {
                        for (final int evenNode : getNodes(evenPseudoNode)) {
                            final double weight = getReducedWeight(node, evenNode) / 2;
                            if (lessThan(weight, update)) {
                                update = weight;
                            }
                        }
                    }
                }

            }
        }

        // Looking for expandings
        for (final int oddBlossom : oddBlossoms) {
            final double weight = y.get(oddBlossom);

            if (lessThan(weight, update)) {
                update = weight;
            }
        }

        for (final int node : evenNodes) {
            y.set(node, y.get(node) + update);
        }
        for (final int node : oddNodes) {
            y.set(node, y.get(node) - update);
        }

        if (debug) {
            System.out.println("Dual update: -" + update + " on A = " + oddNodes + " and +" + update + " on B = "
                    + evenNodes);
        }
    }

    private void performPrimalUpdate() {

        // Looking for augmentations
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {
                for (final int unmatchedNode : unmatchedNodes) {
                    if (equal(getReducedWeight(node, unmatchedNode), 0)) {
                        augment(node, unmatchedNode);
                        return;
                    }
                }
            }
        }

        // Looking for grows
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {
                for (final int matchNode : matchedNonTreeNodes) {
                    for (final int connectionNode : getNodes(getPseudoNode(matchNode))) {
                        if (equal(getReducedWeight(node, connectionNode), 0)) {
                            grow(node, connectionNode, matchNode);
                            return;
                        }
                    }
                }
            }

        }

        // Looking for shrinkings
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {
                for (final int evenPseudoNode : evenNodes) {
                    if (evenPseudoNode != pseudoNode) {
                        for (final int evenNode : getNodes(evenPseudoNode)) {
                            if (equal(getReducedWeight(node, evenNode), 0)) {
                                shrink(node, evenNode);
                                return;
                            }
                        }
                    }
                }
            }
        }

        // Looking for expandings
        for (final int oddBlossom : oddBlossoms) {
            if (equal(y.get(oddBlossom), 0)) {
                expand(oddBlossom);
                return;
            }
        }
    }

    private int getNextRoot() {
        for (int node = 0; node < undirectedGraph.getNodes(); node++) {
            if (getPseudoNode(node) == node && match[node] == -1) {
                return node;
            }
        }

        return -1;
    }

    private void createNewTree(final int root) {

        this.root = root;

        for (int node = 0; node < undirectedGraph.getNodes(); node++) {
            parent[node] = -1;
            if (match[node] != -1) {
                matchedNonTreeNodes.add(node);
            }
        }

        evenNodes.clear();
        oddNodes.clear();
        oddBlossoms.clear();
        evenNodes.add(root);

        unmatchedNodes.remove(root);
    }

    private void augment(final int treeNode, final int otherNode) {

        int second = treeNode;
        for (final int node : getNodes(getPseudoNode(treeNode))) {
            match[node] = -1;
        }
        match(treeNode, otherNode);

        while (getPseudoNode(second) != getPseudoNode(root)) {
            int first = -1;

            for (final int node : getNodes(getPseudoNode(second))) {
                if (parent[node] != -1) {
                    second = node;
                    first = parent[node];
                    break;
                }
            }

            second = parent[first];

            if (parent[first] == -1) {
                match[first] = -1;
                for (final int node : getNodes(getPseudoNode(first))) {
                    if (parent[node] != -1) {
                        first = node;
                        second = parent[node];
                        break;
                    }
                }
            }

            // TODO Improve this
            for (final int node : getNodes(getPseudoNode(first))) {
                match[node] = -1;
            }
            for (final int node : getNodes(getPseudoNode(second))) {
                match[node] = -1;
            }
            match(first, second);
        }

        unmatchedNodes.remove(otherNode);
        unmatchedNodes.remove(root);

        if (debug) {
            System.out.println("Primal update: Augmented matching with (" + treeNode + ", " + otherNode + ")");
        }

        final int root = getNextRoot();

        if (root != -1) {
            createNewTree(root);
        } else {
            expandFinally();
        }

    }

    private void grow(final int treeNode, final int connectionNode, final int matchNode) {
        final int matchedNode = match[matchNode];

        parent[connectionNode] = treeNode;
        parent[matchedNode] = matchNode;

        evenNodes.add(getPseudoNode(matchedNode));
        oddNodes.add(getPseudoNode(matchNode));
        if (getPseudoNode(matchNode) != matchNode) {
            oddBlossoms.add(getPseudoNode(matchNode));
        }

        matchedNonTreeNodes.remove(matchedNode);
        matchedNonTreeNodes.remove(matchNode);

        if (debug) {
            System.out.println("Primal update: Grew tree with (" + connectionNode + "," + treeNode + ") and ("
                    + matchedNode + "," + matchNode + ")");
        }
    }

    private void shrink(final int fromNode, final int toNode) {
        final Collection<Integer> nodes = new LinkedList<Integer>();

        final List<Long> edges = new ArrayList<Long>(); // edges of cycle,
                                                        // ordered as ring

        final Set<Integer> set = new HashSet<Integer>();

        int current = getPseudoNode(fromNode); // current (pseudo-)node
        set.add(current);
        while (current != getPseudoNode(root) && current != getPseudoNode(toNode)) {
            for (final int node : getNodes(current)) {
                if (parent[node] != -1) {
                    current = getPseudoNode(parent[node]);
                    break;
                }
            }

            set.add(getPseudoNode(current));
        }

        int crossing = toNode;

        edges.add(undirectedGraph.getEdge(fromNode, toNode));
        while (!set.contains(getPseudoNode(crossing))) {
            nodes.addAll(getNodes(getPseudoNode(crossing)));

            for (final int node : getNodes(getPseudoNode(crossing))) {
                if (parent[node] != -1) {
                    edges.add(undirectedGraph.getEdge(node, parent[node]));
                    crossing = parent[node];
                    break;
                }
            }
        }

        current = getPseudoNode(fromNode);
        for (final int node : getNodes(current)) {
            if (parent[node] != -1) {
                current = getPseudoNode(node);
                break;
            }
        }
        final List<Long> invertedEdgeOrder = new LinkedList<Long>();
        while (current != getPseudoNode(crossing)) {
            nodes.addAll(getNodes(current));

            for (final int node : getNodes(current)) {
                if (parent[node] != -1) {
                    invertedEdgeOrder.add(undirectedGraph.getEdge(node, parent[node]));
                    current = getPseudoNode(parent[node]);
                    break;
                }
            }

        }
        for (final ListIterator<Long> it = invertedEdgeOrder.listIterator(invertedEdgeOrder.size()); it.hasPrevious();) {
            edges.add(it.previous());
        }

        for (final int node : nodes) {
            match[node] = -1;
            parent[node] = -1;
        }

        nodes.addAll(getNodes(getPseudoNode(crossing)));

        final int nodeNumber = y.size();

        blossoms.add(nodeNumber);

        for (final int node : nodes) {
            evenNodes.remove(getPseudoNode(node));
            oddNodes.remove(getPseudoNode(node));
            oddBlossoms.remove(getPseudoNode(node));

            nodeToPseudoNode.get(node).push(nodeNumber);
        }

        y.add(0.0);

        nodeToPseudoNode.add(new Stack<Integer>());
        nodeToPseudoNode.get(nodeNumber).push(nodeNumber);

        nodeToNodes.add(nodes);
        blossomEdges.add(edges);

        evenNodes.add(nodeNumber);

        if (debug) {
            System.out.println("Primal update: Shrinked blossom " + nodes);
        }

    }

    private void expand(final int oddBlossom) {
        final List<Long> edges = blossomEdges.get(oddBlossom - undirectedGraph.getNodes());

        int treeNode = -1;
        int matchIndex = -1;
        int matchNode = -1;

        for (final int node : getNodes(oddBlossom)) {
            nodeToPseudoNode.get(node).pop();
            if (parent[node] != -1) {
                treeNode = node;
                if (node != getPseudoNode(node)) {
                    oddNodes.add(getPseudoNode(node));
                }
            }
        }

        for (final Iterator<Long> it = edges.iterator(); it.hasNext() && matchNode == -1; ++matchIndex) {
            final long edge = it.next();
            final int[] nodes = {getPseudoNode(undirectedGraph.getFirstNode(edge)), getPseudoNode(undirectedGraph.getSecondNode(edge))};
            for (final int node : nodes) {
                for (final int innerNode : getNodes(node)) {
                    if (match[innerNode] != -1) {
                        matchNode = innerNode;
                    }
                }
            }
        }

        final int pseudoMatchNode = getPseudoNode(matchNode);

        final long nextEdge = edges.get(matchIndex + 1);
        if (getPseudoNode(undirectedGraph.getFirstNode(nextEdge)) != pseudoMatchNode
                && getPseudoNode(undirectedGraph.getSecondNode(nextEdge)) != pseudoMatchNode) {
            matchIndex = edges.size() - 1;
        }

        if (pseudoMatchNode != matchNode) {
            oddBlossoms.add(getPseudoNode(matchNode));
        }

        final Set<Integer> treeNodes = new HashSet<Integer>();

        if (treeNode != -1) {
            final int pseudoTreeNode = getPseudoNode(treeNode);
            oddNodes.add(pseudoTreeNode);

            if (pseudoTreeNode != pseudoMatchNode) {

                if (pseudoTreeNode != treeNode) {
                    oddBlossoms.add(pseudoTreeNode);
                }

                int steps = 0;
                for (int i = 0; i < edges.size(); i++) {
                    ++steps;

                    final long edge = edges.get((i + matchIndex + 1) % edges.size());
                    if (getPseudoNode(undirectedGraph.getFirstNode(edge)) == pseudoTreeNode
                            || getPseudoNode(undirectedGraph.getSecondNode(edge)) == pseudoTreeNode) {
                        break;
                    }
                }

                int lastNode = matchNode;
                int count = 0;
                int direction;
                int currentTreeEdge;

                if (steps % 2 == 0) {
                    direction = 1;
                    currentTreeEdge = matchIndex + 1;
                } else {
                    steps = edges.size() - steps;
                    currentTreeEdge = matchIndex + edges.size();
                    direction = -1;
                }

                for (int i = 0; i < steps; i++) {
                    final long edge = edges.get(currentTreeEdge % edges.size());

                    int currentNode;

                    int firstNode = undirectedGraph.getFirstNode(edge);
                    int secondNode = undirectedGraph.getSecondNode(edge);

                    if (firstNode == lastNode) {
                        currentNode = secondNode;
                    } else if (secondNode == lastNode) {
                        currentNode = firstNode;
                    } else {
                        if (getPseudoNode(lastNode) == getPseudoNode(firstNode)) {
                            currentNode = secondNode;
                            lastNode = firstNode;
                        } else {
                            currentNode = firstNode;
                            lastNode = secondNode;
                        }
                    }

                    parent[lastNode] = currentNode;
                    treeNodes.add(getPseudoNode(currentNode));

                    if (++count % 2 == 0) {
                        evenNodes.add(getPseudoNode(lastNode));
                    } else {
                        oddNodes.add(getPseudoNode(lastNode));
                        if (getPseudoNode(lastNode) != lastNode) {
                            oddBlossoms.add(getPseudoNode(lastNode));
                        }
                    }

                    lastNode = currentNode;
                    currentTreeEdge += direction;
                }
            }
        }

        // Add edges to matching
        for (int i = 0; i < edges.size() / 2 + 1; i++) {
            if (i % 2 == 1) {
                long[] currentEdges = {edges.get((matchIndex + i + 1) % edges.size()),
                        edges.get((matchIndex - i + edges.size()) % edges.size())};
                for (final long edge : currentEdges) {
                    match(edge);
                    int[] currentNodes = {undirectedGraph.getFirstNode(edge), undirectedGraph.getSecondNode(edge)};
                    for (final int node : currentNodes) {
                        if (!treeNodes.contains(getPseudoNode(node))) {
                            matchedNonTreeNodes.add(node);
                        }
                    }
                }
            }
        }

        oddNodes.remove(oddBlossom);
        oddBlossoms.remove(oddBlossom);
        blossoms.remove(oddBlossom);

        if (debug) {
            System.out.println("Primal update: Expanded odd blossom " + getNodes(oddBlossom));
        }

        nodeToNodes.set(oddBlossom, null);
        blossomEdges.set(oddBlossom - undirectedGraph.getNodes(), null);
    }

    private void expandFinally() {
        Integer[] blossoms = new Integer[this.blossoms.size()];
        blossoms = this.blossoms.toArray(blossoms);

        for (int i = blossoms.length - 1; i >= 0; i--) {
            expand(blossoms[i]);
        }

        finished = true;
    }

    private void match(final long edge) {
        match(undirectedGraph.getFirstNode(edge), undirectedGraph.getSecondNode(edge));
    }

    private void match(final int node1, final int node2) {
        match[node1] = node2;
        match[node2] = node1;
    }

    private double getReducedWeight(final int node1, final int node2) {
        double ret = undirectedGraph.getWeight(undirectedGraph.getEdge(node1, node2));
        for (final int node : nodeToPseudoNode.get(node1)) {
            ret -= y.get(node);
        }
        for (final int node : nodeToPseudoNode.get(node2)) {
            ret -= y.get(node);
        }
        return ret;
    }

    private int getPseudoNode(final int pseudoNode) {
        return nodeToPseudoNode.get(pseudoNode).peek();
    }

    private Collection<Integer> getNodes(final int pseudoNode) {
        return nodeToNodes.get(pseudoNode);
    }

    private Set<Long> createFinalMatching() {
        final Set<Long> ret = new HashSet<Long>();

        for (int i = 0; i < match.length; i++) {
            ret.add(undirectedGraph.getEdge(i, match[i]));
        }

        return ret;
    }

    private static final boolean lessThan(final double value1, final double value2) {
        return value2 - value1 > EPSILON;
    }

    private static final boolean equal(final double value1, final double value2) {
        return Math.abs(value1 - value2) < EPSILON;
    }
}
