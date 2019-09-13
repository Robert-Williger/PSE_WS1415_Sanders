package adminTool;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;

public class MapBoundsCalculator {
    private IPointAccess points;
    private Range2D range;

    public void calculateBounds(final IPointAccess points, final Collection<Way> ways,
            final Collection<MultiElement> areas) {
        this.points = points;
        range = new Range2D(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        // updateRange(ways);
        updateRange(areas);
    }

    public Rectangle2D getBounds() {
        return range.toRectangle();
    }

    private void updateRange(final Collection<? extends MultiElement> elements) {
        for (final MultiElement element : elements) {
            updateRange(element);
        }
    }

    private void updateRange(final MultiElement element) {
        for (int i = 0; i < element.size(); ++i) {
            final int node = element.getPoint(i);
            range.add(points.getX(node), points.getY(node));
        }
    }

    private static class Range2D {
        private double minX;
        private double minY;
        private double maxX;
        private double maxY;

        public Range2D(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public void add(final double x, final double y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        public Rectangle2D toRectangle() {
            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        }
    }
}
