package adminTool;

import java.awt.Dimension;
import java.util.Collection;

import adminTool.elements.MultiElement;
import adminTool.elements.Street;

public class Aligner {
    private final UnboundedPointAccess points;
    private final AABB aabb;

    public Aligner(final UnboundedPointAccess points) {
        this.points = points;
        aabb = new AABB(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void performAlignment(final Collection<Street> ways, final Collection<MultiElement> areas) {
        updateAABB(ways);
        updateAABB(areas);
        updatePoints();
    }

    public UnboundedPointAccess getPoints() {
        return points;
    }

    public Dimension getSize() {
        return new Dimension(aabb.getMaxX() - aabb.getMinX(), aabb.getMaxY() - aabb.getMinY());
    }

    private void updatePoints() {
        for (int i = 0; i < points.getPoints(); ++i) {
            points.setPoint(i, points.getX(i) - aabb.getMinX(), points.getY(i) - aabb.getMinY());
        }
    }

    private void updateAABB(final Collection<? extends MultiElement> elements) {
        for (final MultiElement element : elements) {
            updateAABB(element);
        }
    }

    private void updateAABB(final MultiElement element) {
        for (int i = 0; i < element.size(); ++i) {
            final int node = element.getNode(i);
            aabb.setMinX(Math.min(aabb.getMinX(), points.getX(node)));
            aabb.setMinY(Math.min(aabb.getMinY(), points.getY(node)));
            aabb.setMaxX(Math.max(aabb.getMaxX(), points.getX(node)));
            aabb.setMaxY(Math.max(aabb.getMaxY(), points.getY(node)));
        }
    }

}
