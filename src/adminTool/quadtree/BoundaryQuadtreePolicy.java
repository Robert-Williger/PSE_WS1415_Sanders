package adminTool.quadtree;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.elements.Boundary;
import adminTool.util.ShapeUtil;

public class BoundaryQuadtreePolicy implements IQuadtreePolicy {
    private final List<Area> areas;

    public BoundaryQuadtreePolicy(final List<Boundary> boundaries, final IPointAccess points) {
        areas = new ArrayList<>(boundaries.size());
        for (final Boundary boundary : boundaries) {
            final Area area = new Area();
            for (int i = 0; i < boundary.getOutlines(); ++i) {
                area.add(new Area(ShapeUtil.createShape(points, boundary.getOutline(i))));
            }
            for (int i = 0; i < boundary.getHoles(); ++i) {
                area.subtract(new Area(ShapeUtil.createShape(points, boundary.getHole(i))));
            }
            areas.add(area);
        }
    }

    @Override
    public boolean intersects(final int element, final int height, final int x, final int y, final int size) {
        return areas.get(element).intersects(x, y, size, size);
    }

}
