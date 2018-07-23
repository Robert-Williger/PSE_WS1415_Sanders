package adminTool.projection;

import adminTool.elements.IPointAccess;

public class Projector {

    private final IProjection projection;

    public Projector(final IProjection projection) {
        this.projection = projection;
    }

    public void performProjection(final IPointAccess points) {
        for (int i = 0; i < points.size(); ++i) {
            double lat = points.getX(i);
            double lon = points.getY(i);
            double x = projection.getX(lat, lon);
            double y = projection.getY(lat, lon);
            points.set(i, x, y);
        }
    }
}
