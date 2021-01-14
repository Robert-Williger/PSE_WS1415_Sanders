package adminTool.labeling.roadMap;

import static adminTool.util.IntersectionUtil.lineIntersectsline;
import static adminTool.util.IntersectionUtil.inIntervall;
import static adminTool.util.IntersectionUtil.EPSILON;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;

import adminTool.elements.CutPerformer;
import adminTool.elements.PointAccess;
import adminTool.elements.CutPerformer.Cut;
import adminTool.quadtree.DynamicQuadtreeAccess;
import adminTool.quadtree.IQuadtree.ElementConsumer;
import adminTool.quadtree.policies.IQuadtreePolicy;
import adminTool.quadtree.policies.WayQuadtreePolicy;
import adminTool.util.ElementAdapter;
import adminTool.util.FuzzyPointMap;
import util.IntList;

public class Planarization {
    private static final int DEFAULT_MAX_ELEMENTS = 4;
    private static final int DEFAULT_MAX_HEIGHT = 20;

    private final int maxElements;
    private final int maxHeight;
    private FuzzyPointMap fuzzyMap;

    private final double stubThreshold;
    private final double fuzzyThreshold;
    private final double tCrossThreshold;

    private int[] nodeCounts;
    private List<LabelSection> processedRoads;
    private PointAccess points;

    private final double[] offsets;
    private ElementAdapter adapter;

    public Planarization() {
        this(0, 0, 0);
    }

    public Planarization(final double stubThreshold, final double tCrossThreshold, final double fuzzyThreshold) {
        this(stubThreshold, tCrossThreshold, fuzzyThreshold, DEFAULT_MAX_ELEMENTS, DEFAULT_MAX_HEIGHT);
    }

    public Planarization(final double stubThreshold, final double tCrossThreshold, final double fuzzyThreshold,
            final int maxElements, final int maxHeight) {
        this.maxElements = maxElements;
        this.maxHeight = maxHeight;
        this.offsets = new double[2];
        this.stubThreshold = stubThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
        this.tCrossThreshold = tCrossThreshold;
    }

    public List<LabelSection> getRoads() {
        return processedRoads;
    }

    public void planarize(final List<LabelSection> roads, final PointAccess points, final Rectangle2D mapBounds) {
        final double size = Math.max(mapBounds.getMaxX(), mapBounds.getMaxY());
        planarize(roads, points, size);
    }

    public void planarize(final List<LabelSection> roads, final PointAccess points, final double size) {
        this.processedRoads = new ArrayList<>(roads.size());
        this.points = points;
        this.adapter = new ElementAdapter(points);
        countNodes(roads);
        mapEndNodes(roads, size);

        final List<Rectangle2D> lineBounds = WayQuadtreePolicy.calculateLineBounds(roads, points);
        final IQuadtreePolicy policy = new WayQuadtreePolicy(roads, points, (index, height) -> 0, lineBounds);
        final DynamicQuadtreeAccess quadtree = new DynamicQuadtreeAccess(policy, maxHeight, maxElements, size);
        final ArrayList<List<Cut>> cuts = createInitialCutList(roads);

        Set<Integer> comparedElements = new HashSet<Integer>();
        ElementConsumer consumer = createElementConsumer(roads, cuts, lineBounds, comparedElements);
        for (int i = 0; i < roads.size(); ++i) {
            comparedElements.clear();
            quadtree.add(i, consumer);
        }

        cutRoads(roads, cuts);
    }

    private void countNodes(final List<LabelSection> roads) {
        nodeCounts = new int[points.size()];
        for (final LabelSection road : roads) {
            ++nodeCounts[road.getPoint(0)];
            ++nodeCounts[road.getPoint(road.size() - 1)];
        }
    }

    private void mapEndNodes(final List<LabelSection> roads, final double mapSize) {
        fuzzyMap = new FuzzyPointMap(mapSize, mapSize, fuzzyThreshold);
        for (final LabelSection road : roads) {
            putNode(road.getPoint(0));
            putNode(road.getPoint(road.size() - 1));
        }
    }

    private void putNode(final int node) {
        if (fuzzyMap.get(points.getX(node), points.getY(node)) == -1)
            fuzzyMap.put(points.getX(node), points.getY(node), node);
    }

    private ArrayList<List<Cut>> createInitialCutList(final List<LabelSection> roads) {
        final ArrayList<List<Cut>> cutList = new ArrayList<>(roads.size());
        for (int i = 0; i < roads.size(); ++i)
            cutList.add(new ArrayList<>());
        return cutList;
    }

