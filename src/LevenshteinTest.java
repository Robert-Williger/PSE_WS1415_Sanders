public class LevenshteinTest {

    private static int weightedEditDistance(final String a, final String b) {
        final String small;
        final String big;

        if (a.length() < b.length()) {
            small = a;
            big = b;
        } else {
            small = b;
            big = a;
        }

        final int[][] distance = new int[small.length() + 1][big.length() + 1];

        for (int i = 0; i <= small.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= big.length(); j++) {
            distance[0][j] = j;
        }

        int minDist = Integer.MAX_VALUE;

        for (int j = 0; j < big.length(); j++) {
            int currentMinDist = minDist + 1;
            for (int i = 0; i < small.length(); i++) {

                final int cost = small.charAt(i) == big.charAt(j) ? 0 : 1;

                distance[i + 1][j + 1] = minimum( //
                        distance[i][j + 1] + 1, // deletion
                        distance[i + 1][j] + 1, // insertion
                        distance[i][j] + cost // substitution
                );

                if (i > 1) {
                    if (distance[i][j + 1] < currentMinDist) {
                        currentMinDist = distance[i][j + 1];
                    }
                    if (j > 1 && small.charAt(i) == big.charAt(j - 1) && small.charAt(i - 1) == big.charAt(j)) {
                        distance[i + 1][j + 1] = Math.min( //
                                distance[i - 1][j - 1] + cost, // transposition
                                distance[i + 1][j + 1] // current minimum
                                );
                    }
                }

            }

            minDist = Math.min(distance[small.length()][j + 1], minDist);
            if (currentMinDist > minDist) {
                // TODO improvement still possible by transposition?
                break;
            }
        }

        for (int i = 0; i < small.length() + 1; i++) {
            String output = "[";
            for (int j = 0; j < big.length() + 1; j++) {
                output += (distance[i][j] < 10 ? " " + distance[i][j] : distance[i][j]) + " ";
            }
            System.out.println(output + "]");
        }

        return minDist;

    }

    private static int minimum(final int a, final int b, final int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static void main(final String[] args) {
        System.out.println(weightedEditDistance("sinsheimerstrasse karls", "sinsheimer strasse karlsruhe"));
        System.out.println(weightedEditDistance("epplinger s", "eppinger weg karlsruhe"));
        System.out.println(weightedEditDistance("epplinger", "eppingerstrasse karlsruhe"));
        System.out.println(weightedEditDistance("am fsnen", "am fasanengarten karlsruhe"));
        System.out.println(weightedEditDistance("epplinger", "ettlingerstrasse karlsruhe"));
    }
}
