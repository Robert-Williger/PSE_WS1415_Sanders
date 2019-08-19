package adminTool.labeling.roadMap.decomposition;

import java.util.List;

import adminTool.labeling.roadMap.LabelSection;

public abstract class AbstractFilter {

    protected List<LabelSection> roadSections;
    protected List<LabelSection> junctionSections;
    protected List<LabelSection> filteredSections;

    public List<LabelSection> getRoadSections() {
        return roadSections;
    }

    public List<LabelSection> getJunctionSections() {
        return junctionSections;
    }

    public List<LabelSection> getFilteredSections() {
        return filteredSections;
    }

    public abstract void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections);
}
