package adminTool.quadtree;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.util.ElementAdapter;
import adminTool.util.ShapeUtil;

import java.awt.Shape;
import java.util.List;

public class AreaQuadtreePolicy implements IQuadtreePolicy {
    private final Shape[] areas;

    public AreaQuadtreePolicy(final List<MultiElement> areas, final IPointAccess points) {
        this.areas = new Shape[areas.size()];
        final ElementAdapter element = new ElementAdapter(points);
        for (int i = 0; i < areas.size(); ++i) {
            element.setMultiElement(areas.get(i));
            this.areas[i] = ShapeUtil.createClosedPath(element);
        }
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        return areas[index].intersects(x, y, size, size);
    }
}
