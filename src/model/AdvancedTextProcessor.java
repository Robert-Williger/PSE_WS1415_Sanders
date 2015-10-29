package model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.elements.Label;
import model.elements.StreetNode;
import model.map.AddressNode;
import model.map.IMapManager;

public class AdvancedTextProcessor implements ITextProcessor {
    private final int maxDistance;
    private final int suggestions;
    private final BoundedHeap<Tuple> heap;
    private final int[][] distance;
    private final SortedTree indexRoot;
    private int maxStringLength;

    public AdvancedTextProcessor() {
        this(new Entry[0][], new Label[0], null);
    }

    public AdvancedTextProcessor(final Entry[][] entries, final Label[] labels, final IMapManager manager) {
        this(entries, labels, manager, 5, 2);
    }

    public AdvancedTextProcessor(final Entry[][] entries, final Label[] labels, final IMapManager manager,
            final int suggestions, final int maxDistance) {

        this.suggestions = suggestions;
        this.heap = new BoundedHeap<Tuple>(suggestions);
        this.maxDistance = maxDistance;

        final Tree unsortedRoot = setupIndex(entries, labels, manager);
        distance = createDistanceArray();
        maxStringLength += 2 * maxDistance;

        indexRoot = unsortedRoot.toSortedTree();
    }

    private Tree setupIndex(final Entry[][] entries, final Label[] labels, final IMapManager manager) {
        final Tree root = new Tree("", null, null);

        maxStringLength = 0;
        for (final Entry[] entry : entries) {
            for (int i = 0; i < entry.length; i++) {
                final String streetName = entry[i].getStreet().getName();
                final String cityName = entry[i].getCity();
                final String realName = streetName + " " + cityName;
                final StreetNode node = new StreetNode(0.5f, entry[i].getStreet());
                // TODO order by size of city
                add(normalize(streetName + " " + cityName), realName, root, node);
                add(normalize(cityName + " " + streetName), realName, root, node);
            }
        }

        // TODO suburbs can collide --> include city names
        for (final Label label : labels) {
            final AddressNode addressNode = manager.getAddressNode(label.getLocation());
            if (addressNode != null) {
                add(normalize(label.getName()), label.getName(), root, addressNode.getStreetNode());
            }
        }

        return root;
    }

    private int[][] createDistanceArray() {
        final int secondLength = maxStringLength + 2 * maxDistance + 1;
        final int[][] distance = new int[maxStringLength + 1][secondLength];
        for (int i = 0; i < distance.length; i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j < secondLength; j++) {
            distance[0][j] = j;
        }

        return distance;
    }

    private void add(final String normalizedName, final String realName, final Tree tree, final StreetNode streetNode) {
        if (normalizedName.length() > maxStringLength) {
            maxStringLength = normalizedName.length();
        }
        add(normalizedName, realName, streetNode, tree, 0);
    }

    private void add(final String normalizedName, final String realName, final StreetNode streetNode, final Tree root,
            int index) {

        if (normalizedName.length() > 1) {
            for (final Iterator<Tree> iterator = root.getChildren().iterator(); iterator.hasNext();) {
                final Tree child = iterator.next();
                final String childName = child.getIndexName();

                if (normalizedName.charAt(index) == childName.charAt(index)) {
                    ++index;
                    final int length = Math.min(normalizedName.length(), childName.length());
                    while (index < length) {
                        if (normalizedName.charAt(index) != childName.charAt(index)) {
                            iterator.remove();
                            final Tree splitNode = new Tree(normalizedName.substring(0, index), null, null);
                            splitNode.addChild(child);
                            splitNode.addChild(new Tree(normalizedName, realName, streetNode));
                            root.getChildren().add(splitNode);
                            return;
                        }
                        ++index;
                    }
                    if (normalizedName.length() == index) {
                        if (child.getIndexName().length() != index) {
                            iterator.remove();
                            final Tree splitNode = new Tree(normalizedName, realName, streetNode);
                            splitNode.addChild(child);
                            root.getChildren().add(splitNode);
                        } else if (child.getName() == null) {
                            child.setName(realName);
                            child.setStreetNode(streetNode);
                        }
                        return;
                    }
                    add(normalizedName, realName, streetNode, child, index);
                    return;
                }

            }

            root.getChildren().add(new Tree(normalizedName, realName, streetNode));
        }
    }

