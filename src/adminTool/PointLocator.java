package adminTool;

import java.awt.geom.Point2D;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;

public class PointLocator {
    private final IPointAccess points;

    private double x;
    private double y;
    private int segment;
    private double offset;

    public PointLocator(final IPointAccess points) {
        this.points = points;
    }

    public void locate(final MultiElement element, final double desLength) {
        locate(element, 0, 0, desLength);
    }

    public void locate(final MultiElement element, final int index, final double curLength, final double desLength) {
        final Point2D last = new Point2D.Double(points.getX(element.getPoint(index)),
                points.getY(element.getPoint(index)));
        final Point2D current = new Point2D.Double();
        double totalLength = curLength;

        for (int i = index + 1; i < element.size(); i++) {
            current.setLocation(points.getX(element.getPoint(i)), points.getY(element.getPoint(i)));
            final double distance = last.distance(current);

            if (totalLength + distance >= desLength) {
                offset = (desLength - totalLength) / distance;
                segment = i - 1;
                x = last.getX() + offset * (current.getX() - last.getX());
                y = last.getY() + offset * (current.getY() - last.getY());
                return;
            }

            totalLength += distance;
            last.setLocation(current);
        }

        offset = 1;
        segment = element.size() - 2;
        x = current.getX();
        y = current.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSegment() {
        return segment;
    }

    public double getOffset() {
        return offset;
    }

}
