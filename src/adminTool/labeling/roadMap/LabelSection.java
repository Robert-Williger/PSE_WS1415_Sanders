package adminTool.labeling.roadMap;

import adminTool.elements.MultiElement;
import util.IntList;

public class LabelSection extends MultiElement {

    private int roadId;

    public LabelSection(MultiElement element, final int type, final int roadId) {
        super(element, type);
        this.roadId = roadId;
    }

    public LabelSection(IntList indices, final int type, final int roadId) {
        super(indices, type);
        this.roadId = roadId;
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(final int roadId) {
        this.roadId = roadId;
    }

}
