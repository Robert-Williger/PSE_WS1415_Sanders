package adminTool;

import java.awt.Rectangle;
import java.util.List;

import adminTool.elements.MultiElement;

public class Util {
    private static final int OUT_LEFT = 0b0001;
    private static final int OUT_TOP = 0b0010;
    private static final int OUT_RIGHT = 0b0100;
    private static final int OUT_BOTTOM = 0b1000;

    public static Rectangle getBounds(final MultiElement element, final PointAccess points) {
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
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public static boolean polygonContainsPoint(final MultiElement element, final PointAccess points, final double x, final double y) {
        boolean ret = false;
        for (int i = 0, j = element.size() - 1; i < element.size(); j = i++) {
            if (((points.getY(element.getNode(i)) > y) != (points.getY(element.getNode(j)) > y)) && (x < (points.getX(element.getNode(j)) - points.getX(element.getNode(i)))
                    * (y - points.getY(element.getNode(i))) / (points.getY(element.getNode(j)) - points.getY(element.getNode(i))) + points.getX(element.getNode(i))))
                ret = !ret;
        }

        return ret;
    }
    
    public static boolean polygonContainsPoint(final int[] indices, final PointAccess points, final double x, final double y) {
        boolean ret = false;
        for (int i = 0, j = indices.length - 1; i < indices.length; j = i++) {
            if (((points.getY(indices[i]) > y) != (points.getY(indices[j]) > y)) && (x < (points.getX(indices[j]) - points.getX(indices[i]))
                    * (y - points.getY(indices[i])) / (points.getY(indices[j]) - points.getY(indices[i])) + points.getX(indices[i])))
                ret = !ret;
        }

        return ret;
    }

    public static boolean polygonBBContainsPoint(final int[] indices, final PointAccess points, final double x, final double y) {
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
    
    static void reverse(int array[]) {
        int temp;
        for (int i = 0; i < array.length / 2; i++) {
            temp = array[i];
            array[i] = array[array.length-1-i];
            array[array.length-1-i] = temp;
        }
    }
    
    static int[] toArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int index = 0;
        for (final int data : list) {
            ret[index++] = data;
        }
        return ret;
    }
}
