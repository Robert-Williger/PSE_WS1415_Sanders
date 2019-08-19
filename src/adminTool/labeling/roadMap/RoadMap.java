package adminTool.labeling.roadMap;

import adminTool.labeling.Embedding;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.INameInfo;

public class RoadMap {

    private final RoadGraph graph;
    private final Embedding embedding;
    private final ILabelInfo labelInfo;
    private final INameInfo nameInfo;

    public RoadMap(RoadGraph graph, Embedding embedding, ILabelInfo labelInfo, INameInfo nameInfo) {
        super();
        this.graph = graph;
        this.embedding = embedding;
        this.labelInfo = labelInfo;
        this.nameInfo = nameInfo;
    }

    public RoadGraph getGraph() {
        return graph;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public ILabelInfo getLabelInfo() {
        return labelInfo;
    }

    public INameInfo getNameInfo() {
        return nameInfo;
    }
}
