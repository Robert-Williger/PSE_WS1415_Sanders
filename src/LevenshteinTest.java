public class LevenshteinTest {

    private static int weightedEditDistance(final String a, final String b) {
        final String smaller;
        final String longer;

        if (a.length() < b.length()) {
            smaller = a;
            longer = b;
        } else {
            smaller = b;
            longer = a;
        }

        final int[][] distance = new int[smaller.length() + 1][longer.length() + 1];
        final int[][] deletions = new int[smaller.length() + 1][longer.length() + 1];

        for (int i = 0; i <= smaller.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= longer.length(); j++) {
            distance[0][j] = j;
        }

        int minDist = Integer.MAX_VALUE;

        // TODO insertions --> ++deletions
        for (int j = 0; j < longer.length(); j++) {
            for (int i = 0; i < smaller.length(); i++) {

                final int cost = smaller.charAt(i) == longer.charAt(j) ? 0 : 1;

                final int deletion = distance[i][j + 1] + 1;
                final int insertion = distance[i + 1][j] + 1;
                final int substitution = distance[i][j] + cost;

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

                if ((i > 1) && (j > 1) && (smaller.charAt(i) == longer.charAt(j - 1))
                        && (smaller.charAt(i - 1) == longer.charAt(j))) {
                    final int transposition = distance[i - 1][j - 1] + cost;
                    if (transposition < distance[i + 1][j + 1]) {
                        distance[i + 1][j + 1] = transposition;
                        deletions[i + 1][j + 1] = deletions[i - 1][j - 1];
                    }
                }

            }

            if (j + deletions[smaller.length()][j + 1] >= smaller.length() - 1) {
                if (distance[smaller.length()][j + 1] <= minDist) {
                    minDist = distance[smaller.length()][j + 1];
                } else {
                    // TODO return
                    break;
                }
            }

        }

        for (int i = 0; i < smaller.length() + 1; i++) {
            String output = "[";
            for (int j = 0; j < longer.length() + 1; j++) {
                output += (distance[i][j] < 10 ? " " + distance[i][j] : distance[i][j]) + " ";
            }
            System.out.println(output + "]");
        }

        return minDist;

    }

    public static void main(final String[] args) {
        System.out.println(weightedEditDistance("", ""));
        System.out.println(weightedEditDistance("epplinger s", "eppinger weg karlsruhe"));
        System.out.println(weightedEditDistance("epplinger", "eppingerstrasse karlsruhe"));
        System.out.println(weightedEditDistance("am fsnen", "am fasanengarten karlsruhe"));
        System.out.println(weightedEditDistance("epplinger", "ettlingerstrasse karlsruhe"));
    }
}
