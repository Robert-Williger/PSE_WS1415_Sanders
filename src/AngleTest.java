public class AngleTest {

    private static int getAngle(final double x, final double y) {
        return (int) (Math.atan(y / x) * 180 / Math.PI);
    }

    public static void main(String[] args) {
        System.out.println(getAngle(0, 1));
    }
}
