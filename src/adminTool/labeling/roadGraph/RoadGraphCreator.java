package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.io.File;
import java.util.Collection;

import adminTool.IOSMParser;
import adminTool.IPointAccess;
import adminTool.OSMParser;
import adminTool.elements.Way;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;

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

        System.out.println("identifiy");
        final Identification roadIdentifier = new Identification(ways);
        roadIdentifier.identify();

        System.out.println("simplify");
        final Simplification roadSimplifier = new Simplification(maxWayCoordWidth, threshold);
        roadSimplifier.simplify(roadIdentifier.getEqualWays(), projectionPoints);

        System.out.println("planarize");
        final Planarization planarization = new Planarization();
        planarization.planarize(roadSimplifier.getPaths(), roadSimplifier.getPoints(), mapSize);

        System.out.println("transform");
        final Transformation transformation = new Transformation(maxWayCoordWidth, threshold);
        transformation.transform(roadSimplifier.getPaths(), roadSimplifier.getPoints());
    }

}