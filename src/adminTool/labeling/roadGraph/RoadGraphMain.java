package adminTool.labeling.roadGraph;

import java.io.File;
import java.util.Collection;

import adminTool.IOSMParser;
import adminTool.OSMParser;
import adminTool.elements.Way;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;

public class RoadGraphMain {
    public static void main(final String[] args) {
        final int maxWayCoordWidth = 20 << 5;
        final int simplificationThreshold = 20 << 2;
        final int stubThreshold = 20 << 3;
        IOSMParser parser = new OSMParser();
        try {
            parser.read(new File("default.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        Projector projector = new Projector(parser.getNodes(), new MercatorProjection());
        final Collection<Way> ways = parser.getWays();
        parser = null;
        projector.performProjection();

        RoadGraphCreator roadGraphCreator = new RoadGraphCreator(maxWayCoordWidth, simplificationThreshold,
                stubThreshold);
        roadGraphCreator.createRoadGraph(ways, projector.getPoints(), projector.getSize());
    }
}
