package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import adminTool.elements.MultiElement;
import adminTool.elements.PointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.util.IntersectionUtil;
import util.IntList;

public class Transformation {
    private final IDrawInfo info;
    private final CutPerformer cutPerformer;
    private final int junctionThreshold;
    private PointAccess.OfDouble points;
    private List<Road> processedPaths;

    public Transformation(final IDrawInfo info, final int junctionThreshold) {
        this.info = info;
        this.junctionThreshold = junctionThreshold;
        this.cutPerformer = new CutPerformer();
    }

    public List<Road> getProcessedRoads() {
        return processedPaths;
    }

    public void transform(final List<Road> paths, final PointAccess.OfDouble points) {
        this.points = points;
        this.processedPaths = new ArrayList<>(paths.size());

        final CutInfo[] cuts = createCutInfos(paths);

        final Iterator<Road> pathIterator = paths.iterator();
        for (final CutInfo cut : cuts) {
            appendPaths(pathIterator.next(), cut);
        }
    }

    private HashMap<Integer, List<IndexedWay>> createOccuranceMap(final List<? extends MultiElement> paths) {
        HashMap<Integer, List<IndexedWay>> map = new HashMap<>();
        for (int i = 0; i < paths.size(); ++i) {
            final MultiElement element = paths.get(i);
            appendOccurance(map, element.getNode(0), element, i);
            appendOccurance(map, element.getNode(element.size() - 1), element.reverse(), i);
        }
        return map;
    }

    private void appendOccurance(final HashMap<Integer, List<IndexedWay>> map, final int node,
            final MultiElement element, final int index) {
        List<IndexedWay> list = map.get(node);
        if (list == null) {
            list = new ArrayList<IndexedWay>();
            map.put(node, list);
        }
        list.add(new IndexedWay(element, index));
    }

    private CutInfo[] createCutInfos(final List<? extends MultiElement> paths) {
        final CutInfo[] cutArray = createIntialCutInfos(paths);
        final HashMap<Integer, List<IndexedWay>> map = createOccuranceMap(paths);
        final float[] isect = new float[2];

        for (final List<IndexedWay> list : map.values()) {
            if (list.size() > 1) {
                final Shape[] shapes = createShapes(list);
                final Cut[] cuts = createInitialCuts(list);

                for (int u = 0; u < shapes.length - 1; ++u) {
                    for (int v = u + 1; v < shapes.length; ++v) {
                        final Area intersect = new Area(shapes[u]);
                        intersect.intersect(new Area(shapes[v]));
                        if (!intersect.isEmpty()) {
                            for (final PathIterator iterator = intersect.getPathIterator(null); !iterator.isDone();) {
                                iterator.currentSegment(isect);
                                iterator.next();
                                update(isect, cuts[u], list.get(u).element);
                                update(isect, cuts[v], list.get(v).element);
                            }
                        }
                    }
                }

                for (int i = 0; i < cuts.length; ++i) {
                    final int index = list.get(i).index;
                    final MultiElement path = paths.get(index);
                    appendCut(cuts[i], cutArray[index], path.size(), path != list.get(i).element);
                }
            }

        }

        return cutArray;

    }

    private CutInfo[] createIntialCutInfos(final List<? extends MultiElement> paths) {
        final CutInfo[] cutList = new CutInfo[paths.size()];
        for (int i = 0; i < paths.size(); ++i) {
            cutList[i] = new CutInfo();
        }
        return cutList;
    }

    private Shape[] createShapes(final List<IndexedWay> elements) {
        final Shape[] shapes = new Shape[elements.size()];
        for (int i = 0; i < shapes.length; ++i) {
            final MultiElement element = elements.get(i).element;
            shapes[i] = createBoundedStrokedShape(element, info.getStrokeWidth(element.getType()));
        }
        return shapes;
    }

    public Shape createBoundedStrokedShape(final MultiElement element, final float width) {
        final Stroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        Path2D path = new Path2D.Float();
        double totalLength = 0;

        double lastX = points.getX(element.getNode(0));
        double lastY = points.getY(element.getNode(0));
        path.moveTo(lastX, lastY);

        for (int i = 1; i < element.size(); i++) {
            double currentX = points.getX(element.getNode(i));
            double currentY = points.getY(element.getNode(i));
            final double distance = Point.distance(currentX, currentY, lastX, lastY);

            if (totalLength + distance >= junctionThreshold) {
                final double s = (junctionThreshold - totalLength) / distance;
                path.lineTo(lastX + s * (currentX - lastX), lastY + s * (currentY - lastY));
                return stroke.createStrokedShape(path);
            }

            totalLength += distance;
            path.lineTo(currentX, currentY);
            lastX = currentX;
            lastY = currentY;
        }

        return stroke.createStrokedShape(path);
    }

