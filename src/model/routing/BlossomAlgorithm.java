package model.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

public class BlossomAlgorithm implements IPerfectMatchingFinder {

    private IGraph graph;

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

    private int[] match;

    // Values of the dual part
    private List<Integer> y; // TODO Double!?

    private List<Stack<Integer>> nodeToPseudoNode;
    private List<Collection<Integer>> nodeToNodes;

    // Edges of the blossom ordered as cycle
    private List<List<Long>> blossomEdges;

    private Set<Integer> blossoms;

    private boolean finished;
    private boolean debug = true;

    private int count;

    @Override
    public Set<Long> calculatePerfectMatching(final IGraph graph) {
        initialize(graph);
        execute();
        return createFinalMatching();
    }

    private void initialize(final IGraph graph) {
        final int nodes = graph.getNodes();

        this.graph = graph;

        this.unmatchedNodes = new HashSet<Integer>(nodes);
        this.matchedNonTreeNodes = new HashSet<Integer>();
        this.oddBlossoms = new HashSet<Integer>();

        this.evenNodes = new HashSet<Integer>();
        this.oddNodes = new HashSet<Integer>();

        this.match = new int[nodes];
        this.parent = new int[nodes];
        this.y = new ArrayList<Integer>(nodes);

        this.nodeToPseudoNode = new ArrayList<Stack<Integer>>(nodes);
        this.nodeToNodes = new ArrayList<Collection<Integer>>(nodes);

        this.blossomEdges = new ArrayList<List<Long>>();

        this.blossoms = new LinkedHashSet<Integer>();

        for (int i = 0; i < nodes; i++) {
            match[i] = -1;
            parent[i] = -1;
            unmatchedNodes.add(i);
            y.add(0);

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
        int update = Integer.MAX_VALUE;

        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {

                // Looking for augmentations
                for (final int unmatchedNode : unmatchedNodes) {
                    final int weight = getReducedWeight(node, unmatchedNode);

                    if (weight < update) {
                        update = weight;
                    }

                }

                // Looking for grows
                for (final int matchedNode : matchedNonTreeNodes) {
                    for (final int connectionNode : getNodes(getPseudoNode(matchedNode))) {
                        final int weight = getReducedWeight(node, connectionNode);

                        if (weight < update) {
                            update = weight;
                        }
                    }
                }

                // Looking for shrinkings
                for (final int evenPseudoNode : evenNodes) {
                    if (evenPseudoNode != pseudoNode) {
                        for (final int evenNode : getNodes(evenPseudoNode)) {
                            final int weight = getReducedWeight(node, evenNode) / 2;
                            if (weight < update) {
                                update = weight;
                            }
                        }
                    }
                }

            }
        }

        // Looking for expandings
        for (final int oddBlossom : oddBlossoms) {
            final int weight = y.get(oddBlossom);

            if (weight < update) {
                update = weight;
            }
        }

        for (final int node : evenNodes) {
            y.set(node, y.get(node) + update);
        }
        for (final int node : oddNodes) {
            y.set(node, y.get(node) - update);
        }

        if (debug)
            System.out.println("Dual update: -" + update + " on A = " + oddNodes + " and +" + update + " on B = "
                    + evenNodes);
    }

    private void performPrimalUpdate() {
        // ++count;
        // System.out.println(count);
        // Looking for augmentations
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {

                for (final int unmatchedNode : unmatchedNodes) {
                    if (getReducedWeight(node, unmatchedNode) == 0) {
                        augment(node, unmatchedNode);
                        return;
                    }
                }
            }
        }

