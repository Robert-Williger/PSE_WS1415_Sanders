package adminTool.labeling.algorithm.postprocessing;

import java.util.Arrays;

import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.RoadGraph;
import util.IntList;

public class LabelReverser {
    private final RoadGraph graph;
    private final int[] reversedEdges;

    public LabelReverser(final RoadGraph graph) {
        this.graph = graph;
        reversedEdges = new int[graph.numEdges()];
        fillReversedEdgesMap();
    }

    private void fillReversedEdgesMap() {
        int[] sectionToEdge = new int[graph.numEdges() / 2];
        Arrays.fill(sectionToEdge, -1);
        for (int i = 0; i < graph.numNodes(); ++i) {
            for (int edge = graph.beginEdge(i); edge < graph.endEdge(i); ++edge) {
                final int other = sectionToEdge[graph.sectionId(edge)];
                if (other == -1)
                    sectionToEdge[graph.sectionId(edge)] = edge;
                else {
                    reversedEdges[edge] = other;
                    reversedEdges[other] = edge;
                }
            }
        }
    }

    public void reverse(final LabelPath label) {
        final IntList edgePath = label.getEdgePath();

        // reverse label's head and tail variables
        final double headStartCopy = label.getHeadPosition();
        label.setHeadPosition(graph.edgeLength(label.getTailEdge()) - label.getTailPosition());
        label.setTailPosition(graph.edgeLength(label.getHeadEdge()) - headStartCopy);

        // reverse edge path and its edges
        edgePath.reverse();
        for (int i = 0; i < edgePath.size(); ++i)
            edgePath.set(i, reversedEdges[edgePath.get(i)]);
    }
}
