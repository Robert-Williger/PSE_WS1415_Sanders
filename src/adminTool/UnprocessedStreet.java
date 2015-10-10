package adminTool;

import java.awt.geom.Point2D;

import model.elements.MultiElement;
import model.elements.Node;

public class UnprocessedStreet extends MultiElement {

    private final Point2D[] degrees;

    private final int type;
    private final String name;

    public Point2D[] getDegrees() {
        return degrees;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public UnprocessedStreet(final Point2D[] degrees, final Node[] nodes, final int type, final String name) {
        super(nodes);
        this.degrees = degrees;
        this.type = type;
        this.name = name;
    }

}
