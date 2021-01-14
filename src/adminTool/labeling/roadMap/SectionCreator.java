package adminTool.labeling.roadMap;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import adminTool.VisvalingamWyatt;
import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.INameInfo;
import adminTool.labeling.IStringWidthInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadMap.decomposition.JunctionConnectionFilter;
import adminTool.labeling.roadMap.decomposition.LongRoadFilter;
import adminTool.labeling.roadMap.decomposition.RoadConnectionFilter;
import adminTool.labeling.roadMap.decomposition.WellShapedJunctionFilter;
import adminTool.util.ElementAdapter;
import util.IntList;

public class SectionCreator {

    private final IDrawInfo drawInfo;
    private final IStringWidthInfo stringWidthInfo;
    private final QualityMeasure qualityMeasure;

    private final double tCrossThreshold;
    private final double stubThreshold;
    private final double junctionThreshold;
    private final double fuzzyThreshold;
    private final double simplificationThreshold;
    private final double lengthThreshold;

    private List<LabelSection> roadSections;
    private List<LabelSection> junctionSections;
    private PointAccess points;
    private INameInfo nameInfo;
    private ILabelInfo labelInfo;

    public SectionCreator(final IDrawInfo drawInfo, final IStringWidthInfo stringWidthInfo,
            final QualityMeasure qualityMeasure, final double stubThreshold, final double tThreshold,
            final double fuzzyThreshold, final double simplificationThreshold, final double junctionThreshold,
            final double lengthThreshold) {
        this.drawInfo = drawInfo;
        this.stringWidthInfo = stringWidthInfo;
        this.qualityMeasure = qualityMeasure;

        this.stubThreshold = stubThreshold;
        this.tCrossThreshold = tThreshold;
        this.junctionThreshold = junctionThreshold;
        this.fuzzyThreshold = fuzzyThreshold;
        this.simplificationThreshold = simplificationThreshold;
        this.lengthThreshold = lengthThreshold;
    }

    public List<LabelSection> getRoadSections() {
        return roadSections;
    }

    public List<LabelSection> getJunctionSections() {
        return junctionSections;
    }

    public PointAccess getPoints() {
        return points;
    }

    public INameInfo getNameInfo() {
        return nameInfo;
    }

    public ILabelInfo getLabelInfo() {
        return labelInfo;
    }

    public void createSections(final Collection<Way> ways, final PointAccess points, final Rectangle2D mapBounds) {
        long start = System.currentTimeMillis();
        this.points = points;
        Collection<Way> visibleWays = ways.stream().filter(w -> drawInfo.isVisible(w.getType()))
                .collect(Collectors.toList());
        System.out.println(ways.size() + ", " + visibleWays.size());
        identify(visibleWays);
        visibleWays = null;
        System.out.println("planarize (" + roadSections.size() + ", " + junctionSections.size() + ")");
        planarize(mapBounds);
        System.out.println("fuse (" + roadSections.size() + ", " + junctionSections.size() + ")");
        fuse();
        System.out.println("simplify (" + roadSections.size() + ", " + junctionSections.size() + ")");
        simplify();
        System.out.println("transform (" + roadSections.size() + ", " + junctionSections.size() + ")");
        transform();
        System.out.println("filter (" + roadSections.size() + ", " + junctionSections.size() + ")");
        filter();
        System.out.println("resolve (" + roadSections.size() + ", " + junctionSections.size() + ")");
        resolve(mapBounds);
        System.out.println("subdivide (" + roadSections.size() + ", " + junctionSections.size() + ")");
        subdivide();
        System.out.println("decompose (" + roadSections.size() + ", " + junctionSections.size() + ")");
        decompose();
        double time = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("label section creation time: " + time + "s");
    }

    private void identify(final Collection<Way> ways) {
        Identification identification = new Identification();
        identification.identify(ways);
        roadSections = identification.getRoads();
        junctionSections = new ArrayList<>();
        nameInfo = identification.getNameInfo();
        int roadIds = identification.getRoadIds();

        double[] lengths = new double[roadIds];
        for (int roadId = 0; roadId < roadIds; ++roadId)
            lengths[roadId] = stringWidthInfo.getStringWidth(nameInfo.getName(roadId));
        labelInfo = roadId -> lengths[roadId];
    }

    private void planarize(final Rectangle2D mapBounds) {
        long start = System.currentTimeMillis();
        Planarization planarization = new Planarization(stubThreshold, tCrossThreshold, fuzzyThreshold);
        planarization.planarize(roadSections, points, mapBounds);
        roadSections = planarization.getRoads();
        System.out.println(System.currentTimeMillis() - start);
    }

    private void fuse() {
        Fusion fusion = new Fusion();
        fusion.fuse(roadSections);
        roadSections = fusion.getRoads();
    }

    private void simplify() {
        VisvalingamWyatt simplifier = new VisvalingamWyatt(simplificationThreshold);
        ElementAdapter adapter = new ElementAdapter(points);
        for (int i = 0; i < roadSections.size(); ++i) {
            final LabelSection label = roadSections.get(i);
            adapter.setMultiElement(label);
            final IntList simplified = simplifier.simplifyMultiline(adapter);
            for (int j = 0; j < simplified.size(); ++j) {
                simplified.set(j, label.getPoint(simplified.get(j)));
            }
            roadSections.set(i, new LabelSection(simplified, label.getType(), label.getRoadId()));
        }
    }

    private void transform() {
        // TODO own threshold
        Transformation transformation = new Transformation(drawInfo, nameInfo, junctionThreshold, tCrossThreshold);
        transformation.transform(roadSections, points);
        roadSections = transformation.getRoadSections();
        junctionSections = transformation.getJunctionSections();
    }

    private void filter() {
        WellShapedJunctionFilter wellShapedJunctionFilter = new WellShapedJunctionFilter(qualityMeasure);
        wellShapedJunctionFilter.filter(roadSections, junctionSections);
        roadSections = wellShapedJunctionFilter.getRoadSections();
        junctionSections = wellShapedJunctionFilter.getJunctionSections();

        JunctionConnectionFilter junctionConnectionFilter = new JunctionConnectionFilter();
        junctionConnectionFilter.filter(roadSections, junctionSections);
        roadSections = junctionConnectionFilter.getRoadSections();
        junctionSections = junctionConnectionFilter.getJunctionSections();
    }

    private void resolve(final Rectangle2D mapBounds) {
        long start = System.currentTimeMillis();
        final OverlapResolve resolve = new OverlapResolve(drawInfo);
        resolve.resolve(roadSections, junctionSections, points, mapBounds);
        roadSections = resolve.getRoadSections();
        junctionSections = resolve.getJunctionSections();
        System.out.println(System.currentTimeMillis() - start);
    }

    private void subdivide() {
        Subdivision subdivision = new Subdivision(lengthThreshold);
        subdivision.subdivide(roadSections, junctionSections, points);
        roadSections = subdivision.getRoadSections();
        junctionSections = subdivision.getJunctionSections();
    }

    private void decompose() {
        filter();

        LongRoadFilter longRoadFilter = new LongRoadFilter(qualityMeasure, labelInfo, points);
        longRoadFilter.filter(roadSections, junctionSections);
        roadSections = longRoadFilter.getRoadSections();
        junctionSections = longRoadFilter.getJunctionSections();

        RoadConnectionFilter roadConnectionFilter = new RoadConnectionFilter(qualityMeasure, labelInfo);
        roadConnectionFilter.filter(roadSections, junctionSections);
        roadSections = roadConnectionFilter.getRoadSections();
        junctionSections = roadConnectionFilter.getJunctionSections();
    }
}