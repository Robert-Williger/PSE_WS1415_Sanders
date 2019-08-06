package adminTool.labeling.roadGraph;

import adminTool.elements.MultiElement;
import util.IntList;

public class Road extends MultiElement {

    private int roadId;
    private RoadType roadType;

    public Road(MultiElement element, int type, final int roadId, final RoadType roadType) {
        super(element, type);
        this.roadId = roadId;
        this.roadType = roadType;
    }

    public Road(MultiElement element, int type, final int roadId) {
        this(element, type, roadId, RoadType.Road);
    }

    public Road(IntList indices, final int type, final int roadId, final RoadType roadType) {
        super(indices, type);
        this.roadId = roadId;
        this.roadType = roadType;
    }

    public Road(IntList indices, final int type, final int roadId) {
        this(indices, type, roadId, RoadType.Road);
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(final int roadId) {
        this.roadId = roadId;
    }

    public void setRoadType(RoadType roadType) {
        this.roadType = roadType;
    }

    public RoadType getRoadType() {
        return roadType;
    }

}
