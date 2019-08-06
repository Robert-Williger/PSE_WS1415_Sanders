package adminTool.labeling.roadGraph.simplification.hull;

import static adminTool.util.ShapeUtil.createStrokedShape;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.elements.IPointAccess;
import adminTool.elements.Way;
import adminTool.labeling.DrawInfo;
import util.IntList;

public class HullCreator {

    private final DrawInfo info;
    private List<Area> hulls;

    public HullCreator(final DrawInfo info) {
        this.info = info;
    }

    public void createHulls(final List<Way> ways, final IPointAccess points) {
        if (ways.isEmpty()) {
            hulls = new ArrayList<Area>(0);
            return;
        }

        final float strokeWidth = info.getStrokeWidth(ways.get(0).getType());
        final BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

        final Area[] areas = createAreas(createShapes(ways, points, stroke));
        final IntList[] graph = createIntersectionGraph(ways, areas);

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

    private IntList[] createIntersectionGraph(final List<Way> ways, final Area[] areas) {
        IntList[] intersectionGraph = new IntList[areas.length];
        for (int u = 0; u < areas.length; ++u) {
            intersectionGraph[u] = new IntList();
        }
        for (int u = 0; u < areas.length; ++u) {
            final Way uWay = ways.get(u);
            for (int v = u + 1; v < areas.length; ++v) {
                final Way vWay = ways.get(v);
                if (uWay.getNode(0) == vWay.getNode(0) || uWay.getNode(uWay.size() - 1) == vWay.getNode(0)
                        || uWay.getNode(0) == vWay.getNode(vWay.size() - 1)
                        || uWay.getNode(uWay.size() - 1) == vWay.getNode(vWay.size() - 1)) {
                    intersectionGraph[u].add(v);
                    intersectionGraph[v].add(u);
                } else if (areas[u].intersects(areas[v].getBounds2D())) {
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

    private Shape[] createShapes(final List<Way> ways, final IPointAccess points, final BasicStroke stroke) {
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
