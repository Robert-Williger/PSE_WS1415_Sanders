package adminTool.labeling.algorithm.postprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import adminTool.labeling.LabelPath;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.algorithm.HeadIntervalFinder;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import util.DoubleInterval;
import util.IntList;

public class ChainCreator {
    private final RoadGraph graph;
    private final HashMap<Integer, Set<LabelPath>> sectionToLabel;

    private List<ShiftableLabel> labelChain;
    private HeadIntervalFinder intervalFinder;
    private LabelReverser reverser;

    public ChainCreator(RoadMap roadMap, QualityMeasure measure, HashMap<Integer, Set<LabelPath>> sectionToLabel) {
        this.sectionToLabel = sectionToLabel;
        // labelCreator = new ShiftableLabelCreator(roadMap, measure);
        intervalFinder = new HeadIntervalFinder(roadMap, measure);
        graph = roadMap.getGraph();
        reverser = new LabelReverser(graph);
    }

    public List<ShiftableLabel> createChain(int section, LabelPath label) {
        labelChain = new ArrayList<ShiftableLabel>();
        labelChain.add(null);// reserve slot for left dummy label
        expandChain(label, section);
        labelChain.set(0, createDummy(labelChain.get(1).getHeadEdge()));
        return labelChain;
    }

    private void expandChain(final LabelPath label, final int section) {
        final int edge = currentEdge(label, section);
        if (label.getEdgePath().get(0) != edge)
            reverser.reverse(label);

        labelChain.add(createShiftableLabel(label));

        final int nextSection = nextSection(label, section);

        final Set<LabelPath> labels = sectionToLabel.get(nextSection);
        labels.remove(label);

        if (!labels.isEmpty()) {
            final LabelPath nextLabel = labels.iterator().next();
            expandChain(nextLabel, nextSection);
            labels.clear();
        }
    }

    private ShiftableLabel createShiftableLabel(final LabelPath label) {
        final List<DoubleInterval> intervals = intervalFinder.findIntervals(label.getEdgePath(), label.getRoadId());
        DoubleInterval interval = new DoubleInterval(label.getHeadPosition(), label.getHeadPosition());
        for (final DoubleInterval i : intervals) {
            if (label.getHeadPosition() < i.getEnd() + 1E-10 && label.getHeadPosition() > i.getStart() - 1E-10) {
                interval = i;
                break;
            }
        }

        return new ShiftableLabel(label, interval.getStart(), interval.getEnd());
    }

    private ShiftableLabel createDummy(final int edge) {
        final IntList dummyPath = new IntList(1);
        dummyPath.add(edge);
        return new ShiftableLabel(new LabelPath(dummyPath, 0, 0, 0), 0, 0);
    }

    private int currentEdge(final LabelPath label, final int currentSection) {
        final int e1 = label.getHeadEdge();
        final int e2 = label.getTailEdge();

        return currentSection == graph.sectionId(e1) ? e1 : e2;
    }

    private int nextSection(final LabelPath label, final int currentSection) {
        final int s1 = graph.sectionId(label.getHeadEdge());
        final int s2 = graph.sectionId(label.getTailEdge());

        return currentSection == s1 ? s2 : s1;
    }

}
