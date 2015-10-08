package adminTool;

import java.awt.geom.Point2D;

import model.elements.Node;

public class UnprocessedStreet {

    private final Point2D[] degrees;
    private final Node[] nodes;

    private final int type;
    private final String name;

    public Point2D[] getDegrees() {
        return degrees;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public UnprocessedStreet(final Point2D[] degrees, final Node[] nodes, final int type, final String name) {
        this.degrees = degrees;
        this.nodes = nodes;
        this.type = type;
        this.name = name;
    }

}
