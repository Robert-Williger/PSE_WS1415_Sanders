package adminTool;

import java.awt.geom.Point2D;
import java.util.List;

import model.elements.Node;

public class UnprocessedStreet {

    private final List<Point2D.Double> degrees;
    private final List<Node> nodes;

    private final int type;
    private final String name;

    public List<Point2D.Double> getDegrees() {
        return degrees;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public UnprocessedStreet(final List<Point2D.Double> degrees, final List<Node> nodes, final int type,
            final String name) {
        this.degrees = degrees;
        this.nodes = nodes;
        this.type = type;
        this.name = name;
    }

}
