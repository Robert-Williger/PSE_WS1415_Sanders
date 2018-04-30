package adminTool.util;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import adminTool.IPointAccess;
import adminTool.elements.MultiElement;
import util.IntList;

public class IntersectionUtil {
    public static double EPSILON = 1E-5;

    private static final int OUT_LEFT = 0b0001;
    private static final int OUT_TOP = 0b0010;
    private static final int OUT_RIGHT = 0b0100;
    private static final int OUT_BOTTOM = 0b1000;

    public static Rectangle getBounds(final MultiElement element, final IPointAccess points) {
        int minX = points.getX(element.getNode(0));
        int maxX = minX;
        int minY = points.getY(element.getNode(0));
        int maxY = minY;

        for (int i = 1; i < element.size(); i++) {
            minX = Math.min(minX, points.getX(element.getNode(i)));
            maxX = Math.max(maxX, points.getX(element.getNode(i)));
            minY = Math.min(minY, points.getY(element.getNode(i)));
            maxY = Math.max(maxY, points.getY(element.getNode(i)));
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    // test whether rectangle [1] contains rectangle [2]
    public static boolean rectangleContainsRectangle(final int minX1, final int minY1, final int maxX1, final int maxY1,
            final int minX2, final int minY2, final int maxX2, final int maxY2) {
        return minX1 <= minX2 && minY1 <= minY2 && maxX1 >= maxX2 && maxY1 >= maxY2;
    }

    public static boolean rectangleContainsPoint(final int minX, final int minY, final int maxX, final int maxY,
            final int x, final int y) {
        return x >= minX && x < maxX && y >= minY && y < maxY;
    }

    public static boolean polygonContainsPoint(final MultiElement element, final IPointAccess points, final double x,
            final double y) {
        boolean ret = false;
        for (int i = 0, j = element.size() - 1; i < element.size(); j = i++) {
            if (((points.getY(element.getNode(i)) > y) != (points.getY(element.getNode(j)) > y))
                    && (x < (points.getX(element.getNode(j)) - points.getX(element.getNode(i)))
                            * (y - points.getY(element.getNode(i)))
                            / (points.getY(element.getNode(j)) - points.getY(element.getNode(i)))
                            + points.getX(element.getNode(i))))
                ret = !ret;
        }

        return ret;
    }

    public static boolean polygonContainsPoint(final IntList indices, final IPointAccess points, final double x,
            final double y) {
        boolean ret = false;
        for (int i = 0, j = indices.size() - 1; i < indices.size(); j = i++) {
            if (((points.getY(indices.get(i)) > y) != (points.getY(indices.get(j)) > y))
                    && (x < (points.getX(indices.get(j)) - points.getX(indices.get(i)))
                            * (y - points.getY(indices.get(i)))
                            / (points.getY(indices.get(j)) - points.getY(indices.get(i)))
                            + points.getX(indices.get(i))))
                ret = !ret;
        }

        return ret;
    }

    public static Point2D calculatePointInPolygon(final IntList poly, final IPointAccess points) {
        for (int i = 0; i < poly.size(); ++i) {
            final int i2 = (i + 1) % poly.size();
            final int i3 = (i + 2) % poly.size();
            double x = (points.getX(poly.get(i)) + points.getX(poly.get(i2)) + points.getX(poly.get(i3))) / 3.;
            double y = (points.getY(poly.get(i)) + points.getY(poly.get(i2)) + points.getY(poly.get(i3))) / 3.;
            if (polygonContainsPoint(poly, points, x, y)) {
                return new Point2D.Double(x, y);
            }
        }
        return null;
    }

    public static boolean polygonBBContainsPoint(final int[] indices, final IPointAccess points, final double x,
            final double y) {
        int minX = points.getX(indices[0]);
        int maxX = minX;
        int minY = points.getY(indices[0]);
        int maxY = minY;

        for (int i = 1; i < indices.length; i++) {
            minX = Math.min(minX, points.getX(indices[i]));
            maxX = Math.max(maxX, points.getX(indices[i]));
            minY = Math.min(minY, points.getY(indices[i]));
            maxY = Math.max(maxY, points.getY(indices[i]));
        }

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public static boolean rectangleIntersectsSegment(double rectX, double rectY, double rectWidth, double rectHeight,
            double lineX1, double lineY1, double lineX2, double lineY2) {
        int out1, out2;
        if ((out2 = outcode(rectX, rectY, rectWidth, rectHeight, lineX2, lineY2)) == 0) {
            return true;
        }
        while ((out1 = outcode(rectX, rectY, rectWidth, rectHeight, lineX1, lineY1)) != 0) {
            if ((out1 & out2) != 0) {
                return false;
            }
            if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
                double x = ((out1 & OUT_RIGHT) != 0) ? rectX + rectWidth : rectX;
                lineY1 = lineY1 + (x - lineX1) * (lineY2 - lineY1) / (lineX2 - lineX1);
                lineX1 = x;
            } else {
                double y = ((out1 & OUT_BOTTOM) != 0) ? rectY + rectHeight : rectY;
                lineX1 = lineX1 + (y - lineY1) * (lineX2 - lineX1) / (lineY2 - lineY1);
                lineY1 = y;
            }
        }
        return true;
    }

    public static Point2D segmentIntersectsSegment(double x1, double y1, double x2, double y2, double x3, double y3,
            double x4, double y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= -EPSILON && ua <= 1.f + EPSILON && ub >= -EPSILON && ub <= 1.f + EPSILON) {
            return new Point2D.Double(x1 + ua * (x2 - x1), y1 + ua * (y2 - y1));
        }

        return null;
    }

    public static Point2D segmentIntersectsSegment(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        return segmentIntersectsSegment(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(),
                p4.getY());
    }

    public static Point2D segmentIntersectsSegment(Line2D l1, Line2D l2) {
        return segmentIntersectsSegment(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(),
                l2.getX2(), l2.getY2());
    }

    public static boolean lineIntersectsline(double x1, double y1, double x2, double y2, double x3, double y3,
            double x4, double y4, double[] offsets) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return false;
        }
        offsets[0] = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        offsets[1] = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        return true;
    }

    public static boolean lineIntersectsline(Point2D p1, Point2D p2, Point2D p3, Point2D p4, double[] offsets) {
        return lineIntersectsline(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(),
                p4.getY(), offsets);
    }

    public static boolean lineIntersectsline(Line2D l1, Line2D l2, double[] offsets) {
        return lineIntersectsline(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(), l2.getX2(),
                l2.getY2(), offsets);
    }

    public static boolean segmentIntersectsSegment(double x1, double y1, double x2, double y2, double x3, double y3,
            double x4, double y4, double[] offsets) {
        boolean ret = lineIntersectsline(x1, y1, x2, y2, x3, y3, x4, y4, offsets);
        return ret && inIntervall(offsets[0], 0, 1) && inIntervall(offsets[1], 0, 1);
    }

    public static boolean segmentIntersectsSegment(Point2D p1, Point2D p2, Point2D p3, Point2D p4, double[] offsets) {
        return segmentIntersectsSegment(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(),
                p4.getY(), offsets);
    }

    public static boolean segmentIntersectsSegment(Line2D l1, Line2D l2, double[] offsets) {
        return segmentIntersectsSegment(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(),
                l2.getX2(), l2.getY2(), offsets);
    }

    // the double for width and height avoids integer-overflows.
    private static int outcode(final double rectX, final double rectY, final double rectWidth, final double rectHeight,
            final double pointX, final double pointY) {
        int out = 0;
        if (pointX < rectX) {
            out |= OUT_LEFT;
        } else if (pointX > rectX + rectWidth) {
            out |= OUT_RIGHT;
        }

        if (pointY < rectY) {
            out |= OUT_TOP;
        } else if (pointY > rectY + rectHeight) {
            out |= OUT_BOTTOM;
        }

        return out;
    }

    public static boolean inIntervall(final double value, final double from, final double to) {
        return value >= -EPSILON + from && value <= to + EPSILON;
    }
}
