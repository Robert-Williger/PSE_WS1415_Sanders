package adminTool;

import java.awt.geom.Dimension2D;
import java.util.Collection;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;

public class Aligner {
    private IPointAccess points;
    private Range2D range;

    public void performAlignment(final IPointAccess points, final Collection<Way> ways,
            final Collection<MultiElement> areas) {
        this.points = points;
        range = new Range2D(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        updateRange(ways);
        updateRange(areas);
        updatePoints();
    }

    public Dimension getSize() {
        return range.toDimension();
    }

    private void updateRange(final Collection<? extends MultiElement> elements) {
        for (final MultiElement element : elements) {
            updateRange(element);
        }
    }

    private void updateRange(final MultiElement element) {
        for (int i = 0; i < element.size(); ++i) {
            final int node = element.getNode(i);
            range.add(points.getX(node), points.getY(node));
        }
    }

    private void updatePoints() {
        for (int i = 0; i < points.size(); ++i) {
            points.set(i, points.getX(i) - range.minX, points.getY(i) - range.minY);
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

        public Dimension toDimension() {
            return new Dimension(maxX - minX, maxY - minY);
        }
    }

    private static class Dimension extends Dimension2D {
        private double width;
        private double height;

        public Dimension(final double width, final double height) {
            setSize(width, height);
        }

        @Override
        public double getWidth() {
            return width;
        }

        @Override
        public double getHeight() {
            return height;
        }

        @Override
        public void setSize(double width, double height) {
            this.width = width;
            this.height = height;
        }

    }
}
