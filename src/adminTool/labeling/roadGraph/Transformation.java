package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.MultiElement;
import adminTool.elements.PointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.util.IntersectionUtil;
import util.IntList;

public class Transformation {
    private final IDrawInfo info;
    private final double junctionThreshold;
    private final double displacement;
    private PointAccess points;
    private List<Road> processedPaths;

    public Transformation(final IDrawInfo info, final double junctionThreshold, final double displacement) {
        this.info = info;
        this.junctionThreshold = junctionThreshold;
        this.displacement = displacement;
    }

    public List<Road> getRoads() {
        return processedPaths;
    }

    public void transform(final List<Road> paths, final PointAccess points) {
        this.points = points;
        performCuts(paths);
    }

    private void performCuts(final List<Road> paths) {
        final RoadCutPerformer[] cutPerformers = createInitialCutPerformers(paths);
        final HashMap<Integer, List<IndexedWay>> map = createOccuranceMap(paths);

        final float[] isect = new float[2];

        for (final List<IndexedWay> ways : map.values()) {
            if (ways.size() > 1) {
                final Area[] areas = createShapes(ways);
                initCuts(ways, cutPerformers);

                for (int u = 0; u < areas.length - 1; ++u) {
                    final IndexedWay uWay = ways.get(u);
                    final Cut uCut = cutPerformers[uWay.pathIndex].cuts.get(uWay.cutIndex);
                    for (int v = u + 1; v < areas.length; ++v) {
                        final IndexedWay vWay = ways.get(v);
                        final Cut vCut = cutPerformers[vWay.pathIndex].cuts.get(vWay.cutIndex);
                        final Area intersect = new Area(areas[u]);
                        intersect.intersect(areas[v]);
                        if (!intersect.isEmpty()) {
                            for (PathIterator it = intersect.getPathIterator(null); !it.isDone(); it.next()) {
                                it.currentSegment(isect);
                                update(isect, uCut, uWay.element);
                                update(isect, vCut, vWay.element);
                            }
                        }
                    }
                }
                for (final IndexedWay way : ways) {
                    final Cut cut = cutPerformers[way.pathIndex].cuts.get(way.cutIndex);
                    clampCut(cut, way.element);
                    displaceCut(cut, way.element);
                }
            }
        }

        processedPaths = new ArrayList<>(paths.size());
        for (final RoadCutPerformer cut : cutPerformers)
            cut.performCuts(processedPaths);
    }

    private HashMap<Integer, List<IndexedWay>> createOccuranceMap(final List<? extends MultiElement> paths) {
        HashMap<Integer, List<IndexedWay>> map = new HashMap<>();
        for (int i = 0; i < paths.size(); ++i) {
            final MultiElement element = paths.get(i);
            appendOccurance(map, element.getNode(0), element, i, 0);
            appendOccurance(map, element.getNode(element.size() - 1), element.reverse(), i, 1);
        }
        return map;
    }

    private void appendOccurance(final HashMap<Integer, List<IndexedWay>> map, final int node,
            final MultiElement element, final int pathIndex, final int cutIndex) {
        List<IndexedWay> list = map.get(node);
        if (list == null) {
            list = new ArrayList<IndexedWay>();
            map.put(node, list);
        }
        list.add(new IndexedWay(element, pathIndex, cutIndex));
    }

    private void clampCut(final Cut cut, final MultiElement element) {
        final Point2D last = new Point2D.Double(points.getX(element.getNode(0)), points.getY(element.getNode(0)));
        final Point2D current = new Point2D.Double();

        double remainDistance = junctionThreshold;
        for (int segment = 0; segment <= cut.getSegment(); ++segment) {
            current.setLocation(points.getX(element.getNode(segment + 1)), points.getY(element.getNode(segment + 1)));

            final double distance = last.distance(current);
            if (remainDistance < distance) {
                final double offset = remainDistance / distance;
                if (segment != cut.getSegment() || offset < cut.getOffset()) {
                    cut.setOffset(offset);
                    cut.setSegment(segment);
                    points.set(cut.getPoint(), last.getX() + offset * (current.getX() - last.getX()),
                            last.getY() + offset * (current.getY() - last.getY()));
                }
                return;
            }
            remainDistance -= distance;
            last.setLocation(current);
        }
    }

    private void displaceCut(final Cut cut, final MultiElement element) {
        int node = cut.getSegment();
        final Point2D last = new Point2D.Double(points.getX(element.getNode(node)), points.getY(element.getNode(node)));
        final Point2D current = new Point2D.Double();

        double remainDistance = last.distance(points.getX(cut.getPoint()), points.getY(cut.getPoint())) + displacement;

        do {
            current.setLocation(points.getX(element.getNode(node + 1)), points.getY(element.getNode(node + 1)));
            final double distance = last.distance(current);

            if (remainDistance < distance) {
                final double offset = remainDistance / distance;
                cut.setOffset(offset);
                cut.setSegment(node);
                points.set(cut.getPoint(), last.getX() + offset * (current.getX() - last.getX()),
                        last.getY() + offset * (current.getY() - last.getY()));

                return;
            }
            remainDistance -= distance;
            last.setLocation(current);
        } while (++node < element.size() - 1);

        cut.setOffset(1);
        cut.setSegment(element.size() - 2);
        points.set(cut.getPoint(), last.getX(), last.getY());
    }

