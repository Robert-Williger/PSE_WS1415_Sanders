package adminTool.util;

import java.awt.geom.Point2D;

public interface IElement extends Iterable<Point2D> {
    int size();

    double getX(int index);

    double getY(int index);
}