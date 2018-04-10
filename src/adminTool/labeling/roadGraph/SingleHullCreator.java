package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.elements.Way;

public class SingleHullCreator {

    private final IPointAccess points;
    private Area area;

    public SingleHullCreator(final IPointAccess points) {
        this.points = points;
    }

    public Area getHull() {
        return area;
    }

    public void createHull(final List<Way> ways, final float lineWidth) {
        area = new Area();

        for (final Way way : ways) {
            Path2D path = new Path2D.Float();
            int current = way.getNode(0);
            path.moveTo(points.getX(current), points.getY(current));
            for (int i = 1; i < way.size(); ++i) {
                current = way.getNode(i);
                path.lineTo(points.getX(current), points.getY(current));
            }
            Shape shape = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                    .createStrokedShape(path);
            area.add(new Area(shape));
        }
    }
}
