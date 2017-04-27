
public class Test {

    public static void main(final String[] args) {
        System.out.println(Integer.MIN_VALUE - 1);
        polygonContainsPoint(9, 9);
    }

    public static void polygonContainsPoint(final double x, final double y) {
        final int y1 = 0;
        final int y2 = 0;
        final int x1 = 0;
        final int x2 = 0;

        System.out.println((((y1 > y) != (y2 > y)) && (x < (x2 - x1) * (y - y1) / (y2 - y1) + y1)));

    }
}