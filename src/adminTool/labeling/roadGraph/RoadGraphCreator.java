package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.elements.UnboundedPointAccess;
import adminTool.elements.Way;

public class RoadGraphCreator {

    private final DrawInfo info;

    private final int tThreshold;
    private final int stubThreshold;
    private final int junctionThreshold;
    private final int fuzzyThreshold;

    private List<Road> roads;
    private UnboundedPointAccess points;

    public RoadGraphCreator(final DrawInfo info, final int stubThreshold, final int tThreshold,
            final int fuzzyThreshold, final int junctionThreshold) {
        this.info = info;
        this.stubThreshold = stubThreshold;
        this.tThreshold = tThreshold;
        this.junctionThreshold = junctionThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public IPointAccess getPoints() {
        return points;
    }

    public void createRoadGraph(final Collection<Way> ways, final IPointAccess origPoints, final Dimension mapSize) {
        identify(ways, origPoints);
        planarize(mapSize);
        resolve(mapSize);
        // transform();
        System.out.println("road graph creation done");
    }

    private void identify(final Collection<Way> ways, final IPointAccess origPoints) {
        long start = System.currentTimeMillis();

        Identification identification = new Identification();
        identification.identify(ways, origPoints);
        roads = identification.getRoads();
        points = identification.getPoints();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("identify: " + time + "s");
    }

    private void planarize(final Dimension mapSize) {
        long start = System.currentTimeMillis();

        Planarization planarization = new Planarization(stubThreshold, tThreshold, fuzzyThreshold);
        planarization.planarize(roads, points, mapSize);
        roads = planarization.getProcessedRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("planarize: " + time + "s");
    }

    private void transform() {
        long start = System.currentTimeMillis();

        Transformation transformation = new Transformation(info, junctionThreshold);
        transformation.transform(roads, points);
        roads = transformation.getProcessedRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("transform: " + time + "s");
    }

    private void resolve(final Dimension mapSize) {
        long start = System.currentTimeMillis();

        final OverlapResolve resolve = new OverlapResolve(info);
        resolve.resolve(roads, points, mapSize);
        roads = resolve.getProcessedRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("resolve: " + time + "s");
    }

}