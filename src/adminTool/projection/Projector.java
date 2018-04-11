package adminTool.projection;

import java.awt.Dimension;

import adminTool.AABB;
import adminTool.NodeAccess;
import adminTool.BoundedPointAccess;
import adminTool.IPointAccess;

public class Projector {

    private final NodeAccess nodes;
    private final IProjection projection;
    private final BoundedPointAccess points;
    private final Dimension size;

    public Projector(final NodeAccess nodes, final IProjection projection) {
        this.nodes = nodes;
        this.projection = projection;
        points = new BoundedPointAccess(nodes.size());
        size = new Dimension();
    }

    public void performProjection() {
        final AABB aabb = new AABB(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (int i = 0; i < nodes.size(); ++i) {
            double lat = nodes.getLat(i);
            double lon = nodes.getLon(i);
            int x = projection.getX(lat, lon);
            int y = projection.getY(lat, lon);
            points.setPoint(i, x, y);

            aabb.setMinX(Math.min(aabb.getMinX(), x));
            aabb.setMinY(Math.min(aabb.getMinY(), y));
            aabb.setMaxX(Math.max(aabb.getMaxX(), x));
            aabb.setMaxY(Math.max(aabb.getMaxY(), y));
        }
        for (int i = 0; i < nodes.size(); ++i) {
            points.setPoint(i, points.getX(i) - aabb.getMinX(), points.getY(i) - aabb.getMinY());
        }
        size.setSize(aabb.getMaxX() - aabb.getMinX(), aabb.getMaxY() - aabb.getMinY());
    }

    public IPointAccess getPoints() {
        return points;
    }

    public Dimension getSize() {
        return size;
    }
}
