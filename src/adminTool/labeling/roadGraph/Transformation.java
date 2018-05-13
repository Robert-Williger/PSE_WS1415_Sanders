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

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.util.IntersectionUtil;
import util.IntList;

public class Transformation {
    private final int wayWidthSq;
    private final int threshold;
    private final CutPerformer cutPerformer;
    private final Stroke stroke;
    private UnboundedPointAccess points;
    private List<MultiElement> processedPaths;
    private final float[] intersection;

    public Transformation(final int wayWidth, final int junctionThreshold) {
        this.wayWidthSq = wayWidth * wayWidth;
        this.threshold = junctionThreshold;
        this.cutPerformer = new CutPerformer();
        this.stroke = new BasicStroke(wayWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        this.intersection = new float[2];
    }

    public List<MultiElement> getProcessedPaths() {
        return processedPaths;
    }

    public void transform(final List<? extends MultiElement> paths, final UnboundedPointAccess points) {
        this.points = points;
        this.processedPaths = new ArrayList<MultiElement>(paths.size());

        final HashMap<Integer, IntList> map = createOccuranceMap(paths);
        final CutInfo[] cuts = createCuts(paths, map);

        final Iterator<? extends MultiElement> pathIterator = paths.iterator();
        for (final CutInfo cut : cuts) {
            appendPaths(map, pathIterator.next(), cut);
        }
    }

    private HashMap<Integer, IntList> createOccuranceMap(final List<? extends MultiElement> paths) {
        HashMap<Integer, IntList> map = new HashMap<>();
        for (int i = 0; i < paths.size(); ++i) {
            final MultiElement element = paths.get(i);
            appendOccurance(map, element.getNode(0), i);
            appendOccurance(map, element.getNode(element.size() - 1), i);
        }
        return map;
    }

    private void appendOccurance(final HashMap<Integer, IntList> map, final int node, final int i) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(i);
    }

    private CutInfo[] createCuts(final List<? extends MultiElement> paths, final HashMap<Integer, IntList> map) {
        final CutInfo[] cutArray = createInitialCuts(paths);

        for (final HashMap.Entry<Integer, IntList> entry : map.entrySet()) {
            final int node = entry.getKey();
            final IntList list = entry.getValue();

            if (list.size() > 1) {
                final MultiElement[] elements = createDirectedPaths(paths, entry);
                final Shape[] shapes = createShapes(elements);
                final Cut[] cuts = createCuts(elements);

                for (int u = 0; u < shapes.length - 1; ++u) {
                    for (int v = u + 1; v < shapes.length; ++v) {
                        final Area intersect = new Area(shapes[u]);
                        intersect.intersect(new Area(shapes[v]));
                        if (!intersect.isEmpty()) {
                            for (final PathIterator iterator = intersect.getPathIterator(null); !iterator.isDone();) {
                                iterator.currentSegment(intersection);
                                iterator.next();
                                updateCut(cuts[u], elements[u]);
                                updateCut(cuts[v], elements[v]);
                            }
                        }
                    }
                }

                updatePoints(elements, cuts);

                for (int i = 0; i < cuts.length; ++i) {
                    final int index = list.get(i);
                    final MultiElement path = paths.get(index);
                    appendCut(cuts[i], cutArray[index], path.size(), path.getNode(0) != node);
                }
            }

        }

        return cutArray;

    }

    private CutInfo[] createInitialCuts(final List<? extends MultiElement> paths) {
        final CutInfo[] cutList = new CutInfo[paths.size()];
        for (int i = 0; i < paths.size(); ++i) {
            cutList[i] = new CutInfo();
        }
        return cutList;
    }

    private MultiElement[] createDirectedPaths(final List<? extends MultiElement> paths,
            final HashMap.Entry<Integer, IntList> entry) {
        final int node = entry.getKey();
        final IntList list = entry.getValue();
        final MultiElement[] elements = new MultiElement[list.size()];
        for (int i = 0; i < elements.length; ++i) {
            final MultiElement element = paths.get(list.get(i));
            elements[i] = element.getNode(0) != node ? element.reverse() : element;
        }
        return elements;
    }

