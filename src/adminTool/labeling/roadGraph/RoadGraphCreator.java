package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.simplification.Simplification;

public class RoadGraphCreator {

    private final int maxWayCoordWidth;
    private final int threshold;
    private final int stubThreshold;

    private List<MultiElement> paths;

    public RoadGraphCreator(final int maxWayCoordWidth, final int simplificationThreshold, final int stubThreshold) {
        this.maxWayCoordWidth = maxWayCoordWidth;
        this.threshold = simplificationThreshold;
        this.stubThreshold = stubThreshold;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public void createRoadGraph(final Collection<Way> ways, final UnboundedPointAccess points,
            final Dimension mapSize) {

        System.out.println("identifiy");
        final Identification roadIdentifier = new Identification();
        roadIdentifier.identify(ways);

        System.out.println("simplify");
        final Simplification roadSimplifier = new Simplification(maxWayCoordWidth, threshold);
        roadSimplifier.simplify(roadIdentifier.getEqualWays(), points);

        System.out.println("planarize");
        final Planarization planarization = new Planarization(stubThreshold);
        planarization.planarize(roadSimplifier.getPaths(), roadSimplifier.getPoints(), mapSize);

        /*
         * System.out.println("transform"); final Transformation transformation = new Transformation(maxWayCoordWidth,
         * threshold); transformation.transform(roadSimplifier.getPaths(), roadSimplifier.getPoints());
         */

        paths = planarization.getProcessedPaths();
    }

}