        // Looking for grows
        if (count == 19) {
            grow(14, 4, 3);
            return;
        }
        if (count == 35) {
            grow(12, 7, 7);
            return;
        }
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {

                for (final int matchNode : matchedNonTreeNodes) {
                    for (final int connectionNode : getNodes(getPseudoNode(matchNode))) {
                        if (getReducedWeight(node, connectionNode) == 0) {
                            grow(node, connectionNode, matchNode);
                            return;
                        }
                    }
                }
            }

        }

        if (count == 36) {
            shrink(6, 2);
            return;
        }
        // Looking for shrinkings
        for (final int pseudoNode : evenNodes) {
            for (final int node : getNodes(pseudoNode)) {

                for (final int evenNode : evenNodes) {
                    if (getReducedWeight(node, evenNode) == 0) {
                        shrink(node, evenNode);
                        return;
                    }
                }
            }
        }

        // Looking for expandings
        for (final int oddBlossom : oddBlossoms) {
            if (y.get(oddBlossom) == 0) {
                expand(oddBlossom);
                return;
            }
        }
    }

    private int getNextRoot() {
        for (int node = 0; node < graph.getNodes(); node++) {
            if (getPseudoNode(node) == node && match[node] == -1) {
                return node;
            }
        }

        return -1;
    }

    private void createNewTree(final int root) {

        // "Free" matching edges of tree [they are that ones not being a
        // blossom]

        this.root = root;

        for (int node = 0; node < graph.getNodes(); node++) {
            if (match[node] != -1) {
                matchedNonTreeNodes.add(node);
            }
        }

        evenNodes.clear();
        oddNodes.clear();
        evenNodes.add(root);

        unmatchedNodes.remove(root);

    }

    private void augment(final int treeNode, final int otherNode) {
        // TODO parent nur für (-) - (+) Kante, da (+) - (-) Kante über
        // match[(+)] ermittelbar

        match(treeNode, otherNode);

        int pseudoRoot = getPseudoNode(root);

        int second = treeNode;
        while (getPseudoNode(second) != pseudoRoot) {
            int first = -1;
            for (final int node : getNodes(getPseudoNode(second))) {
                if (parent[node] != -1) {
                    first = parent[node];
                    break;
                }
            }
            second = parent[first];

            if (match[match[first]] == first) {
                match[first] = -1;
            }
            match(first, second);

        }

        unmatchedNodes.remove(otherNode);
        unmatchedNodes.remove(root);

        if (debug)
            System.out.println("Primal update: Augmented matching with (" + treeNode + ", " + otherNode + ")");

        int root = getNextRoot();

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

        if (debug)
            System.out.println("Primal update: Grew tree with (" + connectionNode + "," + treeNode + ") and ("
                    + matchedNode + "," + matchNode + ")");
    }

    private void shrink(final int fromNode, final int toNode) {
        final Collection<Integer> nodes = new LinkedList<Integer>();
        final List<Long> edges = new ArrayList<Long>();

        final Set<Integer> set = new HashSet<Integer>();

        int pseudoRoot = getPseudoNode(root);
        int pseudoToNode = getPseudoNode(toNode);

        int current = fromNode;

        set.add(getPseudoNode(fromNode));
        while (getPseudoNode(current) != pseudoRoot && getPseudoNode(current) != pseudoToNode) {
            for (final int node : getNodes(getPseudoNode(current))) {
                if (parent[node] != -1) {
                    current = parent[node];
                    break;
                }
            }

            set.add(getPseudoNode(current));
        }

        int crossing = toNode;

        edges.add(graph.getEdge(fromNode, toNode));
        while (!set.contains(getPseudoNode(crossing))) {
            nodes.addAll(getNodes(getPseudoNode(crossing)));
            edges.add(graph.getEdge(crossing, parent[crossing]));
            crossing = parent[crossing];
        }

        current = fromNode;
        for (final int node : getNodes(getPseudoNode(current))) {
            if (parent[node] != -1) {
                current = node;
                break;
            }
        }
        List<Long> invertedEdgeOrder = new LinkedList<Long>();
        while (getPseudoNode(current) != getPseudoNode(crossing)) {
            nodes.addAll(getNodes(getPseudoNode(current)));

            int currentParent = -1;
            for (final int node : getNodes(getPseudoNode(current))) {
                if (parent[node] != -1) {
                    current = node;
                    currentParent = parent[node];
                    break;
                }
            }
            invertedEdgeOrder.add(graph.getEdge(current, currentParent));
            System.out.println(current + 1 + "," + (currentParent + 1));
            current = currentParent;
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

            nodeToPseudoNode.get(node).push(nodeNumber);
        }

        y.add(0);

        nodeToPseudoNode.add(new Stack<Integer>());
        nodeToPseudoNode.get(nodeNumber).push(nodeNumber);

        nodeToNodes.add(nodes);
        blossomEdges.add(edges);

        evenNodes.add(nodeNumber);

        if (debug)
            System.out.println("Primal update: Shrinked blossom " + nodes);

    }

    private void expand(int oddBlossom) {
        for (final int node : getNodes(oddBlossom)) {
            final Stack<Integer> stack = nodeToPseudoNode.get(node);
            stack.pop();
        }

        for (final int node : getNodes(oddBlossom)) {
            if (parent[node] != -1) {
                oddNodes.add(getPseudoNode(node));
            }
        }

        final List<Long> edges = blossomEdges.get(oddBlossom - graph.getNodes());

        int matchIndex = 0;
        int matchNode = -1;

        for (final long edge : edges) {
            boolean found = false;

            for (final int node : getNodes(getPseudoNode(graph.getFirstNode(edge)))) {
                if (match[node] != -1) {
                    matchNode = node;
                    found = true;
                }
            }

            for (final int node : getNodes(getPseudoNode(graph.getSecondNode(edge)))) {
                if (match[node] != -1) {
                    matchNode = node;
                    found = true;
                }
            }

            if (found) {
                break;
            }

            ++matchIndex;
        }

        int pseudoMatchNode = getPseudoNode(matchNode);

        long nextEdge = edges.get(matchIndex + 1);
        if (getPseudoNode(graph.getFirstNode(nextEdge)) != pseudoMatchNode
                && getPseudoNode(graph.getSecondNode(nextEdge)) != pseudoMatchNode) {
            matchIndex = edges.size() - 1;
        }

        if (getPseudoNode(matchNode) != matchNode) {
            oddBlossoms.add(getPseudoNode(matchNode));
            oddNodes.add(getPseudoNode(matchNode));
        }

        for (final long edge : edges) {
            System.out.println(graph.getFirstNode(edge) + 1 + "," + (graph.getSecondNode(edge) + 1));
        }
        int iterations = edges.size() / 2 + 1;

        // Add edges to matching
        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 1) {
                match(edges.get((matchIndex + i + 1) % edges.size()));
                match(edges.get((matchIndex - i + edges.size()) % edges.size()));
            }
        }

        // Add edges to graph [1 ist hier bekannt als Einstiegspunkt in Baum.]
        // Update even / odd nodes - -blossom + new tree nodes

        oddNodes.remove(oddBlossom);

        int treeNode = -1;
        for (final long edge : edges) {
            if (parent[graph.getFirstNode(edge)] != -1) {
                treeNode = graph.getFirstNode(edge);
                break;
            }

            if (parent[graph.getSecondNode(edge)] != -1) {
                treeNode = graph.getSecondNode(edge);
                break;
            }
        }

        Set<Integer> treeNodes = new HashSet<Integer>();

        // TODO pseudoNode comparison?
        if (treeNode != matchNode && treeNode != -1) {
            if (getPseudoNode(treeNode) != treeNode) {
                oddBlossoms.add(getPseudoNode(treeNode));
            }

            int forwardSteps = 0;
            for (int i = 0; i < edges.size(); i++) {
                ++forwardSteps;

                final long edge = edges.get((i + matchIndex + 1) % edges.size());
                if (graph.getFirstNode(edge) == treeNode || graph.getSecondNode(edge) == treeNode) {
                    break;
                }
            }

            int backwardsSteps = edges.size() - forwardSteps;

            int last = matchNode;
            int count = 0;

            if (forwardSteps % 2 == 0) {
                for (int i = 0; i < forwardSteps; i++) {
                    final long edge = edges.get((i + matchIndex + 1) % edges.size());
                    int current = graph.getFirstNode(edge);
                    if (current == last) {
                        current = graph.getSecondNode(edge);
                    }
                    parent[last] = current;
                    treeNodes.add(last);
                    treeNodes.add(current);

                    (++count % 2 == 0 ? evenNodes : oddNodes).add(getPseudoNode(last));
                    if (count % 2 == 1) {
                        if (getPseudoNode(last) != last) {
                            oddBlossoms.add(getPseudoNode(last));
                        }
                    }
                    last = current;
                }
            } else {
                for (int i = 0; i < backwardsSteps; i++) {
                    final long edge = edges.get((matchIndex - i + edges.size()) % edges.size());
                    int current = graph.getFirstNode(edge);
                    if (current == last) {
                        current = graph.getSecondNode(edge);
                    }

                    parent[last] = current;
                    treeNodes.add(last);
                    treeNodes.add(current);

                    (++count % 2 == 0 ? evenNodes : oddNodes).add(getPseudoNode(last));
                    if (count % 2 == 1) {
                        if (getPseudoNode(last) != last) {
                            oddBlossoms.add(getPseudoNode(last));
                        }
                    }
                    last = current;
                }
            }

            oddNodes.add(getPseudoNode(treeNode));
        } else {
            System.out.println("failed to grow tree at expanding; treeNode " + treeNode);
        }

        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 1) {
                long edge = edges.get((matchIndex + i + 1) % edges.size());
                int node = graph.getFirstNode(edge);
                if (!treeNodes.contains(node)) {
                    matchedNonTreeNodes.add(node);
                }
                node = graph.getSecondNode(edge);
                if (!treeNodes.contains(node)) {
                    matchedNonTreeNodes.add(node);
                }

                edge = edges.get((matchIndex - i + edges.size()) % edges.size());
                node = graph.getFirstNode(edge);
                if (!treeNodes.contains(node)) {
                    matchedNonTreeNodes.add(node);
                }
                node = graph.getSecondNode(edge);
                if (!treeNodes.contains(node)) {
                    matchedNonTreeNodes.add(node);
                }
            }
        }

        oddBlossoms.remove(oddBlossom);
        blossoms.remove(oddBlossom);

        if (debug)
            System.out.println("Primal update: Expanded odd blossom " + getNodes(oddBlossom));

        nodeToNodes.set(oddBlossom, null);
        blossomEdges.set(oddBlossom - graph.getNodes(), null);
    }

    private void expandFinally() {
        System.out.println(blossoms);
        Integer[] blossoms = new Integer[this.blossoms.size()];
        blossoms = this.blossoms.toArray(blossoms);

        for (int i = blossoms.length - 1; i >= 0; i--) {
            expand(blossoms[i]);
            printMatching();
        }

        finished = true;
    }

    private void match(final long edge) {
        match(graph.getFirstNode(edge), graph.getSecondNode(edge));
    }

    private void match(final int node1, final int node2) {
        match[node1] = node2;
        match[node2] = node1;
    }

    private int getReducedWeight(final int node1, final int node2) {
        int ret = graph.getWeight(graph.getEdge(node1, node2));
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
            ret.add(graph.getEdge(i, match[i]));
        }

        return ret;
    }

    private void printMatching() {
        final Set<Long> matching = createFinalMatching();
        for (final long edge : matching) {
            final int node1 = graph.getFirstNode(edge) + 1;
            final int node2 = graph.getSecondNode(edge) + 1;

            if (node1 != 0 && node2 != 0) {
                System.out.print("(" + node1 + "," + node2 + ")");
            }
        }
        System.out.println();
    }
}
