package adminTool.quadtree;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.labeling.roadGraph.ElementAdapter;
import adminTool.util.ShapeUtil;

import java.awt.Shape;
import java.util.List;

public class WayQuadtreePolicy implements IQuadtreePolicy {

    private final Shape[] ways;
    private final IWayWidthInfo widthInfo;

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final IWayWidthInfo widthInfo) {
        this.widthInfo = widthInfo;
        this.ways = new Shape[ways.size()];
        final ElementAdapter element = new ElementAdapter(points);
        for (int i = 0; i < ways.size(); ++i) {
            element.setMultiElement(ways.get(i));
            this.ways[i] = ShapeUtil.createPath(element);
        }
    }

    public WayQuadtreePolicy(final List<? extends MultiElement> ways, final IPointAccess points,
            final double[] maxWayCoordWidths) {
        this(ways, points, (index, height) -> maxWayCoordWidths[height]);
    }

    @Override
    public boolean intersects(final int index, final int height, final double x, final double y, final double size) {
        // respect way width by appending offset to tile borders
        final double maxWayCoordWidth = widthInfo.getWidth(index, height);
        final double rectX = x - maxWayCoordWidth / 2;
        final double rectY = y - maxWayCoordWidth / 2;
        final double rectSize = size + maxWayCoordWidth;

        return ways[index].intersects(rectX, rectY, rectSize, rectSize);
    }

    @FunctionalInterface
    public interface IWayWidthInfo {
        double getWidth(final int index, final int height);
    }
}
