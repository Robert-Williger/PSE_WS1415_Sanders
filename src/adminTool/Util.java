package adminTool;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import adminTool.elements.MultiElement;
import util.IntList;

public class Util {
    private static double EPSILON = 1E-5;

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
            double x = (points.getX(poly.get(i)) + points.getX(poly.get(i + 1)) + points.getX(poly.get(i + 2))) / 3.;
            double y = (points.getY(poly.get(i)) + points.getY(poly.get(i + 1)) + points.getY(poly.get(i + 2))) / 3.;
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

    public static boolean rectangleIntersectsLine(double rectX, double rectY, double rectWidth, double rectHeight,
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

    public static Point2D lineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
            double y4) {
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

    public static boolean lineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
            double y4, double[] offsets) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return false;
        }
        offsets[0] = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        offsets[1] = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (offsets[0] >= -EPSILON && offsets[0] <= 1.f + EPSILON && offsets[1] >= -EPSILON
                && offsets[1] <= 1.f + EPSILON) {
            return true;
        }

        return false;
    }

    public static boolean lineIntersectsLine(double x1, double y1, double x2, double y2, double x3, double y3,
            double x4, double y4) {
        return lineIntersection(x1, y1, x2, y2, x3, y3, x4, y4) != null;
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

    public static double getLength(final MultiElement element, final IPointAccess points) {
        double totalLength = 0;

        int lastX = points.getX(element.getNode(0));
        int lastY = points.getY(element.getNode(0));
        for (int i = 1; i < element.size(); i++) {
            int currentX = points.getX(element.getNode(i));
            int currentY = points.getY(element.getNode(i));
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return totalLength;
    }

    public static Shape createStrokedShape(final IPointAccess points, final MultiElement element, final float wayWidth,
            final int cap, final int join) {
        return createStrokedShape(points, element, new BasicStroke(wayWidth, cap, join));
    }

    public static Shape createStrokedShape(final IPointAccess points, final MultiElement element, final Stroke stroke) {
        Path2D path = new Path2D.Float();
        int current = element.getNode(0);
        path.moveTo(points.getX(current), points.getY(current));
        for (int i = 1; i < element.size(); ++i) {
            current = element.getNode(i);
            path.lineTo(points.getX(current), points.getY(current));
        }
        return stroke.createStrokedShape(path);
    }
}
