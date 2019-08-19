package adminTool.quadtree;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.util.ElementAdapter;
import adminTool.util.IntersectionUtil;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class WayQuadtreePolicy extends BoundingBoxQuadtreePolicy implements IQuadtreePolicy {

    private final List<? extends MultiElement> ways;
    private final IPointAccess points;
    private final IWayWidthInfo widthInfo;

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final IWayWidthInfo widthInfo) {
        super(calculateBounds(ways, points));
        this.widthInfo = widthInfo;
        this.points = points;
        this.ways = ways;
    }

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final double[] maxWayCoordWidths) {
        this(ways, points, (index, height) -> maxWayCoordWidths[height]);
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        // respect way width by appending offset to tile borders
        final double wayWidth = widthInfo.getWidth(index, height);
        final double rectX = x - wayWidth / 2;
        final double rectY = y - wayWidth / 2;
        final double rectSize = size + wayWidth;

        if (!super.intersects(index, height, rectX, rectY, rectSize)) {
            return false;
        }

        final Rectangle2D rectangle = new Rectangle2D.Double(rectX, rectY, rectSize, rectSize);
        final MultiElement way = ways.get(index);

        int last = way.getPoint(0);
        for (int i = 1; i < way.size(); ++i) {
            final int current = way.getPoint(i);
            if (rectangle.intersectsLine(points.getX(last), points.getY(last), points.getX(current),
                    points.getY(current)))
                return true;
            last = current;
        }
        return false;
    }

    private static List<Rectangle2D> calculateBounds(final List<? extends MultiElement> elements,
            final IPointAccess points) {
        final List<Rectangle2D> bounds = new ArrayList<>(elements.size());
        final ElementAdapter adapter = new ElementAdapter(points);

        for (final MultiElement element : elements) {
            adapter.setMultiElement(element);
            final Rectangle2D bb = IntersectionUtil.getBounds(adapter);
            // avoid cases with zero width or height - BoundingBoxQuadtreePolicy always would return false
            // occuring when element is just a horizontal or vertical line
            bb.setRect(bb.getX(), bb.getY(), bb.getWidth() != 0 ? bb.getWidth() : 1,
                    bb.getHeight() != 0 ? bb.getHeight() : 1);
            bounds.add(bb);
        }
        return bounds;
    }

    @FunctionalInterface
    public interface IWayWidthInfo {
        double getWidth(final int index, final int height);
    }
}
