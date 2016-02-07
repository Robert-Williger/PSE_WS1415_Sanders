import java.util.Arrays;

public class PermutationTest {

    private static void permutate(final int n) {
        int[] used = new int[n];
        for (int i = 0; i < used.length; i++) {
            used[i] = i;
        }

        permutate(used, 0);
    }

    // private static void permutate(final int[] permutation, final int depth) {
    //
    // if (depth == permutation.length) {
    // System.out.println(Arrays.toString(permutation));
    // return;
    // }
    // for (int i = 0; i < permutation.length; i++) {
    // if (permutation[i] == -1) {
    // permutation[i] = depth;
    // permutate(permutation, depth + 1);
    // permutation[i] = -1;
    // }
    // }
    // }

    private static void permutate(final int[] permutation, final int depth) {
        if (depth == permutation.length - 1) {
            System.out.println(depth + ", " + Arrays.toString(permutation));
            return;
        }

        permutate(permutation, depth + 1);

        for (int i = depth + 1; i < permutation.length; i++) {
            swap(depth, i, permutation);
            permutate(permutation, depth + 1);
            swap(depth, i, permutation);
        }
    }

    private static void swap(final int i, final int j, final int[] array) {
        final int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void main(String[] args) {
        permutate(3);
    }
}
