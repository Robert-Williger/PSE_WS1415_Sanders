package adminTool.labeling.roadGraph.simplification.hull;

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

import static adminTool.Util.createStrokedShape;

public class HullCreator {
    private final IPointAccess points;

    private List<Area> hulls;

    public HullCreator(final IPointAccess points) {
        this.points = points;
    }

    public void createHulls(final List<Way> ways, final float lineWidth) {
        final BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

        final Area[] areas = createAreas(createShapes(ways, stroke));
        final IntList[] graph = createIntersectionGraph(areas);

        hulls = createHulls(areas, graph);
    }

    public List<Area> getHulls() {
        return hulls;
    }

    private ArrayList<Area> createHulls(final Area[] areas, final IntList[] graph) {
        ArrayList<Area> hulls = new ArrayList<Area>();
        for (int u = 0; u < graph.length; ++u) {
            if (areas[u] != null) {
                hulls.add(areas[u]);
                appendArea(areas[u], u, areas, graph);
            }
        }
        return hulls;
    }

    private IntList[] createIntersectionGraph(final Area[] areas) {
        IntList[] intersectionGraph = new IntList[areas.length];
        for (int u = 0; u < areas.length; ++u) {
            intersectionGraph[u] = new IntList();
        }
        for (int u = 0; u < areas.length; ++u) {
            for (int v = u + 1; v < areas.length; ++v) {
                if (areas[u].intersects(areas[v].getBounds2D())) {
                    final Area uArea = new Area(areas[u]);
                    uArea.intersect(areas[v]);
                    if (!uArea.isEmpty()) {
                        intersectionGraph[u].add(v);
                        intersectionGraph[v].add(u);
                    }
                }
            }
        }

        return intersectionGraph;
    }

    private Shape[] createShapes(final List<Way> ways, final BasicStroke stroke) {
        Shape[] shapes = new Shape[ways.size()];

        int u = 0;
        for (final Way way : ways) {
            Path2D path = new Path2D.Float();
            int current = way.getNode(0);
            path.moveTo(points.getX(current), points.getY(current));
            for (int i = 1; i < way.size(); ++i) {
                current = way.getNode(i);
                path.lineTo(points.getX(current), points.getY(current));
            }

            shapes[u] = createStrokedShape(points, way, stroke);
            ++u;
        }
        return shapes;
    }

    private Area[] createAreas(final Shape[] shapes) {
        Area[] areas = new Area[shapes.length];
        for (int i = 0; i < shapes.length; ++i) {
            areas[i] = new Area(shapes[i]);
        }
        return areas;
    }

    private void appendArea(final Area area, final int node, final Area[] areas, final IntList[] graph) {
        areas[node] = null;
        for (final PrimitiveIterator.OfInt it = graph[node].iterator(); it.hasNext();) {
            final int next = it.nextInt();
            if (areas[next] != null) {
                area.add(areas[next]);
                appendArea(area, next, areas, graph);
            }
        }
    }
}
