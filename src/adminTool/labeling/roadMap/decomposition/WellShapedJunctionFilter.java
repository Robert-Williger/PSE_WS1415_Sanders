package adminTool.labeling.roadMap.decomposition;

import java.util.ArrayList;
import java.util.List;

import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadMap.LabelSection;

public class WellShapedJunctionFilter extends AbstractFilter {
    private final QualityMeasure qualityTest;

    public WellShapedJunctionFilter(final QualityMeasure qualityTest) {
        this.qualityTest = qualityTest;
    }

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        this.filteredSections = new ArrayList<>(junctionSections.size());
        this.junctionSections = new ArrayList<>(junctionSections.size());
        this.roadSections = roadSections;

        for (final LabelSection road : junctionSections)
            addJunctionSection(road);
    }

    private void addJunctionSection(final LabelSection road) {
        (qualityTest.isWellShaped(road) ? junctionSections : filteredSections).add(road);
    }
}
