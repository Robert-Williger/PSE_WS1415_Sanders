package adminTool.labeling;

import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.labeling.roadMap.LabelSection;

public class Embedding {

    private final List<LabelSection> sections;
    private final int[] sectionId;
    private final IPointAccess points;

    public Embedding(List<LabelSection> sections, int[] sectionId, IPointAccess points) {
        super();
        this.sections = sections;
        this.sectionId = sectionId;
        this.points = points;
    }

    public LabelSection getSection(final int edge) {
        return sections.get(sectionId[edge]);
    }

    public IPointAccess getPoints() {
        return points;
    }
}
