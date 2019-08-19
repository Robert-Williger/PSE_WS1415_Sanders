package adminTool.labeling;

import adminTool.elements.MultiElement;
import util.IntList;

public class LabelPath {

    private final IntList edgePath;
    private double headPosition;
    private double tailPosition;
    private int roadId;

    public LabelPath(final LabelPath label) {
        this(label.edgePath, label.headPosition, label.tailPosition, label.roadId);
    }

    public LabelPath(IntList edgePath, double headPosition, double tailPosition, int roadId) {
        super();
        this.edgePath = edgePath;
        this.headPosition = headPosition;
        this.tailPosition = tailPosition;
        this.roadId = roadId;
    }

    public int getHeadEdge() {
        return edgePath.get(0);
    }

    public int getTailEdge() {
        return edgePath.get(edgePath.size() - 1);
    }

    public IntList getEdgePath() {
        return edgePath;
    }

    public double getHeadPosition() {
        return headPosition;
    }

    public double getTailPosition() {
        return tailPosition;
    }

    public void setHeadPosition(double headPosition) {
        this.headPosition = headPosition;
    }

    public void setTailPosition(double tailPosition) {
        this.tailPosition = tailPosition;
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public MultiElement toElement(final Embedding embedding) {
        final IntList nodes = new IntList();
        edgePath.forEach(e -> {
            nodes.addAll(embedding.getSection(e).toList());
            if (e != edgePath.get(edgePath.size() - 1))
                nodes.removeIndex(nodes.size() - 1);
        });
        return new MultiElement(nodes, roadId);
    }

    @Override
    public String toString() {
        return "LabelPath [edgePath=" + edgePath + ", headPosition=" + headPosition + ", tailPosition=" + tailPosition
                + ", roadId=" + roadId + "]";
    }

}
