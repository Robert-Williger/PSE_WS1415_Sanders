package adminTool.util;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import util.IntList;

public final class ShapeUtil {
    private ShapeUtil() {}

    public static double getLength(final IntList element, final IPointAccess points) {
        double totalLength = 0;

        int lastX = points.getX(element.get(0));
        int lastY = points.getY(element.get(0));
        for (int i = 1; i < element.size(); i++) {
            int currentX = points.getX(element.get(i));
            int currentY = points.getY(element.get(i));
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return totalLength;
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

    public static Shape createShape(final IPointAccess points, final MultiElement element) {
        final Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD, element.size() + 1); // + 1 for the closePath..
        path.moveTo(points.getX(element.getNode(0)), points.getY(element.getNode(0)));
        for (int j = 1; j < element.size(); ++j) {
            final int point = element.getNode(j);
            path.lineTo(points.getX(point), points.getY(point));
        }
        path.closePath();
        return path;
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
