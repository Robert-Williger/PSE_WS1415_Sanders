package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import adminTool.VisvalingamWhyatt;
import adminTool.elements.MultiElement;
import adminTool.elements.UnboundedPointAccess;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.WayQuadtreePolicy;
import adminTool.util.IntersectionUtil;
import adminTool.util.ShapeUtil;
import util.IntList;

public class Planarization {
    private static final int DEFAULT_MAX_ELEMENTS_PER_TILE = 4;
    private static final int DEFAULT_MAX_HEIGHT = 20;

    private final int maxElementsPerTile;
    private final int maxHeight;
    private final CutPerformer cutPerformer;
    private final VisvalingamWhyatt simplifier;

    private final int stubThreshold;
    private final int fuzzyThreshold;
    private final int tThreshold;

    private int[] nodeCounts;
    private List<Road> originalPaths;
    private List<Road> processedPaths;
    private UnboundedPointAccess points;

    private final double[] offsets;

    public Planarization() {
        this(0, 0, 0);
    }

    public Planarization(final int stubThreshold, final int tThreshold, final int fuzzyThreshold) {
        this(stubThreshold, tThreshold, fuzzyThreshold, DEFAULT_MAX_ELEMENTS_PER_TILE, DEFAULT_MAX_HEIGHT);
    }

    public Planarization(final int stubThreshold, final int tThreshold, final int fuzzyThreshold,
            final int maxElementsPerTile, final int maxHeight) {
        this.maxElementsPerTile = maxElementsPerTile;
        this.maxHeight = maxHeight;
        this.offsets = new double[2];
        this.cutPerformer = new CutPerformer();
        this.stubThreshold = stubThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
        this.tThreshold = tThreshold;
        this.simplifier = new VisvalingamWhyatt(tThreshold);
    }

    public void planarize(final List<Road> paths, final UnboundedPointAccess points, final Dimension mapSize) {
        this.nodeCounts = new int[points.getPoints()];
        this.originalPaths = paths;
        this.points = points;
        this.processedPaths = new ArrayList<>(paths.size());
        final int size = 1 << (int) Math.ceil(log2(Math.max(mapSize.getWidth(), mapSize.getHeight())));
        countNodes(paths);

        final Quadtree quadtree = createQuadtree(paths, points, size);
        final Iterator<ArrayList<Cut>> cutIterator = createCutList(paths, size, quadtree).iterator();
        for (final Iterator<Road> pathIterator = paths.iterator(); pathIterator.hasNext();) {
            final ArrayList<Cut> cuts = deduplicate(cutIterator.next());
            final Road element = pathIterator.next();
            final List<IntList> elements = cutPerformer.performCuts(element, cuts);
            final Iterator<IntList> iterator = elements.iterator();

            tryAppendEnd(iterator.next(), 0, !cuts.isEmpty() && cuts.get(0).equals(0, 0.), element);
            if (iterator.hasNext()) {
                for (int j = 0; j < elements.size() - 2; ++j) {
                    tryAppendInner(iterator.next(), element);
                }
                final IntList last = iterator.next();
                tryAppendEnd(last, last.size() - 1, cuts.get(cuts.size() - 1).equals(element.size() - 2, 1.), element);
            }
        }
    }

    public List<Road> getProcessedRoads() {
        return processedPaths;
    }

