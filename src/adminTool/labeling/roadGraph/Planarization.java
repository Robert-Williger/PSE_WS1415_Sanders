package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.Util;
import adminTool.elements.MultiElement;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.WayQuadtreePolicy;
import util.IntList;

public class Planarization {
    private static final int DEFAULT_MAX_ELEMENTS_PER_TILE = 32;
    private static final int DEFAULT_MAX_HEIGHT = 32;

    private final int maxElementsPerTile;
    private final int maxHeight;
    private final CutPerformer cutPerformer;
    private final int stubThreshold;

    private List<? extends MultiElement> originalPaths;
    private List<MultiElement> processedPaths;
    private UnboundedPointAccess points;

    private final double[] offsets;

    public Planarization() {
        this(0);
    }

    public Planarization(final int stubThreshold) {
        this(stubThreshold, DEFAULT_MAX_ELEMENTS_PER_TILE, DEFAULT_MAX_HEIGHT);
    }

    public Planarization(final int stubThreshold, final int maxElementsPerTile, final int maxHeight) {
        this.maxElementsPerTile = maxElementsPerTile;
        this.maxHeight = maxHeight;
        this.offsets = new double[2];
        this.cutPerformer = new CutPerformer();
        this.stubThreshold = stubThreshold;
    }

    public void planarize(final List<? extends MultiElement> paths, final UnboundedPointAccess points,
            final Dimension mapSize) {
        this.originalPaths = paths;
        this.points = points;
        this.processedPaths = new ArrayList<MultiElement>(paths.size());
        final int size = 1 << (int) Math.ceil(log2(Math.max(mapSize.getWidth(), mapSize.getHeight())));

        final Quadtree quadtree = createQuadtree(paths, points, size);
        final List<List<Cut>> cuts = createCutList(paths, size, quadtree);
        for (int i = 0; i < paths.size(); ++i) {
            final List<MultiElement> elements = cutPerformer.performCuts(paths.get(i), cuts.get(i));
            final Iterator<MultiElement> iterator = elements.iterator();

            tryAppend(iterator.next());
            if (elements.size() > 1) {
                for (int j = 1; j < elements.size() - 1; ++j) {
                    processedPaths.add(iterator.next());
                }
                tryAppend(iterator.next());
            }
        }
    }

    public List<MultiElement> getProcessedPaths() {
        return processedPaths;
    }

    private void tryAppend(final MultiElement element) {
        if (Util.getLength(element, points) > stubThreshold - Util.EPSILON) {
            processedPaths.add(element);
        }
    }

    private Quadtree createQuadtree(final List<? extends MultiElement> paths, final UnboundedPointAccess points,
            final int size) {
        final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points, maxElementsPerTile, 0, maxHeight);
        final Quadtree quadtree = new Quadtree(paths.size(), policy, size);
        return quadtree;
    }

    private ArrayList<List<Cut>> createCutList(final List<? extends MultiElement> paths, final int mapSize,
            final Quadtree quadtree) {
        ArrayList<List<Cut>> ret = new ArrayList<List<Cut>>(paths.size());
        for (int i = 0; i < paths.size(); ++i) {
            ret.add(new ArrayList<Cut>());
        }
        intersectRec(ret, quadtree, 0, 0, mapSize);
        return ret;
    }

    private void intersectRec(final ArrayList<List<Cut>> cuts, final Quadtree quadtree, final int x, final int y,
            final int size) {
        if (quadtree.isLeaf()) {
            final IntList elements = quadtree.getElements();
            for (int u = 0; u < elements.size() - 1; ++u) {
                final MultiElement eu = originalPaths.get(elements.get(u));
                for (int i = 0; i < eu.size() - 1; ++i) {
                    for (int v = u + 1; v < elements.size(); ++v) {
                        final MultiElement ev = originalPaths.get(elements.get(v));
                        if (eu.getType() != ev.getType()) {
                            for (int j = 0; j < ev.size() - 1; ++j) {
                                final boolean intersects = Util.lineIntersectsLine(getX(eu, i), getY(eu, i),
                                        getX(eu, i + 1), getY(eu, i + 1), getX(ev, j), getY(ev, j), getX(ev, j + 1),
                                        getY(ev, j + 1), offsets);
                                if (intersects) {
                                    int intX = (int) (getX(eu, i) + offsets[0] * (getX(eu, i + 1) - getX(eu, i)));
                                    int intY = (int) (getY(eu, i) + offsets[0] * (getY(eu, i + 1) - getY(eu, i)));
                                    if (Util.rectangleContainsPoint(x, y, x + size, y + size, intX, intY)) {
                                        final int point = points.getPoints();
                                        points.addPoint(intX, intY);
                                        cuts.get(elements.get(u)).add(new Cut(point, i, offsets[0]));
                                        cuts.get(elements.get(v)).add(new Cut(point, j, offsets[1]));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            final Quadtree[] children = quadtree.getChildren();
            final int hs = size / 2;
            for (int i = 0; i < 4; ++i) {
                intersectRec(cuts, children[i], x + Quadtree.getXOffset(i) * hs, y + Quadtree.getYOffset(i) * hs, hs);
            }
        }
    }

    private final int getX(final MultiElement e, final int index) {
        return points.getX(e.getNode(index));
    }

    private final int getY(final MultiElement e, final int index) {
        return points.getY(e.getNode(index));
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }
}