    private Shape[] createShapes(final MultiElement[] elements) {
        final Shape[] shapes = new Shape[elements.length];
        for (int i = 0; i < shapes.length; ++i) {
            shapes[i] = createBoundedStrokedShape(elements[i]);
        }
        return shapes;
    }

    public Shape createBoundedStrokedShape(final MultiElement element) {
        Path2D path = new Path2D.Float();
        double totalLength = 0;

        int lastX = points.getX(element.getNode(0));
        int lastY = points.getY(element.getNode(0));
        path.moveTo(lastX, lastY);

        for (int i = 1; i < element.size(); i++) {
            int currentX = points.getX(element.getNode(i));
            int currentY = points.getY(element.getNode(i));
            final double distance = Point.distance(currentX, currentY, lastX, lastY);

            if (totalLength + distance >= threshold) {
                final double s = (threshold - totalLength) / distance;
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

    private Cut[] createCuts(final MultiElement[] elements) {
        final Cut[] cuts = new Cut[elements.length];
        for (int i = 0; i < cuts.length; ++i) {
            cuts[i] = new Cut(points.getPoints(), 0, 0);
            points.addPoint(0, 0);
        }
        return cuts;
    }

    private void updateCut(final Cut cut, final MultiElement path) {
        final Point last = new Point();
        final Point current = new Point();

        last.setLocation(points.getX(path.getNode(0)), points.getY(path.getNode(0)));
        for (int node = 0; node < path.size() - 1; ++node) {
            current.setLocation(points.getX(path.getNode(node + 1)), points.getY(path.getNode(node + 1)));
            final long dx = current.x - last.x;
            final long dy = current.y - last.y;
            final long square = (dx * dx + dy * dy);
            final double s = ((intersection[0] - last.x) * dx + (intersection[1] - last.y) * dy) / (double) square;
            if (IntersectionUtil.inIntervall(s, 0, 1) && cut.compareTo(node, s) < 0
                    && isNearEnough(last.x + s * dx, last.y + s * dy)) {
                cut.setSegment(node);
                cut.setOffset(s);
            }
            last.setLocation(current);
        }

    }

    private boolean isNearEnough(final double plumbX, final double plumbY) {
        return wayWidthSq
                - Point.distanceSq(plumbX, plumbY, intersection[0], intersection[1]) > -IntersectionUtil.EPSILON;
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

    private void appendPaths(final HashMap<Integer, IntList> map, final MultiElement path, final CutInfo cutInfo) {
        final List<MultiElement> paths = cutPerformer.performCuts(path, cutInfo.getCuts());
        final Iterator<MultiElement> iterator = paths.iterator();

        if (cutInfo.hasFirstJunction()) {
            final MultiElement element = iterator.next();
            element.setType(-1);
            processedPaths.add(element);
        }
        if (cutInfo.hasRoadSection()) {
            processedPaths.add(iterator.next());
        }
        if (cutInfo.hasSecondJunction()) {
            final MultiElement element = iterator.next();
            element.setType(-1);
            processedPaths.add(element);
        }
    }

    private void updatePoints(final MultiElement[] elements, final Cut[] cuts) {
        for (int i = 0; i < cuts.length; ++i) {
            final Cut cut = cuts[i];
            final MultiElement element = elements[i];
            final int segment = cut.getSegment();
            final double offset = cut.getOffset();
            final int x1 = points.getX(element.getNode(segment));
            final int y1 = points.getY(element.getNode(segment));
            final int x2 = points.getX(element.getNode(segment + 1));
            final int y2 = points.getY(element.getNode(segment + 1));
            points.setPoint(cut.getPoint(), (int) (x1 + offset * (x2 - x1)), (int) (y1 + offset * (y2 - y1)));
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
}
