package adminTool.util;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import adminTool.IElement;

public final class ShapeUtil {
    private ShapeUtil() {}

    public static double getLength(final IElement element) {
        double totalLength = 0;

        double lastX = element.getX(0);
        double lastY = element.getY(0);
        for (int i = 1; i < element.size(); i++) {
            double currentX = element.getX(i);
            double currentY = element.getY(i);
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return totalLength;
    }

    public static Path2D createPath(final IElement element) {
        final Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD, element.size() + 1); // + 1 for the closePath..
        path.moveTo(element.getX(0), element.getY(0));
        for (int j = 1; j < element.size(); ++j) {
            path.lineTo(element.getX(j), element.getY(j));
        }
        return path;
    }

    public static Path2D createClosedPath(final IElement element) {
        final Path2D path = createPath(element);
        path.closePath();
        return path;
    }

    public static Shape createStrokedPath(final IElement element, final float wayWidth, final int cap, final int join) {
        return createStrokedPath(element, new BasicStroke(wayWidth, cap, join));
    }

    public static Shape createStrokedPath(final IElement element, final Stroke stroke) {
        return stroke.createStrokedShape(createPath(element));
    }
}
