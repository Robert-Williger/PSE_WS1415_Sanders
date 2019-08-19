package adminTool.util;

import java.awt.geom.Point2D;
import java.util.Iterator;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;

public class ElementAdapter implements IElement {
    private final IPointAccess points;
    private MultiElement element;

    public ElementAdapter(final IPointAccess points) {
        this.points = points;
    }

    public ElementAdapter(ElementAdapter elementAdapter) {
        this(elementAdapter.points);
        setMultiElement(elementAdapter.element);
    }

    public void setMultiElement(final MultiElement element) {
        this.element = element;
    }

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public double getX(final int index) {
        return points.getX(element.getPoint(index));
    }

    @Override
    public double getY(final int index) {
        return points.getY(element.getPoint(index));
    }

    public double getLength() {
        return getLength(0, size());
    }

    // get length between nodes [from, to)
    public double getLength(final int from, final int to) {
        double totalLength = 0;

        Point2D last = new Point2D.Double(getX(from), getY(from));
        Point2D current = new Point2D.Double();
        for (int i = from + 1; i < to; i++) {
            current.setLocation(getX(i), getY(i));
            totalLength += last.distance(current);
            last.setLocation(current);
        }

        return totalLength;
    }

    @Override
    public Iterator<Point2D> iterator() {
        return new PointIterator(new ElementAdapter(this));
    }

    private static class PointIterator implements Iterator<Point2D> {
        private final IElement element;
        private int pointer;

        public PointIterator(final IElement element) {
            this.element = element;
            this.pointer = 0;
        }

        @Override
        public boolean hasNext() {
            return pointer < element.size();
        }

        @Override
        public Point2D next() {
            final Point2D ret = new Point2D.Double(element.getX(pointer), element.getY(pointer));
            ++pointer;
            return ret;
        }

    }
}
