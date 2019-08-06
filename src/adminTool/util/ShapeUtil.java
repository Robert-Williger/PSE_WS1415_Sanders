package adminTool.util;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import adminTool.IElement;

public final class ShapeUtil {
    private ShapeUtil() {}

    public static Path2D createPath(final IElement element) {
        final Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD, element.size() + 1);
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
