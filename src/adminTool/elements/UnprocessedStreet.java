package adminTool.elements;

import java.awt.geom.Point2D;

public class UnprocessedStreet extends Way {

    private final Point2D[] degrees;
    private final boolean oneway;

    public Point2D[] getDegrees() {
        return degrees;
    }

    public UnprocessedStreet(final Point2D[] degrees, final Node[] nodes, final int type, final String name,
            final boolean oneway) {
        super(nodes, type, name);
        this.oneway = oneway;
        this.degrees = degrees;
    }

    public boolean isOneway() {
        return oneway;
    }
}
