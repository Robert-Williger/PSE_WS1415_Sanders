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
    private final int[][] distance;
    private TreeNode indexRoot;
    private int maxStringLength;

    public AdvancedTextProcessor(final Entry[][] entries, final Label[] labels, final IMapManager manager,
            final int suggestions) {

        this.maxDistance = 2;
        this.suggestions = suggestions;

        setupIndex(entries, labels, manager);
        distance = createDistanceArray();
        maxStringLength += 2 * maxDistance;
    }

    private void setupIndex(final Entry[][] entries, final Label[] labels, final IMapManager manager) {
        indexRoot = new TreeNode("", null, null);

        maxStringLength = 0;
        for (final Entry[] entry : entries) {
            for (int i = 0; i < entry.length; i++) {
                final String streetName = entry[i].getStreet().getName();
                final String cityName = entry[i].getCity();
                final String realName = streetName + " " + cityName;
                final StreetNode node = new StreetNode(0.5f, entry[i].getStreet());
                // TODO order by size of city
                add(normalize(realName), realName, node);
                add(normalize(cityName + " " + streetName), realName, node);
            }
        }

        // TODO suburbs can collide --> include city names
        for (final Label label : labels) {
            final AddressNode addressNode = manager.getAddressNode(label.getLocation());
            if (addressNode != null) {
                add(normalize(label.getName()), label.getName(), addressNode.getStreetNode());
            }
        }
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

    private void add(final String normalizedName, final String realName, final StreetNode streetNode) {
        if (normalizedName.length() > maxStringLength) {
            maxStringLength = normalizedName.length();
        }
        add(normalizedName, realName, streetNode, indexRoot, 0);
    }

    private void add(final String normalizedName, final String realName, final StreetNode streetNode,
            final TreeNode root, int index) {

        if (normalizedName.length() > 1) {
            for (final Iterator<TreeNode> iterator = root.getChildren().iterator(); iterator.hasNext();) {
                final TreeNode child = iterator.next();
                final String childName = child.getName();

                if (normalizedName.charAt(index) == childName.charAt(index)) {
                    ++index;
                    final int length = Math.min(normalizedName.length(), childName.length());
                    while (index < length) {
                        if (normalizedName.charAt(index) != childName.charAt(index)) {
                            iterator.remove();
                            final TreeNode splitNode = new TreeNode(normalizedName.substring(0, index), null, null);
                            splitNode.addChild(child);
                            splitNode.addChild(new TreeNode(normalizedName, realName, streetNode));
                            root.getChildren().add(splitNode);
                            return;
                        }
                        ++index;
                    }
                    if (normalizedName.length() == index) {
                        if (child.getName().length() != index) {
                            iterator.remove();
                            final TreeNode splitNode = new TreeNode(normalizedName, realName, streetNode);
                            splitNode.addChild(child);
                            root.getChildren().add(splitNode);
                        } else if (child.getRealName() == null) {
                            child.setRealName(realName);
                            child.setStreetNode(streetNode);
                        }
                        return;
                    }
                    add(normalizedName, realName, streetNode, child, index);
                    return;
                }

            }

            root.getChildren().add(new TreeNode(normalizedName, realName, streetNode));
        }
    }

    @Override
    public List<String> suggest(final String address) {
        long start = System.currentTimeMillis();

        final BoundedHeap<Tuple> tuples = new BoundedHeap<Tuple>(suggestions);
        final String normalizedAddress = normalize(address);

        suggest(normalizedAddress, indexRoot, tuples);

        int length = Math.min(suggestions, tuples.size());

        final String[] choice = new String[length];
        for (int i = 0; i < length; i++) {
            choice[length - i - 1] = tuples.deleteMax().getName();
        }

        System.out.println(System.currentTimeMillis() - start);
        return Arrays.asList(choice);
    }

    private void suggest(final String input, final TreeNode node, final BoundedHeap<Tuple> heap) {
        int distance = prefixEditDistance(input, node.getName());

        if (distance <= maxDistance) {
            if (heap.size() == suggestions) {
                Tuple max = heap.max();
                if (distance > max.getDistance() || distance == max.getDistance()
                        && node.getName().length() > max.getName().length()) {
                    return;
                }
            }

            if (node.getRealName() != null && node.getName().length() >= input.length() - 1) {
                heap.insert(new Tuple(node.getRealName(), distance));
            }

            for (final TreeNode child : node.getChildren()) {
                suggest(input, child, heap);
            }
        }
    }

    @Override
    public StreetNode parse(final String address) {
        long start = System.currentTimeMillis();
        final StreetNode node = parse(normalize(address), indexRoot, 0);
        System.out.println(System.currentTimeMillis() - start);
        return node;
    }

    private StreetNode parse(final String address, final TreeNode root, int index) {
        for (final TreeNode child : root.getChildren()) {
            final String childName = child.getName();
            if (childName.charAt(index) == address.charAt(index)) {
                ++index;
                while (index < childName.length()) {
                    if (childName.charAt(index) != address.charAt(index)) {
                        return null;
                    }
                    ++index;
                }
                return index != address.length() ? parse(address, child, index) : child.getStreetNode();
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

        int minDist = maxDistance + 1;
        // TODO necessary?
        int bLength = Math.min(b.length(), maxStringLength);

        for (int j = 0; j < bLength; j++) {
            int currentMinDist = minDist + 1;
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

        return minDist;

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
        replaceAllSB(nameSB, "-", " ");

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

    private static class TreeNode {
        private final List<TreeNode> children;
        private final String name;
        private StreetNode streetNode;
        private String realName;

        public TreeNode(final String name, final String realName, final StreetNode streetNode) {
            this.name = name;
            this.realName = realName;
            this.streetNode = streetNode;
            children = new LinkedList<TreeNode>();
        }

        public void addChild(final TreeNode node) {
            children.add(node);
        }

        public String getName() {
            return name;
        }

        public void setStreetNode(final StreetNode node) {
            this.streetNode = node;
        }

        public StreetNode getStreetNode() {
            return streetNode;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public void setRealName(final String realName) {
            this.realName = realName;
        }

        public String getRealName() {
            return realName;
        }
    }
}
