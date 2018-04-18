package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.Util;
import adminTool.elements.MultiElement;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import util.IntList;

import static adminTool.Util.lineIntersectsLine;

public class Transformation {
    private final int wayWidth;
    private final int threshold;
    private final CutPerformer cutPerformer;
    private UnboundedPointAccess points;
    private List<MultiElement> processedPaths;

    public Transformation(final int wayWidth, final int threshold) {
        this.wayWidth = wayWidth;
        this.threshold = threshold;
        this.cutPerformer = new CutPerformer();
    }

    public List<MultiElement> getProcessedPaths() {
        return processedPaths;
    }

    public void transform(final List<? extends MultiElement> paths, final UnboundedPointAccess points) {
        this.points = points;
        this.processedPaths = new ArrayList<MultiElement>(paths.size());

        final HashMap<Integer, IntList> map = createOccuranceMap(paths);
        final List<List<Cut>> cuts = createCutList(paths, map);

        final Iterator<? extends MultiElement> pathIterator = paths.iterator();
        for (final Iterator<List<Cut>> cutIterator = cuts.iterator(); cutIterator.hasNext();) {
            final List<Cut> cut = cutIterator.next();
            appendPaths(map, pathIterator.next(), cut);
        }
    }

    private List<List<Cut>> createCutList(final List<? extends MultiElement> paths,
            final HashMap<Integer, IntList> map) {
        final List<List<Cut>> cutList = new ArrayList<List<Cut>>(paths.size());

        final float[] coords = new float[2];
        final Point lastU = new Point();
        final Point currentU = new Point();
        final Point lastV = new Point();
        final Point currentV = new Point();
        for (int i = 0; i < paths.size(); ++i) {
            cutList.add(new ArrayList<Cut>(2));
        }

        for (final HashMap.Entry<Integer, IntList> entry : map.entrySet()) {
            final int node = entry.getKey();
            final IntList list = entry.getValue();

            if (list.size() > 1) {
                final MultiElement[] elements = createPaths(paths, entry);
                final Shape[] shapes = createShapes(points, elements, list);
                final Cut[] cuts = createCuts(elements);

                for (int u = 0; u < shapes.length - 1; ++u) {
                    final PathIterator uIt = shapes[u].getPathIterator(null);
                    applySegment(lastU, uIt, coords);
                    while (!uIt.isDone()) {
                        applySegment(currentU, uIt, coords);
                        for (int v = u + 1; v < shapes.length; ++v) {
                            final PathIterator vIt = shapes[v].getPathIterator(null);
                            applySegment(lastV, vIt, coords);
                            while (!vIt.isDone()) {
                                applySegment(currentV, vIt, coords);

                                final Point2D intersection = lineIntersectsLine(lastU, currentU, lastV, currentV);
                                if (intersection != null) {
                                    updateCut(cuts[u], elements[u], intersection);
                                    updateCut(cuts[v], elements[v], intersection);
                                }
                                lastV.setLocation(currentV);
                            }
                        }
                        lastU.setLocation(currentU);
                    }
                }

                for (int i = 0; i < cuts.length; ++i) {
                    final int index = list.get(i);
                    final MultiElement path = paths.get(index);
                    appendCut(path.size(), cutList.get(index), path.getNode(0) != node, cuts[i]);
                }
            }
        }

        return cutList;
    }

    private void appendCut(final int pathsize, final List<Cut> cutList, final boolean reverse, final Cut cut) {
        final Cut c = reverse ? new Cut(cut.getPoint(), pathsize - 1 - cut.getSegment(), 1 - cut.getOffset()) : cut;
        cutList.add(c);
    }

    private HashMap<Integer, IntList> createOccuranceMap(final List<? extends MultiElement> paths) {
        HashMap<Integer, IntList> map = new HashMap<>();
        for (int i = 0; i < paths.size(); ++i) {
            final MultiElement element = paths.get(i);
            append(map, element.getNode(0), i);
            append(map, element.getNode(element.size() - 1), i);
        }
        return map;
    }

    private Cut[] createCuts(final MultiElement[] elements) {
        final Cut[] cuts = new Cut[elements.length];
        for (int i = 0; i < cuts.length; ++i) {
            cuts[i] = new Cut(points.getPoints(), 0, 0);
            points.addPoint(0, 0);
        }
        return cuts;
    }

    private MultiElement[] createPaths(final List<? extends MultiElement> paths,
            final HashMap.Entry<Integer, IntList> entry) {
        final MultiElement[] elements = new MultiElement[entry.getValue().size()];
        for (int i = 0; i < elements.length; ++i) {
            final MultiElement element = paths.get(i);
            elements[i] = reduce(element.getNode(0) != entry.getKey() ? element.reverse() : element);
        }
        return elements;
    }

    private void updateCut(final Cut cut, final MultiElement path, final Point2D intersect) {
        final Point last = new Point();
        final Point current = new Point();

        last.setLocation(points.getX(path.getNode(0)), points.getY(path.getNode(0)));
        for (int segment = 0; segment < path.size() - 1; ++segment) {
            current.setLocation(points.getX(path.getNode(segment + 1)), points.getY(path.getNode(segment + 1)));
            final long dx = current.x - last.x;
            final long dy = current.y - last.y;
            final long square = (dx * dx + dy * dy);
            final double s = ((intersect.getX() - last.x) * dx + (intersect.getY() - last.y) * dy) / (double) square;
            if (s > -Util.EPSILON && s < 1 + Util.EPSILON) {
                // TODO epsilon comparison?
                if (segment > cut.getSegment() || (segment == cut.getSegment() && s > cut.getOffset())) {
                    cut.setSegment(segment);
                    cut.setOffset(s);
                    points.setPoint(cut.getPoint(), (int) (last.x + s * dx), (int) (last.y + s * dy));

                }
            }
            last.setLocation(current);
        }
    }

    private void applySegment(final Point point, final PathIterator iterator, final float[] coords) {
        iterator.currentSegment(coords);
        iterator.next();
        point.setLocation((int) coords[0], (int) coords[1]);
    }

    private Shape[] createShapes(final IPointAccess points, final MultiElement[] paths, final IntList list) {
        final Shape[] shapes = new Shape[list.size()];
        for (int i = 0; i < shapes.length; ++i) {
            shapes[i] = Util.createStrokedShape(points, paths[i],
                    new BasicStroke(wayWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        }
        return shapes;
    }

    private void append(final HashMap<Integer, IntList> map, final int node, final int i) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(i);
    }

    private MultiElement reduce(final MultiElement element) {
        double totalLength = 0;

        int lastX = points.getX(element.getNode(0));
        int lastY = points.getY(element.getNode(0));
        for (int i = 1; i < element.size(); i++) {
            int currentX = points.getX(element.getNode(i));
            int currentY = points.getY(element.getNode(i));
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            if (totalLength >= threshold) {
                return element.subElement(0, i + 1);
            }
            lastX = currentX;
            lastY = currentY;
        }
        return element;
    }

    private void appendPaths(final HashMap<Integer, IntList> map, final MultiElement path, final List<Cut> cuts) {
        final List<MultiElement> paths = cutPerformer.performCuts(path, cuts);
        final Iterator<MultiElement> iterator = paths.iterator();
        if (map.get(path.getNode(0)).size() > 1) {
            final MultiElement element = iterator.next();
            element.setType(-1);
            processedPaths.add(element);
        }
        processedPaths.add(iterator.next());
        if (map.get(path.getNode(path.size() - 1)).size() > 1) {
            final MultiElement element = iterator.next();
            element.setType(-1);
            processedPaths.add(element);
        }
    }
}
