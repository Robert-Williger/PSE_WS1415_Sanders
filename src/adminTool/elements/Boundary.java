package adminTool.elements;

import java.awt.Point;
import java.awt.Polygon;

import adminTool.elements.Node;

public class Boundary {

    private final String name;
    private final Node[][] inner;
    private final Node[][] outer;

    private Polygon[] innerPolygons;
    private Polygon[] outerPolygons;

    public Boundary(final String name, final Node[][] outer, final Node[][] inner) {
        this.name = name;
        this.inner = inner;
        this.outer = outer;
    }

    public boolean contains(final Point point) {
        if (innerPolygons == null) {
            revalidate();
        }

        for (final Polygon innerP : innerPolygons) {
            if (innerP.contains(point)) {
                return false;
            }
        }

        for (final Polygon outerP : outerPolygons) {
            if (outerP.contains(point)) {
                return true;
            }
        }

        return false;
    }

    private Polygon convert(final Node[] nodes) {
        final int[] x = new int[nodes.length];
        final int[] y = new int[x.length];
        int count = 0;
        for (final Node node : nodes) {
            x[count] = node.getX();
            y[count] = node.getY();
            ++count;
        }

        return new Polygon(x, y, count);
    }

    private void fill(Polygon[] polygons, final Node[][] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            polygons[i] = convert(nodes[i]);
        }
    }

    public void revalidate() {
        outerPolygons = new Polygon[outer.length];
        innerPolygons = new Polygon[inner.length];
        fill(outerPolygons, outer);
        fill(innerPolygons, inner);
    }

    public String getName() {
        return name;
    }
}
