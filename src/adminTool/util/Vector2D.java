package adminTool.util;

public class Vector2D {

    private double x;
    private double y;

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;

    }

    public double y() {
        return y;
    }

    public double norm() {
        return Math.sqrt(dot(this, this));
    }

    public double normSq() {
        return dot(this, this);
    }

    public void setX(final double x) {
        this.x = x;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public void setVector(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public void setVector(final Vector2D v) {
        setVector(v.x, v.y);
    }

    public static double dot(final Vector2D a, final Vector2D b) {
        return a.x * b.x + a.y * b.y;
    }

    public static double cross(final Vector2D a, final Vector2D b) {
        return a.x * b.y - a.y * b.x;
    }

    public static double angle(final Vector2D a, final Vector2D b) {
        return Math.atan2(cross(a, b), dot(a, b));
    }

    public static void main(final String[] args) {
        Vector2D a = new Vector2D(2, 0);
        Vector2D b = new Vector2D(3, 0);
        System.out.println(angle(a, b) * 180 / Math.PI);
    }
}
