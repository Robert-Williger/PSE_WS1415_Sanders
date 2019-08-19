package adminTool.labeling.roadMap.decomposition;

import java.util.ArrayList;
import java.util.List;

import adminTool.PointLocator;
import adminTool.elements.CutPerformer;
import adminTool.elements.PointAccess;
import adminTool.elements.CutPerformer.Cut;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.util.ElementAdapter;
import util.IntList;

public class LongRoadFilter extends AbstractFilter {
    private final ILabelInfo labelInfo;
    private final QualityMeasure qualityTest;
    private final PointAccess points;
    private final CutPerformer cutPerformer;

    private final ElementAdapter adapter;
    private final PointLocator locator;

    public LongRoadFilter(final QualityMeasure qualityTest, final ILabelInfo labelInfo, final PointAccess points) {
        this.labelInfo = labelInfo;
        this.qualityTest = qualityTest;
        this.points = points;
        this.cutPerformer = new CutPerformer();
        this.adapter = new ElementAdapter(points);
        this.locator = new PointLocator(points);
    }

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        this.filteredSections = new ArrayList<>();
        this.junctionSections = junctionSections;
        this.roadSections = new ArrayList<>(roadSections.size());

        filterLongRoads(roadSections);
    }

    private void filterLongRoads(final List<LabelSection> roadSections) {
        for (final LabelSection road : roadSections) {
            final double labelLength = labelInfo.getLength(road.getRoadId());
            adapter.setMultiElement(road);
            final double roadLength = adapter.getLength();
            if (!qualityTest.hasWellShapedPiece(road, labelLength))
                this.roadSections.add(road);
            else if (roadLength >= 2 * labelLength)
                appendTwoHalves(road, roadLength);
            else
                this.roadSections.add(road);
        }
    }

    private void appendTwoHalves(final LabelSection road, final double roadLength) {
        locator.locate(road, roadLength / 2);
        final Cut cut = new Cut(points.size(), locator.getSegment(), locator.getOffset());
        final List<IntList> halves = cutPerformer.performCut(road, cut);
        // add midpoint twice so that both halves do not share the same end point
        halves.get(1).set(0, points.size() + 1);
        for (final IntList half : halves) {
            points.addPoint(locator.getX(), locator.getY());
            roadSections.add(new LabelSection(half, road.getType(), road.getRoadId()));
        }
    }
}
