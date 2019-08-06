package adminTool.labeling.roadGraph;

import java.awt.geom.Dimension2D;
import java.util.Collection;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.INameInfo;
import adminTool.labeling.IStringWidthInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadGraph.filter.JunctionFilter;
import adminTool.labeling.roadGraph.filter.LongRoadFilter;
import adminTool.labeling.roadGraph.filter.QualityFilter;

public class RoadGraphCreator {

    private final IDrawInfo drawInfo;
    private final IStringWidthInfo stringWidthInfo;

    private final double tCrossThreshold;
    private final double stubThreshold;
    private final double junctionThreshold;
    private final double fuzzyThreshold;
    private final double lengthThreshold;

    private final double lMax;
    private final double alphaMax;

    private int roadIds;
    private List<Road> roads;
    private PointAccess points;
    private INameInfo nameInfo;

    public RoadGraphCreator(final IDrawInfo drawInfo, final IStringWidthInfo stringWidthInfo,
            final double stubThreshold, final double tThreshold, final double fuzzyThreshold,
            final double junctionThreshold, final double lMax, final double alphaMax, final double lengthThreshold) {
        this.drawInfo = drawInfo;
        this.stringWidthInfo = stringWidthInfo;
        this.stubThreshold = stubThreshold;
        this.tCrossThreshold = tThreshold;
        this.junctionThreshold = junctionThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
        this.lengthThreshold = lengthThreshold;

        this.lMax = lMax;
        this.alphaMax = alphaMax;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public IPointAccess getPoints() {
        return points;
    }

    public void createRoadGraph(final Collection<Way> ways, final IPointAccess points, final Dimension2D mapSize) {
        identify(ways, points);
        planarize(mapSize);
        fuse();
        transform();
        filter();
        //resolve(mapSize);
        //filter();
        subdivide();
        decompose();
        System.out.println("road graph creation done");
    }

    private void identify(final Collection<Way> ways, final IPointAccess origPoints) {
        long start = System.currentTimeMillis();

        Identification identification = new Identification();
        identification.identify(ways, origPoints);
        roads = identification.getRoads();
        points = identification.getPoints();
        nameInfo = identification.getNameInfo();
        roadIds = identification.getRoadIds();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("identify: " + time + "s");
    }

    private void fuse() {
        long start = System.currentTimeMillis();

        Fusion fusion = new Fusion();
        fusion.fuse(roads);
        roads = fusion.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("fuse: " + time + "s");
    }

    private void planarize(final Dimension2D mapSize) {
        long start = System.currentTimeMillis();

        Planarization planarization = new Planarization(stubThreshold, tCrossThreshold, fuzzyThreshold);
        planarization.planarize(roads, points, mapSize);
        roads = planarization.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("planarize: " + time + "s");
    }

    private void transform() {
        long start = System.currentTimeMillis();

        // TODO own threshold
        Transformation transformation = new Transformation(drawInfo, junctionThreshold, tCrossThreshold);
        transformation.transform(roads, points);
        roads = transformation.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("transform: " + time + "s");
    }

    private void filter() {
        long start = System.currentTimeMillis();

        int nodeMax = 0;
        for (final Road road : roads)
            nodeMax = Math.max(nodeMax, road.size());
        QualityMeasure qualityMeasure = new QualityMeasure(points, lMax, alphaMax, nodeMax);

        QualityFilter qualityFilter = new QualityFilter(qualityMeasure);
        qualityFilter.filter(roads);
        roads = qualityFilter.getRoads();

        JunctionFilter junctionFilter = new JunctionFilter();
        junctionFilter.filter(roads);
        roads = junctionFilter.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("filter: " + time + "s");
    }

    private void resolve(final Dimension2D mapSize) {
        long start = System.currentTimeMillis();

        final OverlapResolve resolve = new OverlapResolve(drawInfo);
        resolve.resolve(roads, points, mapSize);
        roads = resolve.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("resolve: " + time + "s");
    }

    private void subdivide() {
        long start = System.currentTimeMillis();

        Subdivision subdivision = new Subdivision(lengthThreshold);
        subdivision.subdivide(roads, points);
        roads = subdivision.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("subdivision: " + time + "s");
    }

    private void decompose() {
        long start = System.currentTimeMillis();

        int nodeMax = 0;
        for (final Road road : roads)
            nodeMax = Math.max(nodeMax, road.size());
        QualityMeasure qualityMeasure = new QualityMeasure(points, lMax, alphaMax, nodeMax);

        double[] lengths = new double[roadIds];
        for (int roadId = 0; roadId < roadIds; ++roadId)
            lengths[roadId] = stringWidthInfo.getStringWidth(nameInfo.getName(roadId));
        ILabelInfo labelInfo = (roadId) -> lengths[roadId];

        LongRoadFilter longRoadFilter = new LongRoadFilter(qualityMeasure, labelInfo, points);
        longRoadFilter.filter(roads);
        roads = longRoadFilter.getRoads();

        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("filter: " + time + "s");
    }
}