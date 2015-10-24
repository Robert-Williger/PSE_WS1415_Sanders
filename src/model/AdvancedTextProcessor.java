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
    private final TreeNode indexRoot;
    private int count;

    public AdvancedTextProcessor(final Entry[][] entries, final Label[] labels, final IMapManager manager,
            final int suggestions) {

        this.maxDistance = 3;
        this.suggestions = suggestions;

        indexRoot = setupIndex(entries, labels, manager);
        // printIndex(indexRoot, 0);
    }

    private void printIndex(final TreeNode root, final int depth) {
        String spaces = "";
        for (int i = 0; i < depth; i++) {
            spaces += "  ";
        }
        ++count;
        System.out.println(spaces + "[" + depth + "] " + root.getName());
        for (final TreeNode child : root.getChildren()) {
            printIndex(child, depth + 1);
        }
    }

    private TreeNode setupIndex(final Entry[][] entries, final Label[] labels, final IMapManager manager) {
        final TreeNode root = new TreeNode("", null, null);

        for (final Entry[] entry : entries) {
            for (int i = 0; i < entry.length; i++) {
                final String streetName = entry[i].getStreet().getName();
                final String cityName = entry[i].getCity();
                final String realName = streetName + " " + cityName;
                final StreetNode node = new StreetNode(0.5f, entry[i].getStreet());
                // TODO order by size of city
                add(normalize(realName), realName, node, root, 0);
                add(normalize(cityName + " " + streetName), realName, node, root, 0);
            }
        }

        // TODO suburbs can collide --> include city names
        for (final Label label : labels) {
            final AddressNode addressNode = manager.getAddressNode(label.getLocation());
            if (addressNode != null) {
                add(normalize(label.getName()), label.getName(), addressNode.getStreetNode(), root, 0);
            }
        }

        return root;
    }

    // TODO Epplinger Straße --> Ettlinger Straße instead of Eppinger Straße!?
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
        final BoundedHeap<Tuple> tuples = new BoundedHeap<Tuple>(suggestions);
        final String normalizedAddress = normalize(address);

        count = 0;
        suggest(normalizedAddress, indexRoot, tuples);
        System.out.println(count);

        int length = Math.min(suggestions, tuples.size());

        final String[] choice = new String[length];
        for (int i = 0; i < length; i++) {
            choice[length - i - 1] = tuples.deleteMax().getName();
        }

        return Arrays.asList(choice);
    }

    private void suggest(final String input, final TreeNode node, final BoundedHeap<Tuple> heap) {
        int distance = prefixEditDistance(input, node.getName());

        ++count;

        if (distance <= maxDistance) {
            if (heap.size() == suggestions) {
                Tuple min = heap.max();
                if (distance > min.getDistance() || distance == min.getDistance()
                        && node.getName().length() > min.getName().length()) {
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
        return parse(normalize(address), indexRoot, 0);// nodeMap.get(normalize(address));
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

        final int[][] distance = new int[a.length() + 1][b.length() + 1];
        final int[][] deletions = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            distance[0][j] = j;
        }

        int minDist = Integer.MAX_VALUE;

        for (int j = 0; j < b.length(); j++) {
            for (int i = 0; i < a.length(); i++) {

                final int cost = a.charAt(i) == b.charAt(j) ? 0 : 1;

                final int deletion = distance[i][j + 1] + 1;
                final int insertion = distance[i + 1][j] + 1;
                final int substitution = distance[i][j] + cost;

                // TODO improve code!
                if (deletion <= insertion) {
                    if (deletion <= substitution) {
                        deletions[i + 1][j + 1] = deletions[i][j + 1] + 1;
                        distance[i + 1][j + 1] = deletion;
                    } else {
                        deletions[i + 1][j + 1] = deletions[i][j];
                        distance[i + 1][j + 1] = substitution;
                    }
                } else {
                    if (insertion <= substitution) {
                        deletions[i + 1][j + 1] = deletions[i + 1][j] - 1;
                        distance[i + 1][j + 1] = insertion;
                    } else {
                        deletions[i + 1][j + 1] = deletions[i][j];
                        distance[i + 1][j + 1] = substitution;
                    }
                }

                // TODO improve code!
                if ((i > 1) && (j > 1) && (a.charAt(i) == b.charAt(j - 1)) && (a.charAt(i - 1) == b.charAt(j))) {
                    final int transposition = distance[i - 1][j - 1] + cost;
                    if (transposition < distance[i + 1][j + 1]) {
                        distance[i + 1][j + 1] = transposition;
                        deletions[i + 1][j + 1] = deletions[i - 1][j - 1];
                    }
                }

            }

            if (j + deletions[a.length()][j + 1] >= a.length() - 1) {
                if (distance[a.length()][j + 1] <= minDist) {
                    minDist = distance[a.length()][j + 1];
                } else {
                    // TODO minDist may improve later..
                    return minDist;
                }
            }

        }

        return minDist;
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