    private Cut[] createInitialCuts(final List<IndexedWay> elements) {
        final Cut[] cuts = new Cut[elements.size()];
        for (int i = 0; i < cuts.length; ++i) {
            cuts[i] = new Cut(points.size(), 0, 0);
            final MultiElement element = elements.get(i).element;
            points.addPoint(points.getX(element.getNode(0)), points.getY(element.getNode(0)));
        }
        return cuts;
    }

    private void update(final float[] isect, final Cut cut, final MultiElement path) {
        final Point last = new Point();
        final Point current = new Point();
        final double wayWidthSq = info.getStrokeWidth(path.getType()) * info.getStrokeWidth(path.getType());
        boolean found = false;

        last.setLocation(points.getX(path.getNode(0)), points.getY(path.getNode(0)));
        for (int node = 0; node < path.size() - 1; ++node) {
            current.setLocation(points.getX(path.getNode(node + 1)), points.getY(path.getNode(node + 1)));
            final int dx = current.x - last.x;
            final int dy = current.y - last.y;
            final double s = (((double) isect[0] - last.x) * dx + (isect[1] - last.y) * dy) / (dx * dx + dy * dy);
            final double plumbX = last.x + s * dx;
            final double plumbY = last.y + s * dy;

            if (isInWayShape(isect, plumbX, plumbY, wayWidthSq)) {
                found = true;
                if (IntersectionUtil.inIntervall(s, 0, 1) && cut.compareTo(node, s) < 0) {
                    cut.setSegment(node);
                    cut.setOffset(s);
                    points.setPoint(cut.getPoint(), last.x + s * dx, last.y + s * dy);
                }
            } else if (found) {
                break;
            }
            last.setLocation(current);
        }

    }

    private boolean isInWayShape(final float[] isect, final double plumbX, final double plumbY,
            final double wayWidthSq) {
        return wayWidthSq - Point.distanceSq(plumbX, plumbY, isect[0], isect[1]) > -IntersectionUtil.EPSILON;
    }

    private void appendCut(final Cut cut, final CutInfo info, final int pathsize, final boolean reverse) {
        final Cut my = reverse ? new Cut(cut.getPoint(), pathsize - 2 - cut.getSegment(), 1 - cut.getOffset()) : cut;
        final List<Cut> cutList = info.getCuts();

        if (cutList.isEmpty() || (reverse != my.compareTo(cutList.get(0)) < 0)) {
            cutList.add(my);
            if (reverse)
                info.setSecondJunction();
            else {
                info.setFirstJunction();
            }
        } else {
            cutList.clear();
            info.reset();
            info.setFirstJunction();
        }
    }

    private void appendPaths(final Road path, final CutInfo cutInfo) {
        final List<IntList> paths = cutPerformer.performCuts(path, cutInfo.getCuts());
        final Iterator<IntList> iterator = paths.iterator();

        if (cutInfo.hasFirstJunction()) {
            processedPaths.add(new Road(iterator.next(), path.getType(), path.getName(), -1));
        }
        if (cutInfo.hasRoadSection()) {
            processedPaths.add(new Road(iterator.next(), path.getType(), path.getName(), path.getRoadId()));
        }
        if (cutInfo.hasSecondJunction()) {
            processedPaths.add(new Road(iterator.next(), path.getType(), path.getName(), -1));
        }
    }

    private static class CutInfo {
        private int info;
        private final List<Cut> cuts;

        public CutInfo() {
            setRoadSection();
            cuts = new ArrayList<>(2);
        }

        public void setFirstJunction() {
            info |= 0b001;
        }

        public void setRoadSection() {
            info |= 0b010;
        }

        public void setSecondJunction() {
            info |= 0b100;
        }

        public void reset() {
            info = 0;
        }

        public boolean hasFirstJunction() {
            return (info & 0b001) != 0;
        }

        public boolean hasRoadSection() {
            return (info & 0b010) != 0;
        }

        public boolean hasSecondJunction() {
            return (info & 0b100) != 0;
        }

        public List<Cut> getCuts() {
            return cuts;
        }
    }

    private static class IndexedWay {
        private final MultiElement element;
        private final int index;

        public IndexedWay(final MultiElement element, final int index) {
            this.element = element;
            this.index = index;
        }
    }
}
