package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.IOSMParser;
import adminTool.IPointAccess;
import adminTool.OSMParser;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.hull.HullCreator;
import adminTool.labeling.roadGraph.hull.HullSimplifier;
import adminTool.labeling.roadGraph.triangulation.Triangulator;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.WayQuadtreePolicy;
import util.IntList;

public class RoadGraphCreator {
    public static void main(final String[] args) {
        final IOSMParser parser = new OSMParser();
        try {
            parser.read(new File("default.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        Projector projector = new Projector(parser.getNodes(), new MercatorProjection());
        projector.performProjection();

        new RoadGraphCreator(parser.getWays(), projector.getPoints(), projector.getSize());
    }

    public RoadGraphCreator(final Collection<Way> ways, final IPointAccess projectionPoints, final Dimension mapSize) {
        final int maxWayCoordWidth = 20 << 5;
        final int threshold = 20 << 2;

        final RoadIdentifier roadIdentifier = new RoadIdentifier(ways);
        roadIdentifier.identify();

        final HullCreator hullCreator = new HullCreator(projectionPoints);
        final HullSimplifier hullSimplifier = new HullSimplifier();
        final Triangulator triangulator = new Triangulator();
        final PathFormer pathFormer = new PathFormer();

        List<MultiElement> paths = new ArrayList<MultiElement>();
        UnboundedPointAccess points = new UnboundedPointAccess();

        int equalWayNr = 0;
        for (final List<Way> equalWays : roadIdentifier.getEqualWays()) {
            hullCreator.createHulls(equalWays, maxWayCoordWidth);
            hullSimplifier.simplify(hullCreator.getHulls(), threshold);
            triangulator.triangulate(hullSimplifier.getPoints(), hullSimplifier.getOutlines(),
                    hullSimplifier.getHoles());
            pathFormer.formPaths(triangulator.getTriangulation(), maxWayCoordWidth);

            final IPointAccess pathPoints = pathFormer.getPoints();
            for (final IntList path : pathFormer.getPaths()) {
                final int[] indices = path.toArray();
                for (int i = 0; i < indices.length; ++i) {
                    indices[i] += points.getPoints();
                }
                paths.add(new MultiElement(indices, equalWayNr));
            }
            for (int i = 0; i < pathPoints.getPoints(); ++i) {
                points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
            }

            if (equalWayNr % 1000 == 0) {
                System.out.println(equalWayNr);
            }
            ++equalWayNr;

        }
        final int sizeLog = (int) Math.ceil(log2(Math.min(mapSize.getWidth(), mapSize.getHeight())));
        final int maxElementsPerTile = 8;
        final int maxHeight = 8;
        final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points, maxElementsPerTile, 0, maxHeight);
        Quadtree quadtree = new Quadtree(paths.size(), policy, 1 << sizeLog);

    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

}