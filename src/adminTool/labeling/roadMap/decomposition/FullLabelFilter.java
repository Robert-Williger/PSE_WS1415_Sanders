package adminTool.labeling.roadMap.decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.algorithm.HeadIntervalFinder;
import adminTool.labeling.algorithm.milp.LabelCandidate;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import adminTool.util.ElementAdapter;
import util.DoubleInterval;
import util.IntList;

public class FullLabelFilter extends AbstractFilter {

    private HashMap<Integer, IntList> sectionMap;
    private double[] lengths;

    private RoadGraph graph;
    private ILabelInfo labelInfo;
    private List<LabelCandidate> candidates;
    private HeadIntervalFinder intervalFinder;
    private final QualityMeasure measure;
    private final IPointAccess points;

    public FullLabelFilter(QualityMeasure measure, IPointAccess points) {
        super();
        this.measure = measure;
        this.points = points;
    }

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        sectionMap = createSectionMap(roadSections);
        lengths = new double[roadSections.size()];
        ElementAdapter adapter = new ElementAdapter(points);
        for (int i = 0; i < roadSections.size(); ++i) {
            adapter.setMultiElement(roadSections.get(i));
            lengths[i] = adapter.getLength();
        }
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

    private int getEnd(final LabelSection road, final int start) {
        return road.getPoint(0) == start ? road.getPoint(road.size() - 1) : road.getPoint(0);
    }

    private HashMap<Integer, IntList> createSectionMap(final List<LabelSection> roads) {
        final HashMap<Integer, IntList> map = new HashMap<>();
        int id = 0;
        for (final LabelSection road : roads) {
            appendOccurance(road.getPoint(0), id, map);
            appendOccurance(road.getPoint(road.size() - 1), id, map);
            ++id;
        }
        return map;
    }

    private void appendOccurance(final int node, final int roadId, final HashMap<Integer, IntList> map) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(roadId);
    }
}
