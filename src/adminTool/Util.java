package adminTool;

import adminTool.elements.Node;

public class Util {
    private static final int OUT_LEFT = 0b0001;
    private static final int OUT_TOP = 0b0010;
    private static final int OUT_RIGHT = 0b0100;
    private static final int OUT_BOTTOM = 0b1000;

    // test whether rectangle [1] contains rectangle [2]
    public static boolean rectangleContainsRectangle(final int minX1, final int minY1, final int maxX1, final int maxY1,
            final int minX2, final int minY2, final int maxX2, final int maxY2) {
        return minX1 <= minX2 && minY1 <= minY2 && maxX1 >= maxX2 && maxY1 >= maxY2;
    }

    public static boolean rectangleContainsPoint(final int minX, final int minY, final int maxX, final int maxY,
            final int x, final int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public static boolean polygonContainsPoint(final Node[] nodes, final double x, final double y) {
        boolean ret = false;
        for (int i = 0, j = nodes.length - 1; i < nodes.length; j = i++) {
            if (((nodes[i].getY() > y) != (nodes[j].getY() > y)) && (x < (nodes[j].getX() - nodes[i].getX())
                    * (y - nodes[i].getY()) / (nodes[j].getY() - nodes[i].getY()) + nodes[i].getX()))
                ret = !ret;
        }

        return ret;
    }

    public static boolean polygonBBContainsPoint(final Node[] nodes, final double x, final double y) {
        int minX = nodes[0].getX();
        int maxX = minX;
        int minY = nodes[0].getY();
        int maxY = minY;

        for (int i = 1; i < nodes.length; i++) {
            minX = Math.min(minX, nodes[i].getX());
            maxX = Math.max(maxX, nodes[i].getX());
            minY = Math.min(minY, nodes[i].getY());
            maxY = Math.max(maxY, nodes[i].getY());
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
}