    @Override
    public List<String> suggest(final String address) {
        final String normalizedAddress = normalize(address);

        suggestBinary(normalizedAddress, indexRoot, 0);

        int length = Math.min(suggestions, heap.size());

        final String[] choice = new String[length];
        for (int i = 0; i < length; i++) {
            choice[length - i - 1] = heap.deleteMax().getName();
        }

        return Arrays.asList(choice);
    }

    private void suggestBinary(final String input, final SortedTree tree, int index) {
        int distance = prefixEditDistance(input, tree.getIndexName());

        if (distance <= maxDistance) {
            if (heap.size() == suggestions) {
                Tuple max = heap.max();
                if (max.getDistance() == 0 || distance >= max.getDistance()) {
                    return;
                }
            }

            if (tree.getName() != null && tree.getIndexName().length() >= input.length() - 1) {
                heap.insert(new Tuple(tree.getName(), distance));
            }

            final SortedTree child = binarySearch(tree, input, index);
            if (child != null) {
                if (input.length() > (child.getIndexName().length())) {
                    suggestBinary(input, child, child.getIndexName().length());
                } else {
                    suggest(input, child);
                }
            }

            for (final SortedTree node : tree.getChildren()) {
                if (node != child) {
                    suggest(input, node);
                }
            }
        }
    }

    private void suggest(final String input, final SortedTree tree) {
        int distance = prefixEditDistance(input, tree.getIndexName());

        if (distance <= maxDistance) {
            if (heap.size() == suggestions) {
                Tuple max = heap.max();
                if (max.getDistance() == 0 || distance >= max.getDistance()) {
                    return;
                }
            }

            // TODO improve this?
            if (tree.getName() != null
                    && tree.getIndexName().length() >= input.length() - (tree.getIndexName().length() >> 1)) {
                heap.insert(new Tuple(tree.getName(), distance));
            }

            for (final SortedTree node : tree.getChildren()) {
                suggest(input, node);
            }
        }
    }

    @Override
    public StreetNode parse(final String address) {
        return parse(indexRoot, normalize(address), 0);
    }

    private StreetNode parse(final SortedTree root, final String input, int index) {
        final SortedTree child = binarySearch(root, input, index);
        int childLength;
        if (child == null || (childLength = child.getIndexName().length()) > input.length()) {
            return null;
        }

        while (index < childLength) {
            if (child.getIndexName().charAt(index) != input.charAt(index)) {
                return null;
            }
            ++index;
        }

        return index == input.length() ? child.getStreetNode() : parse(child, input, index);
    }

    // TODO fit edit costs --> nearby keys lower costs than far away ones
    // binary search looks for child with same character as input at given index
    private SortedTree binarySearch(final SortedTree root, final String input, int index) {
        final SortedTree[] children = root.getChildren();

        int left = 0;
        int right = children.length - 1;

        while (left <= right) {
            int middle = (left + right) / 2;
            int dif = children[middle].getIndexName().charAt(index) - input.charAt(index);

            if (dif == 0) { // maybe found
                return children[middle];
            }

            if (dif < 0) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }

        return null;
    }

