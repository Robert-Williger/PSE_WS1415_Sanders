package adminTool.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class IntersectionUtil {
    public static double EPSILON = 1E-7;

    private static final int OUT_LEFT = 0b0001;
    private static final int OUT_TOP = 0b0010;
    private static final int OUT_RIGHT = 0b0100;
    private static final int OUT_BOTTOM = 0b1000;

    public static Rectangle2D getBounds(final IElement element) {
        double minX = element.getX(0);
        double maxX = minX;
        double minY = element.getY(0);
        double maxY = minY;

        for (int i = 1; i < element.size(); i++) {
            minX = Math.min(minX, element.getX(i));
            maxX = Math.max(maxX, element.getX(i));
            minY = Math.min(minY, element.getY(i));
            maxY = Math.max(maxY, element.getY(i));
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    // test whether rectangle [1] contains rectangle [2]
    public static boolean rectangleContainsRectangle(final double minX1, final double minY1, final double maxX1,
            final double maxY1, final double minX2, final double minY2, final double maxX2, final double maxY2) {
        return minX1 <= minX2 && minY1 <= minY2 && maxX1 >= maxX2 && maxY1 >= maxY2;
    }

    public static boolean rectangleContainsPoint(final double minX, final double minY, final double maxX,
            final double maxY, final double x, final double y) {
        return x >= minX && x < maxX && y >= minY && y < maxY;
    }

    public static boolean polygonContainsPoint(final IElement element, final double x, final double y) {
        boolean ret = false;
        for (int i = 0, j = element.size() - 1; i < element.size(); j = i++) {
            if (((element.getY(i) > y) != (element.getY(j) > y)) && (x < (element.getX(j) - element.getX(i))
                    * (y - element.getY(i)) / (element.getY(j) - element.getY(i)) + element.getX(i)))
                ret = !ret;
        }

        return ret;
    }

    public static Point2D calculatePointInPolygon(final IElement element) {
        for (int i1 = 0; i1 < element.size(); ++i1) {
            final int i2 = (i1 + 1) % element.size();
            final int i3 = (i1 + 2) % element.size();
            double x = (element.getX(i1) + element.getX(i2) + element.getX(i3)) / 3.;
            double y = (element.getY(i1) + element.getY(i2) + element.getY(i3)) / 3.;
            if (polygonContainsPoint(element, x, y)) {
                return new Point2D.Double(x, y);
            }
        }
        return null;
    }

    public static boolean polygonBBContainsPoint(final IElement element, final double x, final double y) {
        double minX = element.getX(0);
        double maxX = minX;
        double minY = element.getY(0);
        double maxY = minY;

        for (int i = 1; i < element.size(); i++) {
            minX = Math.min(minX, element.getX(i));
            maxX = Math.max(maxX, element.getX(i));
            minY = Math.min(minY, element.getY(i));
            maxY = Math.max(maxY, element.getY(i));
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
        return inIntervall(value, from, to, EPSILON);
    }

    public static boolean inIntervall(final double value, final double from, final double to, final double epsilon) {
        return value >= -epsilon + from && value <= to + epsilon;
    }

}