    private ElementConsumer createElementConsumer(final List<LabelSection> roads, final List<List<Cut>> cuts,
            final List<Rectangle2D> lineBounds, final Set<Integer> comparedElements) {
        return (elements, x, y, size) -> {
            final int eu = elements.get(elements.size() - 1);
            final LabelSection ru = roads.get(eu);
            for (final OfInt it = elements.iterator(); it.hasNext();) {
                final int ev = it.nextInt();
                if (!comparedElements.add(ev) || !lineBounds.get(eu).intersects(lineBounds.get(ev)))
                    continue;

                final LabelSection rv = roads.get(ev);
                for (int su = 0; su < ru.size() - 1; ++su) {
                    // also check for self intersection
                    for (int sv = eu != ev ? 0 : su + 2; sv < rv.size() - 1; ++sv) {
                        if (intersectsBiased(ru, rv, su, sv))
                            update(cuts.get(eu), cuts.get(ev), su, sv, ru);
                    }
                }
            }
        };

    }

    private void cutRoads(final List<LabelSection> roads, final ArrayList<List<Cut>> cutList) {
        final CutPerformer cutPerformer = new CutPerformer();
        final Iterator<List<Cut>> cutIterator = cutList.iterator();
        for (final Iterator<LabelSection> pathIterator = roads.iterator(); pathIterator.hasNext();) {
            final List<Cut> cuts = cutIterator.next();
            if (cuts.isEmpty())
                processedRoads.add(pathIterator.next());
            else {
                final LabelSection element = pathIterator.next();
                final List<IntList> elements = cutPerformer.performCuts(element, cuts);
                final Iterator<IntList> iterator = elements.iterator();

                tryAppendOuter(createRoad(iterator.next(), element), 0, cuts.get(0).equals(0, 0.));

                for (int j = 0; j < elements.size() - 2; ++j) {
                    tryAppend(createRoad(iterator.next(), element));
                }

                final LabelSection road = createRoad(iterator.next(), element);
                tryAppendOuter(road, road.size() - 1, cuts.get(cuts.size() - 1).equals(element.size() - 2, 1.));
            }
        }
    }

    private void update(List<Cut> cu, List<Cut> cv, int su, int sv, LabelSection ru) {
        final double px = x(ru, su) + offsets[0] * (x(ru, su + 1) - x(ru, su));
        final double py = y(ru, su) + offsets[0] * (y(ru, su + 1) - y(ru, su));
        final int point = getPoint(px, py);
        cu.add(new Cut(point, su, offsets[0]));
        cv.add(new Cut(point, sv, offsets[1]));
    }

    private int getPoint(final double x, final double y) {
        int point = fuzzyMap.get(x, y);
        if (point != -1) {
            return point;
        }

        point = points.size();
        fuzzyMap.put(x, y, point);
        points.addPoint(x, y);
        return point;
    }

    private final double x(final LabelSection e, final int index) {
        return points.getX(e.getPoint(index));
    }

    private final double y(final LabelSection e, final int index) {
        return points.getY(e.getPoint(index));
    }

    private final boolean intersectsBiased(final LabelSection u, final LabelSection v, final int su, final int sv) {
        final boolean intersect = lineIntersectsline(x(u, su), y(u, su), x(u, su + 1), y(u, su + 1), x(v, sv), y(v, sv),
                x(v, sv + 1), y(v, sv + 1), offsets);
        return intersect && inIntervall(offsets[0], getLowerOffset(u, su), getUpperOffset(u, su))
                && inIntervall(offsets[1], getLowerOffset(v, sv), getUpperOffset(v, sv));
    }

    private double getUpperOffset(final LabelSection e, final int i) {
        return i != e.size() - 2 ? 1 : 1 + getExtendedOffset(e, i);
    }

    private double getLowerOffset(final LabelSection e, final int i) {
        return i != 0 ? 0 : -getExtendedOffset(e, i);
    }

    private double getExtendedOffset(final LabelSection e, final int i) {
        final double length = Point.distance(x(e, i), y(e, i), x(e, i + 1), y(e, i + 1));
        return tCrossThreshold / length;
    }

    private void tryAppend(final LabelSection element) {
        if (element.size() > 3 || element.getPoint(0) != element.getPoint(element.size() - 1))
            processedRoads.add(element);
    }

    private void tryAppendOuter(final LabelSection element, final int lastIdx, final boolean duplicate) {
        if (isLongEnough(element) || (!duplicate && !isOriginalDeadEnd(element.getPoint(lastIdx))))
            tryAppend(element);
    }

    private LabelSection createRoad(final IntList list, final LabelSection element) {
        return new LabelSection(list, element.getType(), element.getRoadId());
    }

    private boolean isOriginalDeadEnd(final int endNode) {
        return isOriginalNode(endNode) && getNodeCount(endNode) == 1;
    }

    private boolean isLongEnough(final LabelSection element) {
        adapter.setMultiElement(element);
        return adapter.getLength() > stubThreshold - EPSILON;
    }

    private int getNodeCount(final int node) {
        return nodeCounts[node];
    }

    private boolean isOriginalNode(final int node) {
        return node < nodeCounts.length;
    }
}
