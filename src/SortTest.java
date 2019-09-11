import java.util.Arrays;

public class SortTest {

    public static void main(String[] args) {
        int[] data = new int[] { 1, 5, 8, 2, 4, 3 };
        int[] ret = Arrays.stream(data, 1, 6).boxed().sorted((a, b) -> a.compareTo(b)).mapToInt(i -> i).toArray();
        System.out.println(Arrays.toString(ret));
    }
}
