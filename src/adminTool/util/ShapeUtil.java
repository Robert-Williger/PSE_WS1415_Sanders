package adminTool.util;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import adminTool.IPointAccess;
import adminTool.elements.MultiElement;
import util.IntList;

public final class ShapeUtil {
    private ShapeUtil() {
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

    public static Shape createStrokedShape(final IPointAccess points, final IntList element, final Stroke stroke) {
        Path2D path = new Path2D.Float();
        int current = element.get(0);
        path.moveTo(points.getX(current), points.getY(current));
        for (int i = 1; i < element.size(); ++i) {
            current = element.get(i);
            path.lineTo(points.getX(current), points.getY(current));
        }
        return stroke.createStrokedShape(path);
    }
}
