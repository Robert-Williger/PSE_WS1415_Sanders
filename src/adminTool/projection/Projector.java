package adminTool.projection;

import adminTool.NodeAccess;
import adminTool.elements.UnboundedPointAccess;

public class Projector {

    private final IProjection projection;
    private UnboundedPointAccess points;

    public Projector(final IProjection projection) {
        this.projection = projection;
    }

    public void performProjection(final NodeAccess nodes) {
        points = new UnboundedPointAccess(nodes.size());
        for (int i = 0; i < nodes.size(); ++i) {
            double lat = nodes.getLat(i);
            double lon = nodes.getLon(i);
            int x = projection.getX(lat, lon);
            int y = projection.getY(lat, lon);
            points.addPoint(x, y);
        }
    }

    public UnboundedPointAccess getPoints() {
        return points;
    }
}
