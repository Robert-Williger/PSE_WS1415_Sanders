package adminTool.labeling.algorithm.milp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.algorithm.HeadIntervalFinder;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import util.DoubleInterval;
import util.IntList;

public class CandidateCreator {

    private RoadGraph graph;
    private ILabelInfo labelInfo;
    private List<LabelCandidate> candidates;
    private HeadIntervalFinder intervalFinder;
    private final QualityMeasure measure;

    public CandidateCreator(QualityMeasure measure) {
        super();
        this.measure = measure;
    }

    public Collection<LabelCandidate> getCandidates() {
        return candidates;
    }

    public void createCandidates(RoadMap roadMap) {
        init(roadMap, measure);

        final LabelCandidate label = new LabelCandidate();
        final IntList edgePath = label.getEdgePath();
        boolean[] marked = new boolean[graph.numNodes()];
        for (int node = 0; node < graph.numNodes(); ++node) {
            if (graph.isJunction(node))
                continue;

            for (int edge = graph.beginEdge(node); edge < graph.endEdge(node); ++edge) {
                final int next = graph.edgeHead(edge);
                if (graph.isJunction(next))
                    continue;

                // edge is a road section
                label.setHeadEdgeReversed(isReversed(node, next));
                label.setTailEdgeReversed(isReversed(node, next));
                label.setRoadId(graph.road(node));
                edgePath.add(edge);
                final double labelLength = labelInfo.getLength(graph.road(node));
                tryAppendCandidates(label, node, next);
                constructLabels(label, node, node, marked, labelLength);
                edgePath.removeIndex(edgePath.size() - 1);
            }
        }
    }

    private void constructLabels(LabelCandidate label, int startNode, int penultimateNode, boolean[] marked,
            double maxLength) {
        final int lastNode = graph.edgeHead(label.getTailEdge());
        final IntList edgePath = label.getEdgePath();

        for (int nextEdge = graph.beginEdge(lastNode); nextEdge < graph.endEdge(lastNode); ++nextEdge) {
            final int nextNode = graph.edgeHead(nextEdge);
            if (nextNode == penultimateNode || (graph.isJunction(nextNode) && marked[nextNode])
                    || (graph.isRegular(nextNode) && graph.road(nextNode) != label.getRoadId()))
                continue;
            // do not subtract road section length if it forms a cyclic label
            final double edgeLength = nextEdge != label.getHeadEdge() ? graph.edgeLength(nextEdge) : 0;

            edgePath.add(nextEdge);
            label.setTailEdgeReversed(isReversed(lastNode, nextNode));
            if (graph.isRoadSection(lastNode, nextNode))
                tryAppendCandidates(label, startNode, nextNode);

            if (maxLength - edgeLength >= 0) {
                marked[nextNode] = true;
                constructLabels(label, startNode, lastNode, marked, maxLength - edgeLength);
                marked[nextNode] = false;
            }

            edgePath.removeIndex(edgePath.size() - 1);

        }
    }

    private void tryAppendCandidates(final LabelCandidate candidate, final int startNode, final int endNode) {
        if (startNode > endNode)
            return;

        final List<DoubleInterval> intervals = intervalFinder.findIntervals(candidate.getEdgePath(),
                candidate.getRoadId(), candidate.isHeadEdgeReversed());
        for (final DoubleInterval interval : intervals) {
            candidate.setHeadInterval(interval);
            candidates.add(new LabelCandidate(candidate));
        }
    }

    private void init(RoadMap roadMap, final QualityMeasure measure) {
        this.graph = roadMap.getGraph();
        this.labelInfo = roadMap.getLabelInfo();
        this.intervalFinder = new HeadIntervalFinder(roadMap, measure);
        this.candidates = new ArrayList<>();
    }

    private boolean isReversed(final int start, final int end) {
        return start > end;
    }
}
