package adminTool.labeling.algorithm;

import java.util.ArrayList;
import java.util.List;

import adminTool.elements.MultiElement;
import adminTool.labeling.Embedding;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import adminTool.util.ElementAdapter;
import util.DoubleInterval;
import util.IntList;
import util.IntegerInterval;

public class HeadIntervalFinder {

    private final QualityMeasure measure;
    private final RoadGraph graph;
    private final ElementAdapter adapter;
    private final ILabelInfo labelInfo;
    private final Embedding embedding;

    public HeadIntervalFinder(final RoadMap roadMap, final QualityMeasure measure) {
        this.graph = roadMap.getGraph();
        this.embedding = roadMap.getEmbedding();
        this.labelInfo = roadMap.getLabelInfo();
        this.adapter = new ElementAdapter(embedding.getPoints());
        this.measure = measure;
    }

    public List<DoubleInterval> findIntervals(final IntList edgePath, final int roadId) {
        return findIntervals(edgePath, roadId, false);
    }

    public List<DoubleInterval> findIntervals(final IntList edgePath, final int roadId, final boolean isHeadReversed) {
        return edgePath.size() != 1 ? findNonSingleIntervals(edgePath, roadId, isHeadReversed)
                : findSingleIntervals(edgePath.get(0), roadId, isHeadReversed);
    }

    public List<DoubleInterval> findSingleIntervals(final int edge, final int roadId, final boolean reversed) {
        List<DoubleInterval> ret = new ArrayList<>();

        final double edgeLength = graph.edgeLength(edge);
        final double labelLength = labelInfo.getLength(roadId);
        MultiElement label = new MultiElement(embedding.getSection(edge).toList(), 0);

        adapter.setMultiElement(label);
        List<IntegerInterval> intervals = measure.getAllMaximumWellShapedIntervals(label);
        for (IntegerInterval interval : intervals) {
            final double wellShapedLength = adapter.getLength(interval.getStart(), interval.getEnd() + 1);
            if (wellShapedLength >= labelLength) {
                double min = adapter.getLength(0, interval.getStart() + 1);
                double max = min + wellShapedLength - labelLength;
                ret.add(interval(min, max, edgeLength, reversed));
            }
        }

        return ret;
    }

    public List<DoubleInterval> findNonSingleIntervals(final IntList path, final int roadId, final boolean reversed) {
        List<DoubleInterval> ret = new ArrayList<>();

        final int headEdge = path.get(0);
        final int tailEdge = path.get(path.size() - 1);
        final double labelLength = labelInfo.getLength(roadId);
        final double headLength = graph.edgeLength(headEdge);
        final double tailLength = graph.edgeLength(tailEdge);
        final double internalLength = getInternalPathLength(path);

        final IntList nodes = embedding.getSection(headEdge).toList();
        final int lastHeadSegment = nodes.size() - 2;
        nodes.removeIndex(nodes.size() - 1);
        for (int i = 1; i < path.size() - 1; ++i) {
            nodes.addAll(embedding.getSection(path.get(i)).toList());
            nodes.removeIndex(nodes.size() - 1);
        }

        final int firstTailSegment = nodes.size();
        nodes.addAll(embedding.getSection(tailEdge).toList());

        MultiElement label = new MultiElement(nodes, 0);
        List<IntegerInterval> intervals = measure.getAllMaximumWellShapedIntervals(label);
        adapter.setMultiElement(label);
        for (IntegerInterval interval : intervals) {
            if (interval.getStart() <= lastHeadSegment && interval.getEnd() > firstTailSegment
                    && adapter.getLength(interval.getStart(), interval.getEnd() + 1) >= labelLength) {
                final double freeLength = labelLength - internalLength; // rest length on head + tail segment
                final double missedHeadLength = adapter.getLength(0, interval.getStart() + 1);
                final double useableTailLength = tailLength - adapter.getLength(interval.getEnd(), adapter.size());

                double min = Math.max(missedHeadLength, headLength - freeLength);
                double max = Math.min(headLength, headLength + useableTailLength - freeLength);
                ret.add(interval(min, max, headLength, reversed));
            }
        }

        return ret;
    }

    private DoubleInterval interval(final double min, final double max, final double length, final boolean reversed) {
        return !reversed ? new DoubleInterval(min, max) : new DoubleInterval(length - max, length - min);
    }

    private double getInternalPathLength(IntList edgePath) {
        double internalLength = 0;
        for (int i = 1; i < edgePath.size() - 1; ++i)
            internalLength += graph.edgeLength(edgePath.get(i));
        return internalLength;
    }
}
