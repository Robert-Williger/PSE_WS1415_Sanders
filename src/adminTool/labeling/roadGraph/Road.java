package adminTool.labeling.roadGraph;

import adminTool.elements.Way;
import util.IntList;

public class Road extends Way {

    private int roadId;

    public Road(int[] indices, int type, String name, final int roadId) {
        super(indices, type, name, false);
        this.roadId = roadId;
    }

    public Road(IntList indices, int type, String name, final int roadId) {
        super(indices, type, name, false);
        this.roadId = roadId;
    }

    public int getRoadId() {
        return roadId;
    }

}
