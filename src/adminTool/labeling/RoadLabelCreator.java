package adminTool.labeling;

import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.elements.LineLabel;
import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import adminTool.labeling.algorithm.IRoadMapLabelAlgorithm;
import adminTool.labeling.algorithm.Postprocessor;
import adminTool.labeling.algorithm.milp.MILPSolver;
import adminTool.labeling.algorithm.postprocessing.LabelShifter;
import adminTool.labeling.algorithm.postprocessing.LabelSpinner;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.labeling.roadMap.RoadMap;
import adminTool.labeling.roadMap.RoadMapsCreator;
import adminTool.labeling.roadMap.SectionCreator;
import adminTool.metrics.IDistanceMap;

public class RoadLabelCreator {

    private static final double MAX_WAY_WIDTH = 18;
    private static final double FUZZY_THRESHOLD = 1;
    private static final double SIMPLIFICATION_THRESHOLD = 4;
    private static final double T_THRESHOLD = 2;
    private static final double SECTION_LENGTH_THRESHOLD = 350;

    private static final double ALPHA_MAX = 22.5 / 180 * Math.PI; // 22.5°
    private static final double L_MAX = 20;

    private static final double STUB_THRESHOLD = MAX_WAY_WIDTH;
    private static final double JUNCTION_THRESHOLD = 2 * MAX_WAY_WIDTH;

    private static final double OVERLAP_OFFSET = 10;

    private Collection<Way> ways;
    private Dimension2D mapSize;

    private List<LineLabel> labeling;
    private PointAccess points;

    public RoadLabelCreator(final Collection<Way> ways, final IPointAccess points, final Dimension2D mapSize) {
        this.ways = ways;
        this.mapSize = mapSize;

        this.points = new PointAccess(points.size());
        for (int i = 0; i < points.size(); ++i) {
            this.points.addPoint(points.getX(i), points.getY(i));
        }
    }

    public void createLabels(final IDistanceMap pixelsToCoords, final IDrawInfo drawInfo, final int zoom) {
        this.labeling = new ArrayList<>();

        final double fuzzyThreshold = pixelsToCoords.map(FUZZY_THRESHOLD);
        final double tThreshold = pixelsToCoords.map(T_THRESHOLD);
        final double lengthThreshold = pixelsToCoords.map(SECTION_LENGTH_THRESHOLD);
        final double lMax = pixelsToCoords.map(L_MAX);
        final double stubThreshold = pixelsToCoords.map(STUB_THRESHOLD);
        final double junctionThreshold = pixelsToCoords.map(JUNCTION_THRESHOLD);
        final double overlapOffset = pixelsToCoords.map(OVERLAP_OFFSET);
        final double simplificationThreshold = Math.pow(pixelsToCoords.map(SIMPLIFICATION_THRESHOLD), 2);

        QualityMeasure qualityMeasure = new QualityMeasure(points, lMax, ALPHA_MAX);

        SectionCreator sectionCreator = new SectionCreator(drawInfo, new StringWidthInfo(pixelsToCoords),
                qualityMeasure, stubThreshold, tThreshold, fuzzyThreshold, simplificationThreshold, junctionThreshold,
                lengthThreshold);
        sectionCreator.createSections(ways, points, mapSize);
        List<LabelSection> roadSections = sectionCreator.getRoadSections();
        List<LabelSection> junctionSections = sectionCreator.getJunctionSections();
        points = sectionCreator.getPoints();

        RoadMapsCreator roadMapsCreator = new RoadMapsCreator();
        roadMapsCreator.createMaps(roadSections, junctionSections, points, sectionCreator.getLabelInfo(),
                sectionCreator.getNameInfo());
        Collection<RoadMap> roadMaps = roadMapsCreator.getRoadMaps();

        IRoadMapLabelAlgorithm labelingAlgorithm = new MILPSolver(qualityMeasure, overlapOffset);

        LabelShifter shifter = new LabelShifter();
        LabelSpinner spinner = new LabelSpinner();
        Postprocessor postprocessor = new Postprocessor(sectionCreator.getPoints());
        for (final RoadMap roadMap : roadMaps) {
            labelingAlgorithm.calculateLabeling(roadMap);
            shifter.postprocess(roadMap, labelingAlgorithm.getLabeling(), qualityMeasure);
            spinner.postprocess(roadMap, shifter.getLabeling());
            postprocessor.postprocess(roadMap, shifter.getLabeling(), zoom);
            labeling.addAll(postprocessor.getLabeling());
        }
    }

    public Collection<LineLabel> getLabeling() {
        return labeling;
    }

    public IPointAccess getPoints() {
        return points;
    }
}
