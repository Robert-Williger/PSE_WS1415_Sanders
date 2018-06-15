package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.simplification.Simplification;

public class RoadGraphCreator {

    private final DrawInfo info;

    private final int simplificationThreshold;
    private final int tThreshold;
    private final int stubThreshold;
    private final int junctionThreshold;
    private final int fuzzyThreshold;

    private List<MultiElement> paths;
    private UnboundedPointAccess points;
    private ITypeMap map;

    public RoadGraphCreator(final DrawInfo info, final int simplificationThreshold, final int stubThreshold,
            final int tThreshold, final int fuzzyThreshold, final int junctionThreshold) {
        this.info = info;
        this.simplificationThreshold = simplificationThreshold;
        this.stubThreshold = stubThreshold;
        this.tThreshold = tThreshold;
        this.junctionThreshold = junctionThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public IPointAccess getPoints() {
        return points;
    }

    public ITypeMap getMap() {
        return map;
    }

    public void createRoadGraph(final Collection<Way> ways, final IPointAccess origPoints, final Dimension mapSize) {
        Collection<List<Way>> identifiedWays = identify(ways);
        simplify(origPoints, identifiedWays);
        // planarize(mapSize);
        // transform();
        // resolve(mapSize);
        System.out.println("road graph creation done");
    }

    private Collection<List<Way>> identify(final Collection<Way> ways) {
        System.out.println("identifiy");
        Identification identification = new Identification();
        identification.identify(ways);
        Collection<List<Way>> identifiedWays = identification.getEqualWays();
        return identifiedWays;
    }

    private void simplify(final IPointAccess origPoints, final Collection<List<Way>> identifiedWays) {
        System.out.println("simplify");
        Simplification simplification = new Simplification(info, simplificationThreshold);
        simplification.simplify(identifiedWays, origPoints);
        paths = simplification.getPaths();
        points = simplification.getPoints();
        map = simplification.getTypeMap();
    }

    private void planarize(final Dimension mapSize) {
        System.out.println("planarize");
        Planarization planarization = new Planarization(stubThreshold, tThreshold, fuzzyThreshold);
        planarization.planarize(paths, points, mapSize);
        paths = planarization.getProcessedPaths();
    }

    private void transform() {
        System.out.println("transform");
        Transformation transformation = new Transformation(map, info, junctionThreshold);
        transformation.transform(paths, points);
        paths = transformation.getProcessedPaths();
    }

    private void resolve(final Dimension mapSize) {
        System.out.println("resolve");
        final OverlapResolve resolve = new OverlapResolve(map, info, tThreshold);
        resolve.resolve(paths, points, mapSize);
        paths = resolve.getProcessedPaths();
    }

}