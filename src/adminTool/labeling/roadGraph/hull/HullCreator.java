package adminTool.labeling.roadGraph.hull;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.IPointAccess;
import adminTool.elements.Way;
import util.IntList;

public class HullCreator {
    private final IPointAccess points;

    private Shape[] shapes;
    private Area[] areas;
    private IntList[] intersectionGraph;
    private boolean[] marked;
    private List<Area> hulls;

    public HullCreator(final IPointAccess points) {
        this.points = points;
    }

    public void createHulls(final List<Way> ways, final float lineWidth) {
        final BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        createShapes(ways, stroke);
        createAreas(shapes);
        createIntersectionGraph(areas);

        createHulls();
    }

    public List<Area> getHulls() {
        return hulls;
    }

    private void createHulls() {
        hulls = new ArrayList<Area>();
        for (int u = 0; u < intersectionGraph.length; ++u) {
            if (!marked[u]) {
                appendArea(areas[u], u);
                hulls.add(areas[u]);
            }
        }
    }

    private void createIntersectionGraph(final Area[] areas) {
        intersectionGraph = new IntList[areas.length];
        for (int u = 0; u < areas.length; ++u) {
            intersectionGraph[u] = new IntList();
        }
        for (int u = 0; u < areas.length; ++u) {
            for (int v = u + 1; v < areas.length; ++v) {
                if (areas[u].intersects(areas[v].getBounds2D())) {
                    Area uArea = new Area(areas[u]);
                    uArea.intersect(areas[v]);
                    if (!uArea.isEmpty()) {
                        intersectionGraph[u].add(v);
                        intersectionGraph[v].add(u);
                    }
                }
            }
            ++u;
        }

        marked = new boolean[areas.length];
    }

    private void createShapes(final List<Way> ways, final BasicStroke stroke) {
        shapes = new Shape[ways.size()];

        int u = 0;
        for (final Way way : ways) {
            Path2D path = new Path2D.Float();
            int current = way.getNode(0);
            path.moveTo(points.getX(current), points.getY(current));
            for (int i = 1; i < way.size(); ++i) {
                current = way.getNode(i);
                path.lineTo(points.getX(current), points.getY(current));
            }
            shapes[u] = stroke.createStrokedShape(path);
            ++u;
        }
    }

    private void createAreas(final Shape[] shapes) {
        areas = new Area[shapes.length];
        for (int i = 0; i < shapes.length; ++i) {
            areas[i] = new Area(shapes[i]);
        }
    }

    private void appendArea(final Area area, final int node) {
        marked[node] = true;
        for (final PrimitiveIterator.OfInt it = intersectionGraph[node].iterator(); it.hasNext();) {
            final int next = it.nextInt();
            if (!marked[next]) {
                area.add(areas[next]);
                appendArea(area, next);
            }
        }
    }
}