    private int prefixEditDistance(String a, String b) {
        if (a.length() > b.length()) {
            String temp = b;
            b = a;
            a = temp;
        }

        int minDist = Integer.MAX_VALUE;
        int currentMinDist = Integer.MAX_VALUE;
        // TODO necessary?
        int bLength = Math.min(b.length(), maxStringLength);

        for (int j = 0; j < bLength; j++) {
            currentMinDist = minDist + 1;
            for (int i = 0; i < a.length(); i++) {

                final int cost = a.charAt(i) == b.charAt(j) ? 0 : 1;

                distance[i + 1][j + 1] = min( //
                        distance[i][j + 1] + 1, // deletion
                        distance[i + 1][j] + 1, // insertion
                        distance[i][j] + cost // substitution
                );

                if (i > 1) {
                    if (distance[i][j + 1] < currentMinDist) {
                        currentMinDist = distance[i][j + 1];
                    }
                    if (j > 1 && a.charAt(i) == b.charAt(j - 1) && a.charAt(i - 1) == b.charAt(j)) {
                        distance[i + 1][j + 1] = Math.min( //
                                distance[i - 1][j - 1] + cost, // transposition
                                distance[i + 1][j + 1] // current minimum
                                );
                    }
                }

            }

            minDist = Math.min(distance[a.length()][j + 1], minDist);
            if (currentMinDist > minDist) {
                // TODO improvement still possible by transposition?
                return minDist;
            }
        }

        return Math.min(minDist, currentMinDist);

    }

    private static int min(final int a, final int b, final int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static String normalize(String name) {

        name = name.toLowerCase();

        final StringBuilder nameSB = new StringBuilder(name);
        replaceAllSB(nameSB, "ß", "ss");
        replaceAllSB(nameSB, "ä", "ae");
        replaceAllSB(nameSB, "ü", "ue");
        replaceAllSB(nameSB, "ö", "oe");
        // TODO is that reasonable?
        replaceAllSB(nameSB, " ", "");
        replaceAllSB(nameSB, "-", "");

        return nameSB.toString();
    }

    private static void replaceAllSB(final StringBuilder nameSB, final String from, final String to) {
        int index = nameSB.indexOf(from);
        while (index >= 0) {
            nameSB.replace(index, index + from.length(), to);
            index = index + to.length();
            index = nameSB.indexOf(from, index);
        }
    }

    private static class Tuple implements Comparable<Tuple> {

        private final String s;
        private final int d;

        public Tuple(final String s, final int distance) {
            this.s = s;
            d = distance;
        }

        public Integer getDistance() {
            return d;
        }

        public String getName() {
            return s;
        }

        @Override
        public int compareTo(final Tuple o) {
            int ret = d - o.d;
            if (ret == 0) {
                ret = s.length() - o.s.length();
            }
            return ret;
        }

    }

    private static class Tree {
        private static final SortedTree[] emptyChildren = new SortedTree[0];

        private String name;
        private final String indexName;
        private final List<Tree> children;
        private StreetNode streetNode;

        public Tree(final String indexName, final String name, final StreetNode streetNode) {
            this.indexName = indexName;
            this.name = name;
            this.streetNode = streetNode;
            children = new LinkedList<Tree>();
        }

        public void addChild(final Tree node) {
            children.add(node);
        }

        public String getIndexName() {
            return indexName;
        }

        public void setStreetNode(final StreetNode node) {
            this.streetNode = node;
        }

        public List<Tree> getChildren() {
            return children;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public SortedTree toSortedTree() {
            final SortedTree[] children;

            if (!this.children.isEmpty()) {

                children = new SortedTree[this.children.size()];

                int count = -1;
                for (final Tree node : this.children) {
                    children[++count] = node.toSortedTree();
                }

                Arrays.sort(children);

            } else {
                children = emptyChildren;
            }

            return new SortedTree(indexName, name, streetNode, children);
        }
    }

    private static class SortedTree implements Comparable<SortedTree> {
        private final String name;
        private final String indexName;
        private final SortedTree[] children;
        private final StreetNode streetNode;

        public SortedTree(final String indexName, final String name, final StreetNode streetNode,
                final SortedTree[] children) {
            this.indexName = indexName;
            this.name = name;
            this.streetNode = streetNode;
            this.children = children;
        }

        public String getIndexName() {
            return indexName;
        }

        public StreetNode getStreetNode() {
            return streetNode;
        }

        public SortedTree[] getChildren() {
            return children;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(final SortedTree o) {
            return indexName.compareTo(o.indexName);
        }
    }
}
