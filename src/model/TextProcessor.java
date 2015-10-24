package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.elements.Label;
import model.elements.Street;
import model.elements.StreetNode;
import model.map.AddressNode;
import model.map.IMapManager;

/*
 * First Implementation of TextProcessing.
 *
 */
public class TextProcessor implements ITextProcessor {
    private final HashMap<String, StreetNode> nodeMap;
    private final int suggestions;
    private final Tuple[] distanceArray;

    /**
     * TextProcessing-Constructor.
     * 
     * @param labels
     * @param manager
     * 
     * @param nodeMap
     *            HashMap, which maps every address on a StreetNode.
     * @param suggestions
     *            Number of suggestions, which will be returned as list.
     */
    public TextProcessor(final Entry[][] entries, final Label[] labels, final IMapManager manager, final int suggestions) {

        this.nodeMap = new HashMap<String, StreetNode>(entries.length);
        for (final Entry[] entry : entries) {
            final Street street = entry[0].getStreet();
            nodeMap.put(normalize(street.getName()), new StreetNode(0.5f, street));
        }

        for (final Label label : labels) {
            final AddressNode addressNode = manager.getAddressNode(label.getLocation());
            if (addressNode != null) {
                nodeMap.put(label.getName(), addressNode.getStreetNode());
            }
        }

        this.suggestions = suggestions;
        this.distanceArray = new Tuple[nodeMap.size()];
    }

    @Override
    public List<String> suggest(final String address) {

        final String normalizedAddress = normalize(address);
        int index = -1;
        for (final Map.Entry<String, StreetNode> entry : nodeMap.entrySet()) {

            int length = normalizedAddress.length();
            if (length > entry.getKey().length()) {
                length = entry.getKey().length();
            }

            final int distance = weightedEditDistance(normalizedAddress, entry.getKey().substring(0, length));

            final Tuple t = new Tuple(entry.getValue().getStreet().getName(), distance);

            distanceArray[++index] = t;
        }
        // Sorts ascending by editDistance
        Arrays.sort(distanceArray);

        // Prevents OutOfBoundsException, if there are only few addresses.
        int length = Math.min(suggestions, distanceArray.length);

        final List<String> stringList = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            stringList.add(distanceArray[i].getName());
        }

        return stringList;

    }

    @Override
    public StreetNode parse(final String address) {
        return nodeMap.get(normalize(address));
    }

    /*
     * Implementation of edit Distance with swap-cost 1 instead of 2 using
     * Damerau-Levensthein.
     * http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
     * includes pseudocode.
     */
    private static int weightedEditDistance(final String a, final String b) {
        final int[][] distance = new int[a.length() + 1][b.length() + 1];
        int cost;

        for (int i = 0; i <= a.length(); i++) {
            distance[i][0] = i;

        }
        for (int j = 0; j <= b.length(); j++) {
            distance[0][j] = j;

        }

        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {

                if (a.charAt(i) == b.charAt(j)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                distance[i + 1][j + 1] = minimum(distance[i][j + 1] + 1, distance[i + 1][j] + 1, distance[i][j] + cost);

                if ((i > 1) && (j > 1) && (a.charAt(i) == b.charAt(j - 1)) && (a.charAt(i - 1) == b.charAt(j))) {
                    distance[i + 1][j + 1] = Math.min(distance[i + 1][j + 1], distance[i - 1][j - 1] + cost); // transposition
                }

            }

        }

        return distance[a.length()][b.length()];

    }

    // Minimum of three Integers. Needed for editDistance.
    private static int minimum(final int a, final int b, final int c) {
        return Math.min(Math.min(a, b), c);
    }

    // Inner class: (String, distance)
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
            final Tuple t = (Tuple) o;
            return java.lang.Integer.compare(d, t.getDistance());
        }

    }

    /*
     * Normalizes a String. Lowercase, ä, ü, ö, ß.
     */
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

}
