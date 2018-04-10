package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.IOSMParser;
import adminTool.IPointAccess;
import adminTool.OSMParser;
import adminTool.BoundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;
import adminTool.quadtree.IQuadtreePolicy;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.WayQuadtreePolicy;
import util.IntList;

public class CreateRoadGraph {
    public static void main(final String[] args) {
        new CreateRoadGraph();
    }

    public CreateRoadGraph() {
        long start = System.currentTimeMillis();

        final IOSMParser parser = new OSMParser();
        try {
            parser.read(new File("default.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");
        start = System.currentTimeMillis();

        Projector projector = new Projector(parser.getNodes(), new MercatorProjection());
        projector.performProjection();
        final BoundedPointAccess projectionPoints = projector.getPoints();

        System.out.println("Projection time: " + (System.currentTimeMillis() - start) / 1000 + "s");

        RoadIdentifier roadIdentifier = new RoadIdentifier(parser.getWays());
        final Collection<List<Way>> identifiedWays = roadIdentifier.identify();

        final int maxWayCoordWidth = 20 << 5;
        final int threshold = 20 << 2;

        final HullCreator hullCreator = new HullCreator(projectionPoints);
        final HullSimplifier hullSimplifier = new HullSimplifier();
        final PolyWriter polyWriter = new PolyWriter("test");
        final Triangulator triangulator = new Triangulator("test");
        final TriangulationReader triangulationReader = new TriangulationReader("test");
        final PathFormer pathFormer = new PathFormer();

        List<MultiElement> paths = new ArrayList<MultiElement>();
        UnboundedPointAccess points = new UnboundedPointAccess();

        int equalWayNr = 0;

        long hullTime = 0;
        long simpTime = 0;
        long polyTime = 0;
        long triaTime = 0;
        long readTime = 0;
        long pathTime = 0;

        long time;
        for (final List<Way> equalWays : identifiedWays) {
            hullTime -= System.currentTimeMillis();
            hullCreator.createHulls(equalWays, maxWayCoordWidth);

            hullTime += System.currentTimeMillis();

            for (final Area hull : hullCreator.getHulls()) {
                simpTime -= System.currentTimeMillis();
                hullSimplifier.simplify(hull, threshold);

                time = System.currentTimeMillis();
                simpTime += time;
                polyTime -= time;
                try {
                    polyWriter.write(hullSimplifier.getPoints(), hullSimplifier.getOutline(),
                            hullSimplifier.getHoles());

                    time = System.currentTimeMillis();
                    polyTime += time;
                    triaTime -= time;

                    triangulator.triangulate();
                    time = System.currentTimeMillis();
                    triaTime += time;
                    readTime -= time;

                    triangulationReader.read();
                    time = System.currentTimeMillis();
                    readTime += time;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*
                 * pathFormer.formPaths(triangulationReader.getTriangulation()); pathTime += System.currentTimeMillis();
                 * 
                 * final IPointAccess pathPoints = pathFormer.getPoints(); for (final IntList path :
                 * pathFormer.getPaths()) { final int[] indices = path.toArray(); for (int i = 0; i < indices.length;
                 * ++i) { indices[i] += points.getPoints(); } paths.add(new MultiElement(indices, equalWayNr)); } for
                 * (int i = 0; i < pathPoints.getPoints(); ++i) { points.addPoint(pathPoints.getX(i),
                 * pathPoints.getY(i)); }
                 */
            }
            if (equalWayNr % 1000 == 0) {

                System.out.println("hullTime: " + hullTime / 1000.);
                System.out.println("simpTime: " + simpTime / 1000.);
                System.out.println("polyTime: " + polyTime / 1000.);
                System.out.println("triaTime: " + triaTime / 1000.);
                System.out.println("readTime: " + readTime / 1000.);
                System.out.println("pathTime: " + pathTime / 1000.);

                //System.out.println(equalWayNr);
            }
            ++equalWayNr;

        }
        System.out.println("building quadtree");

        /*
         * final Dimension size = projector.getSize(); final int sizeLog = (int)
         * Math.ceil(log2(Math.min(size.getWidth(), size.getHeight()))); final int maxElementsPerTile = 8; final int
         * maxHeight = 8; final IQuadtreePolicy policy = new WayQuadtreePolicy(paths, points, maxElementsPerTile,
         * maxWayCoordWidth, maxHeight); Quadtree quadtree = new Quadtree(paths.size(), policy, 1 << sizeLog);
         */
    }

    private double log2(final double value) {
        return (Math.log(value) / Math.log(2));
    }

}