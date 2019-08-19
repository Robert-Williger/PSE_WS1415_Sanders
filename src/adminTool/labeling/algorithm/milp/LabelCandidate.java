package adminTool.labeling.algorithm.milp;

import java.util.function.IntConsumer;

import util.DoubleInterval;
import util.IntList;

public class LabelCandidate {

    private final IntList edgePath;

    private int roadId;
    private boolean headEdgeReversed;
    private boolean tailEdgeReversed;
    private DoubleInterval interval;

    private LabelCandidate(final IntList edgePath) {
        this.edgePath = edgePath;
    }

    public LabelCandidate() {
        this(new IntList());
    }

    public LabelCandidate(LabelCandidate label) {
        this(new IntList(label.getEdgePath()));
        this.roadId = label.roadId;
        this.headEdgeReversed = label.headEdgeReversed;
        this.tailEdgeReversed = label.tailEdgeReversed;
        this.interval = label.interval;
    }

    public boolean isSingleEdge() {
        return edgePath.size() == 1;
    }

    public void setRoadId(final int roadId) {
        this.roadId = roadId;
    }

    public void setTailEdgeReversed(final boolean reversed) {
        this.tailEdgeReversed = reversed;
    }

    public void setHeadEdgeReversed(final boolean reversed) {
        this.headEdgeReversed = reversed;
    }

    public int getEdge(int index) {
        return edgePath.get(index);
    }

    public IntList getEdgePath() {
        return edgePath;
    }

    public int getHeadEdge() {
        return edgePath.get(0);
    }

    public int getTailEdge() {
        return edgePath.get(edgePath.size() - 1);
    }

    public int getRoadId() {
        return roadId;
    }

    public boolean isHeadEdgeReversed() {
        return headEdgeReversed;
    }

    public boolean isTailEdgeReversed() {
        return tailEdgeReversed;
    }

    public DoubleInterval getHeadInterval() {
        return interval;
    }

    public void setHeadInterval(final DoubleInterval interval) {
        this.interval = interval;
    }

    public void forEachInternal(final IntConsumer consumer) {
        for (int i = 1; i < edgePath.size() - 1; ++i) {
            consumer.accept(edgePath.get(i));
        }
    }

    public void forEachEdge(final IntConsumer consumer) {
        edgePath.forEach(consumer);
    }

}