    private Quadtree createQuadtree(final List<? extends MultiElement> paths, final UnboundedPointAccess points,
            final int size) {
        final int[] maxWayCoordWidths = new int[maxHeight]; // use paths without any width
        final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points, maxWayCoordWidths);
        final Quadtree quadtree = new Quadtree(paths.size(), policy, size, maxHeight, maxElementsPerTile);
        return quadtree;
    }

    private void countNodes(final List<? extends MultiElement> paths) {
        for (final MultiElement path : paths) {
            ++nodeCounts[path.getNode(0)];
            ++nodeCounts[path.getNode(path.size() - 1)];
        }
    }

    private List<ArrayList<Cut>> createCutList(final List<? extends MultiElement> paths, final int mapSize,
            final Quadtree quadtree) {
        final ArrayList<ArrayList<Cut>> ret = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); ++i) {
            ret.add(new ArrayList<Cut>());
        }
        final FuzzyPointMap map = new FuzzyPointMap(mapSize, mapSize, fuzzyThreshold);
        intersectRec(ret, quadtree, map, 0, 0, mapSize);
        return ret;
    }

    private void intersectRec(final ArrayList<ArrayList<Cut>> cuts, final Quadtree quadtree, final FuzzyPointMap map,
            final int x, final int y, final int size) {
        if (quadtree.isLeaf()) {
            cutSegments(cuts, quadtree, map, x, y, size);
        } else {
            final int hs = size / 2;
            for (int i = 0; i < IQuadtree.NUM_CHILDREN; ++i) {
                intersectRec(cuts, quadtree.getChild(i), map, x + IQuadtree.getXOffset(i) * hs,
                        y + IQuadtree.getYOffset(i) * hs, hs);
            }
        }
    }

    private void cutSegments(final ArrayList<ArrayList<Cut>> cuts, final Quadtree quadtree, final FuzzyPointMap map,
            final int x, final int y, final int size) {
        final IntList elements = quadtree.getElements();
        for (int i = 0; i < elements.size() - 1; ++i) {
            final MultiElement u = originalPaths.get(elements.get(i));
            for (int ui = 0; ui < u.size() - 1; ++ui) {
                if (!segmentInTile(x, y, size, u, ui))
                    continue;

                for (int j = i + 1; j < elements.size(); ++j) {
                    final MultiElement v = originalPaths.get(elements.get(j));
                    for (int vi = 0; vi < v.size() - 1; ++vi) {
                        if (segmentInTile(x, y, size, v, vi) && intersectsBiased(u, v, ui, vi)) {
                            int intX = (int) (getX(u, ui) + offsets[0] * (getX(u, ui + 1) - getX(u, ui)));
                            int intY = (int) (getY(u, ui) + offsets[0] * (getY(u, ui + 1) - getY(u, ui)));
                            if (IntersectionUtil.rectangleContainsPoint(x, y, x + size, y + size, intX, intY)) {
                                int point = getFuzzyPoint(map, intX, intY);
                                cuts.get(elements.get(i)).add(new Cut(point, ui, offsets[0]));
                                cuts.get(elements.get(j)).add(new Cut(point, vi, offsets[1]));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean segmentInTile(final int x, final int y, final int size, final MultiElement u, int ui) {
        return IntersectionUtil.rectangleIntersectsSegment(x, y, size, size, getX(u, ui), getY(u, ui), getX(u, ui + 1),
                getY(u, ui + 1));
    }

    private int getFuzzyPoint(final FuzzyPointMap map, int intX, int intY) {
        int point = map.getPoint(intX, intY);
        if (point != -1) {
            return point + nodeCounts.length;
        }

        map.addPoint(intX, intY);
        point = points.getPoints();
        points.addPoint(intX, intY);
        return point;
    }

    private ArrayList<Cut> deduplicate(final ArrayList<Cut> cuts) {
        Collections.sort(cuts);

        final ArrayList<Cut> ret = new ArrayList<Cut>(cuts.size());

        Cut last = new Cut(0, -1, 0);
        for (final Cut current : cuts) {
            if (!last.equals(current) && last.getPoint() != current.getPoint())
                ret.add(current);
            last = current;
        }

        return ret;

    }

    private final int getX(final MultiElement e, final int index) {
        return points.getX(e.getNode(index));
    }

    private final int getY(final MultiElement e, final int index) {
        return points.getY(e.getNode(index));
    }

    private final boolean intersectsBiased(final MultiElement u, final MultiElement v, final int ui, final int vi) {
        final boolean intersect = IntersectionUtil.lineIntersectsline(getX(u, ui), getY(u, ui), getX(u, ui + 1),
                getY(u, ui + 1), getX(v, vi), getY(v, vi), getX(v, vi + 1), getY(v, vi + 1), offsets);
        return intersect && IntersectionUtil.inIntervall(offsets[0], getLowerOffset(u, ui), getUpperOffset(u, ui))
                && IntersectionUtil.inIntervall(offsets[1], getLowerOffset(v, vi), getUpperOffset(v, vi));
    }

    private double getUpperOffset(final MultiElement e, final int i) {
        return i != e.size() - 2 ? 1 : 1 + getExtendedOffset(e, i);
    }

    private double getLowerOffset(final MultiElement e, final int i) {
        return i != 0 ? 0 : -getExtendedOffset(e, i);
    }

    private double getExtendedOffset(final MultiElement e, final int i) {
        final double length = Point.distance(getX(e, i), getY(e, i), getX(e, i + 1), getY(e, i + 1));
        return tThreshold / length;
    }

    private void tryAppendInner(final IntList list, final Road element) {
        if (list.size() > 3 || list.get(0) != list.get(list.size() - 1)) {
            appendRoad(list, element);
        }
    }

    private void tryAppendEnd(final IntList list, final int lastIdx, final boolean duplicate, final Road element) {
        if (isLongEnough(list) || (!duplicate && !isOriginalDeadEnd(list.get(lastIdx)))) {
            appendRoad(list, element);
        }
    }

    private void appendRoad(final IntList list, final Road element) {
        final IntList simplified = simplifier.simplifyMultiline(points, list.iterator());
        processedPaths.add(new Road(simplified, element.getType(), element.getName(), element.getRoadId()));
    }

    private boolean isOriginalDeadEnd(final int endNode) {
        return isOriginalNode(endNode) && getNodeCount(endNode) == 1;
    }

    private boolean isLongEnough(final IntList element) {
        return ShapeUtil.getLength(element, points) > stubThreshold - IntersectionUtil.EPSILON;
    }

    private int getNodeCount(final int node) {
        return nodeCounts[node];
    }

    private boolean isOriginalNode(final int node) {
        return node < nodeCounts.length;
    }

    private final double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }
}
