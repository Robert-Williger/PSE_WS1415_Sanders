package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import java.util.List;
import java.util.Set;

import adminTool.elements.MultiElement;
import adminTool.elements.PointAccess;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.WayQuadtreePolicy;
import adminTool.util.ShapeUtil;
import util.IntList;

public class OverlapResolve {
    private static final int DEFAULT_MAX_ELEMENTS_PER_TILE = 8;
    private static final int DEFAULT_MAX_HEIGHT = 20;

    private final IDrawInfo info;
    private final CutPerformer cutPerformer;
    private List<Set<Integer>> sets;
    private List<List<Overlap>> overlaps;
    private float[] isect;

    private PointAccess.OfDouble points;
    private List<Road> paths;
    private List<Road> processedPaths;

    private List<Area> areas;

    public OverlapResolve(final IDrawInfo info) {
        this.info = info;
        this.isect = new float[2];
        this.cutPerformer = new CutPerformer();
    }

    public List<Road> getProcessedRoads() {
        return processedPaths;
    }

    public void resolve(final List<Road> paths, final PointAccess.OfDouble points, final Dimension mapSize) {
        this.points = points;
        this.paths = paths;
        this.processedPaths = new ArrayList<>(paths.size());
        this.sets = new ArrayList<>(paths.size());
        this.overlaps = new ArrayList<>(paths.size());
        this.areas = new ArrayList<>(paths.size());

        final ElementAdapter element = new ElementAdapter(points);
        for (final Road path : paths) {
            sets.add(new HashSet<>());
            overlaps.add(new ArrayList<>());
            element.setMultiElement(path);
            areas.add(new Area(ShapeUtil.createStrokedPath(element, info.getStrokeWidth(path.getType()),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
        }

        final int size = 1 << (int) Math.ceil(log2(Math.max(mapSize.getWidth(), mapSize.getHeight())));
        final WayQuadtreePolicy.IWayWidthInfo widthInfo = (index, height) -> info
                .getStrokeWidth(paths.get(index).getType());
        final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points, widthInfo);
        final Quadtree quadtree = new Quadtree(paths.size(), policy, size, DEFAULT_MAX_HEIGHT,
                DEFAULT_MAX_ELEMENTS_PER_TILE);
        findOverlapsRec(quadtree, 0, 0, size);

        for (int i = 0; i < paths.size(); ++i) {
            final Road road = paths.get(i);
            List<Cut> cuts = createCutList(overlaps.get(i));
            final List<IntList> elements = cutPerformer.performSortedCuts(road, cuts);

            for (int j = 0; j < elements.size(); ++j) {
                processedPaths.add(
                        new Road(elements.get(j), road.getType(), road.getName(), j % 2 == 0 ? road.getRoadId() : -2));
            }
        }
    }

    private List<Cut> createCutList(final List<Overlap> sections) {
        final List<Cut> cuts = new ArrayList<Cut>();
        if (!sections.isEmpty()) {
            Collections.sort(sections, new Comparator<Overlap>() {
                @Override
                public int compare(final Overlap o1, final Overlap o2) {
                    return o1.from.compareTo(o2.from);
                }
            });

            final Iterator<Overlap> iterator = sections.iterator();
            Overlap joinedSection = iterator.next();
            while (iterator.hasNext()) {
                final Overlap current = iterator.next();
                if (current.from.compareTo(joinedSection.to) < 0) {
                    joinedSection.to = current.to;
                } else {
                    cuts.add(joinedSection.from);
                    cuts.add(joinedSection.to);
                    joinedSection = current;
                }
            }

            cuts.add(joinedSection.from);
            cuts.add(joinedSection.to);
        }
        return cuts;
    }

    private void findOverlapsRec(final Quadtree tree, final int x, final int y, final int size) {
        if (tree.isLeaf())
            findOverlaps(tree, x, y, size);
        else {
            final int hs = size / 2;
            for (int i = 0; i < IQuadtree.NUM_CHILDREN; ++i) {
                findOverlapsRec(tree.getChild(i), x + IQuadtree.getXOffset(i) * hs, y + IQuadtree.getYOffset(i) * hs,
                        hs);
            }
        }
    }

    private void findOverlaps(final Quadtree quadtree, final int x, final int y, final int size) {
        final IntList elements = quadtree.getElements();
        for (int i = 0; i < elements.size() - 1; ++i) {
            final int ui = elements.get(i);

            for (int j = i + 1; j < elements.size(); ++j) {
                final int vi = elements.get(j);
                if (!sets.get(ui).add(vi) || !sets.get(vi).add(ui))
                    continue;

                final Area intersection = new Area(areas.get(vi));
                intersection.intersect(areas.get(ui));
                final int ei = paths.get(ui).getRoadId() < paths.get(vi).getRoadId() ? ui : vi;
                addOverlaps(intersection.getPathIterator(null), paths.get(ei), overlaps.get(ei));
            }
        }
    }

    private boolean connected(final MultiElement a, final MultiElement b) {
        return a.getNode(0) == b.getNode(0) || a.getNode(0) == b.getNode(b.size() - 1)
                || a.getNode(a.size() - 1) == b.getNode(0) || a.getNode(a.size() - 1) == b.getNode(b.size() - 1);
    }

    private void addOverlaps(final PathIterator iterator, final MultiElement e, final List<Overlap> overlaps) {
        Overlap sect = null;

        while (!iterator.isDone()) {
            switch (iterator.currentSegment(isect)) {
                case PathIterator.SEG_MOVETO:
                    sect = createSection(calculatePlumb(isect, e));
                    break;
                case PathIterator.SEG_LINETO:
                    updateSection(sect, calculatePlumb(isect, e));
                    break;
                case PathIterator.SEG_CLOSE:
                    // if (!sect.from.equals(0, 0) && !sect.to.equals(e.size() - 2, 1))
                    overlaps.add(sect);
                    break;
            }
            iterator.next();
        }
    }

    private void updateSection(final Overlap section, final CoordCut cut) {
        if (cut.compareTo(section.from) < 0) {
            updateCut(section.from, cut);
        } else if (cut.compareTo(section.to) > 0) {
            updateCut(section.to, cut);
        }
    }

    private void updateCut(final Cut c, final CoordCut cut) {
        c.setOffset(cut.getOffset());
        c.setSegment(cut.getSegment());
        points.setPoint(c.getPoint(), cut.x, cut.y);
    }

    private Overlap createSection(final CoordCut cut) {
        final Overlap ret = new Overlap();

        ret.from = new Cut(points.size(), cut.getSegment(), cut.getOffset());
        points.addPoint(cut.x, cut.y);
        ret.to = new Cut(points.size(), cut.getSegment(), cut.getOffset());
        points.addPoint(cut.x, cut.y);

        return ret;
    }

    private CoordCut calculatePlumb(final float[] point, final MultiElement path) {
        final CoordCut ret = new CoordCut(points.size(), -1, 0);

        final Point last = new Point();
        final Point current = new Point();
        double minDistSq = Double.MAX_VALUE;

        last.setLocation(points.getX(path.getNode(0)), points.getY(path.getNode(0)));
        for (int node = 0; node < path.size() - 1; ++node) {
            current.setLocation(points.getX(path.getNode(node + 1)), points.getY(path.getNode(node + 1)));
            final int dx = current.x - last.x;
            final int dy = current.y - last.y;
            double s = (((double) point[0] - last.x) * dx + (point[1] - last.y) * dy) / (dx * dx + dy * dy);
            s = Math.min(1, Math.max(0, s));
            final double plumbX = last.x + s * dx;
            final double plumbY = last.y + s * dy;
            final double distanceSq = Point.distanceSq(plumbX, plumbY, point[0], point[1]);

            if (distanceSq < minDistSq) {
                minDistSq = distanceSq;
                ret.setOffset(s);
                ret.setSegment(node);
                ret.setLocation(plumbX, plumbY);
            }
            last.setLocation(current);
        }

        return ret;
    }

    private final double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

    private static class Overlap {
        private Cut from;
        private Cut to;
    }

    private static class CoordCut extends Cut {
        public CoordCut(int point, int segment, double offset) {
            super(point, segment, offset);
        }

        private double x;
        private double y;

        public void setLocation(final double x, final double y) {
            this.x = x;
            this.y = y;
        }
    }
}
