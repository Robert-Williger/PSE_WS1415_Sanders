package adminTool.quadtree;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import adminTool.elements.Boundary;
import adminTool.elements.IPointAccess;
import adminTool.util.ElementAdapter;
import adminTool.util.ShapeUtil;

public class BoundaryQuadtreePolicy implements IQuadtreePolicy {
    private final List<Shape> areas;

    public BoundaryQuadtreePolicy(final List<Boundary> boundaries, final IPointAccess points) {
        final ElementAdapter element = new ElementAdapter(points);

        areas = new ArrayList<>(boundaries.size());
        for (final Boundary boundary : boundaries) {
            final Area area = new Area();
            for (int i = 0; i < boundary.getOutlines(); ++i) {
                element.setMultiElement(boundary.getOutline(i));
                area.add(new Area(ShapeUtil.createClosedPath(element)));
            }
            for (int i = 0; i < boundary.getHoles(); ++i) {
                element.setMultiElement(boundary.getHole(i));
                area.add(new Area(ShapeUtil.createClosedPath(element)));
            }
            areas.add(area);
        }
    }

    @Override
    public boolean intersects(final int element, final int height, final double x, final double y, final double size) {
        return areas.get(element).intersects(x, y, size, size);
    }

}