    private RoadCutPerformer[] createInitialCutPerformers(final List<Road> paths) {
        final RoadCutPerformer[] cutList = new RoadCutPerformer[paths.size()];
        for (int i = 0; i < paths.size(); ++i) {
            cutList[i] = new RoadCutPerformer(paths.get(i));
        }
        return cutList;
    }

    private Area[] createShapes(final List<IndexedWay> elements) {
        final Area[] shapes = new Area[elements.size()];
        for (int i = 0; i < shapes.length; ++i) {
            final MultiElement element = elements.get(i).element;
            shapes[i] = new Area(createBoundedStrokedShape(element, info.getStrokeWidth(element.getType())));
        }
        return shapes;
    }

    public Shape createBoundedStrokedShape(final MultiElement element, final double width) {
        final Stroke stroke = new BasicStroke((float) width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
        final Path2D path = new Path2D.Double();

        final Point2D last = new Point2D.Double(points.getX(element.getNode(0)), points.getY(element.getNode(0)));
        final Point2D current = new Point2D.Double();
        double totalLength = 0;
        path.moveTo(last.getX(), last.getY());

        for (int i = 1; i < element.size(); i++) {
            current.setLocation(points.getX(element.getNode(i)), points.getY(element.getNode(i)));
            final double distance = last.distance(current);

            if (totalLength + distance >= junctionThreshold) {
                final double s = (junctionThreshold - totalLength) / distance;
                path.lineTo(last.getX() + s * (current.getX() - last.getX()),
                        last.getY() + s * (current.getY() - last.getY()));
                return stroke.createStrokedShape(path);
            }

            totalLength += distance;
            path.lineTo(current.getX(), current.getY());
            last.setLocation(current);
        }

        return stroke.createStrokedShape(path);
    }

    private void initCuts(final List<IndexedWay> elements, final RoadCutPerformer[] cutPerformers) {
        for (final IndexedWay way : elements) {
            cutPerformers[way.pathIndex].cuts.get(way.cutIndex).setPoint(points.size());
            points.addPoint(points.getX(way.element.getNode(0)), points.getY(way.element.getNode(0)));
        }
    }

    private void update(final float[] isect, final Cut cut, final MultiElement path) {
        final Point2D last = new Point2D.Double(points.getX(path.getNode(cut.getSegment())),
                points.getY(path.getNode(cut.getSegment())));
        final Point2D current = new Point2D.Double();
        final double wayWidth = info.getStrokeWidth(path.getType()) / 2;

        for (int segment = cut.getSegment(); segment < path.size() - 1; ++segment) {
            current.setLocation(points.getX(path.getNode(segment + 1)), points.getY(path.getNode(segment + 1)));
            final double dx = current.getX() - last.getX();
            final double dy = current.getY() - last.getY();
            double s = ((isect[0] - last.getX()) * dx + (isect[1] - last.getY()) * dy) / (dx * dx + dy * dy);
            final double plumbX = last.getX() + s * dx;
            final double plumbY = last.getY() + s * dy;

            if (IntersectionUtil.inIntervall(s, 0, 1) && cut.compareTo(segment, s) < 0
                    && isInWayShape(isect, plumbX, plumbY, wayWidth)) {
                s = Math.min(Math.max(s, 0), 1);
                cut.setSegment(segment);
                cut.setOffset(s);
                points.set(cut.getPoint(), last.getX() + s * dx, last.getY() + s * dy);
            }

            last.setLocation(current);
        }

    }

    private boolean isInWayShape(final float[] isect, final double plumbX, final double plumbY, final double wayWidth) {
        return wayWidth - Point2D.distance(plumbX, plumbY, isect[0], isect[1]) > -IntersectionUtil.EPSILON;
    }

    private static class RoadCutPerformer {
        private static CutPerformer cutPerformer = new CutPerformer();
        private final List<Cut> cuts;
        private final Road road;

        public RoadCutPerformer(final Road road) {
            this.road = road;
            cuts = new ArrayList<>(2);
            cuts.add(new Cut(road.getNode(0), 0, 0));
            cuts.add(new Cut(road.getNode(road.size() - 1), 0, 0));
        }

        public void performCuts(final List<Road> roads) {
            final Cut endCut = cuts.get(1);
            flipCut(endCut);

            double dif = cuts.get(0).getSegment() + cuts.get(0).getOffset() - endCut.getSegment() - endCut.getOffset();
            if (dif >= -IntersectionUtil.EPSILON) {
                road.setRoadType(RoadType.Junction);
                roads.add(road);
                return;
            }

            final List<IntList> paths = cutPerformer.performSortedCuts(road, cuts);
            roads.add(new Road(paths.get(1), road.getType(), road.getRoadId(), RoadType.Road)); // road section
            for (int i = 0; i < 2; ++i) {
                final IntList path = paths.get(2 * i);
                if (path.get(0) != path.get(path.size() - 1))
                    roads.add(new Road(path, road.getType(), road.getRoadId(), RoadType.Junction));// junction edge
            }
        }

        private void flipCut(final Cut cut) {
            cut.setSegment(road.size() - 2 - cut.getSegment());
            cut.setOffset(1 - cut.getOffset());
        }
    }

    private static class IndexedWay {
        private final MultiElement element;
        private final int pathIndex;
        private final int cutIndex;

        public IndexedWay(final MultiElement element, final int pathIndex, final int cutIndex) {
            this.element = element;
            this.pathIndex = pathIndex;
            this.cutIndex = cutIndex;
        }
    }
}
