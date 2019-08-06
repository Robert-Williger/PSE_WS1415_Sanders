package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
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
import adminTool.util.IntersectionUtil;
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

    private PointAccess points;
    private List<Road> paths;
    private List<Road> processedPaths;

    private List<Area> areas;

    public OverlapResolve(final IDrawInfo info) {
        this.info = info;
        this.isect = new float[2];
        this.cutPerformer = new CutPerformer();
    }

    public List<Road> getRoads() {
        return processedPaths;
    }

    public void resolve(final List<Road> origPaths, final PointAccess points, final Dimension2D mapSize) {
        this.points = points;

        this.paths = new ArrayList<>(origPaths.size());
        this.processedPaths = new ArrayList<>(origPaths.size());

        for (final Road road : origPaths) {
            if (road.getRoadType() == RoadType.Junction || road.getRoadType() == RoadType.Road)
                paths.add(road);
            else
                processedPaths.add(road);
        }
        this.sets = new ArrayList<>(paths.size());
        this.overlaps = new ArrayList<>(paths.size());
        this.areas = new ArrayList<>(paths.size());

        final ElementAdapter element = new ElementAdapter(points);
        for (final Road path : paths) {
            sets.add(new HashSet<>());
            overlaps.add(new ArrayList<>());
            element.setMultiElement(path);
            areas.add(new Area(ShapeUtil.createStrokedPath(element, (float) (info.getStrokeWidth(path.getType())),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
        }

        final double size = Math.max(mapSize.getWidth(), mapSize.getHeight());
        final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points,
                (index, height) -> info.getStrokeWidth(paths.get(index).getType()));
        final Quadtree quadtree = new Quadtree(paths.size(), policy, size, DEFAULT_MAX_HEIGHT,
                DEFAULT_MAX_ELEMENTS_PER_TILE);

        findOverlapsRec(quadtree, 0, 0, size);

        for (int i = 0; i < paths.size(); ++i) {
            final Road road = paths.get(i);
            if (overlaps.get(i).isEmpty())
                processedPaths.add(road);
            else if (road.getRoadType() == RoadType.Road) {
                final List<Cut> cuts = createCutList(overlaps.get(i));
                final List<IntList> elements = cutPerformer.performSortedCuts(road, cuts);

                processedPaths.add(new Road(elements.get(0), road.getType(), road.getRoadId(),
                        cuts.get(0).getOffset() > IntersectionUtil.EPSILON ? RoadType.Road : RoadType.Stub));
                processedPaths.add(new Road(elements.get(1), road.getType(), road.getRoadId(), RoadType.Overlap));
                processedPaths.add(new Road(elements.get(cuts.size()), road.getType(), road.getRoadId(),
                        cuts.get(cuts.size() - 1).getOffset() < 1 - IntersectionUtil.EPSILON ? RoadType.Road
                                : RoadType.Stub));

                for (int j = 2; j < elements.size() - 1; j += 2) {
                    processedPaths.add(new Road(elements.get(j), road.getType(), road.getRoadId(), RoadType.Road));
                    processedPaths
                            .add(new Road(elements.get(j + 1), road.getType(), road.getRoadId(), RoadType.Overlap));
                }
            } else {
                road.setRoadType(RoadType.Overlap);
                processedPaths.add(road);
            }
        }
    }

    private List<Cut> createCutList(final List<Overlap> sections) {
        final List<Cut> cuts = new ArrayList<Cut>();

        Collections.sort(sections);
        final Iterator<Overlap> iterator = sections.iterator();
        Overlap join = iterator.next();
        while (iterator.hasNext()) {
            final Overlap cur = iterator.next();
            double dif = join.to.getSegment() + join.to.getOffset() - cur.from.getSegment() - cur.from.getOffset();
            if (dif >= -IntersectionUtil.EPSILON)
                join.to = cur.to;
            else {
                cuts.add(join.from);
                cuts.add(join.to);
                join = cur;
            }
        }

        cuts.add(join.from);
        cuts.add(join.to);

        return cuts;
    }

    private void findOverlapsRec(final Quadtree tree, final double x, final double y, final double size) {
        if (tree.isLeaf())
            findOverlaps(tree, x, y, size);
        else {
            final double hs = size / 2;
            for (int i = 0; i < IQuadtree.NUM_CHILDREN; ++i) {
                findOverlapsRec(tree.getChild(i), x + IQuadtree.getXOffset(i) * hs, y + IQuadtree.getYOffset(i) * hs,
                        hs);
            }
        }
    }

    private void findOverlaps(final Quadtree quadtree, final double x, final double y, final double size) {
        final IntList elements = quadtree.getElements();
        for (int i = 0; i < elements.size() - 1; ++i) {
            final int ui = elements.get(i);

            for (int j = i + 1; j < elements.size(); ++j) {
                final int vi = elements.get(j);
                if (connected(paths.get(ui), paths.get(vi)) || !sets.get(ui).add(vi) || !sets.get(vi).add(ui))
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
        points.set(c.getPoint(), cut.x, cut.y);
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

        final Point2D last = new Point2D.Double();
        final Point2D current = new Point2D.Double();
        double minDistSq = Double.MAX_VALUE;

        last.setLocation(points.getX(path.getNode(0)), points.getY(path.getNode(0)));
        for (int node = 0; node < path.size() - 1; ++node) {
            current.setLocation(points.getX(path.getNode(node + 1)), points.getY(path.getNode(node + 1)));
            final double dx = current.getX() - last.getX();
            final double dy = current.getY() - last.getY();
            double s = ((point[0] - last.getX()) * dx + (point[1] - last.getY()) * dy) / (dx * dx + dy * dy);
            s = Math.min(1, Math.max(0, s));
            final double plumbX = last.getX() + s * dx;
            final double plumbY = last.getY() + s * dy;
            final double distanceSq = Point2D.distanceSq(plumbX, plumbY, point[0], point[1]);

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

    private static class Overlap implements Comparable<Overlap> {
        private Cut from;
        private Cut to;

        @Override
        public int compareTo(Overlap o) {
            return from.compareTo(o.from);
        }
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
