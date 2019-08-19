package adminTool.labeling.algorithm.postprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import adminTool.labeling.LabelPath;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.algorithm.HeadIntervalFinder;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import util.DoubleInterval;
import util.IntList;

public class LabelShifter {

    private HashMap<Integer, Set<LabelPath>> sectionToLabel;
    private RoadGraph graph;
    private ChainCreator chainCreator;

    private Collection<LabelPath> labeling;
    private HeadIntervalFinder intervalFinder;

    public Collection<LabelPath> getLabeling() {
        return labeling;
    }

    public void postprocess(RoadMap roadMap, Collection<LabelPath> labeling, QualityMeasure measure) {
        this.labeling = new ArrayList<LabelPath>(labeling.size());
        intervalFinder = new HeadIntervalFinder(roadMap, measure);
        graph = roadMap.getGraph();
        sectionToLabel = new HashMap<>();
        chainCreator = new ChainCreator(roadMap, measure, sectionToLabel);

        for (final LabelPath label : labeling) {
            fillMap(graph, label, 0);
            fillMap(graph, label, label.getEdgePath().size() - 1);
        }

        for (final LabelPath label : labeling) {
            final int section = graph.sectionId(label.getEdgePath().get(0));
            if (label.getEdgePath().size() == 1) {
                final Set<LabelPath> labels = sectionToLabel.get(section);
                if (labels.size() > 1)
                    labels.remove(label); // needless label
                else {
                    final List<DoubleInterval> intervals = intervalFinder.findIntervals(label.getEdgePath(),
                            label.getRoadId());
                    DoubleInterval interval = new DoubleInterval(label.getHeadPosition(), label.getHeadPosition());
                    for (final DoubleInterval i : intervals) {
                        if (label.getHeadPosition() < i.getEnd() + 1E-10
                                && label.getHeadPosition() > i.getStart() - 1E-10) {
                            interval = i;
                            break;
                        }
                    }
                    final double headMidLength = (interval.getEnd() - interval.getStart()) / 2;
                    final double distance = headMidLength - label.getHeadPosition();
                    label.setHeadPosition(headMidLength);
                    label.setTailPosition(label.getTailPosition() + distance);
                    this.labeling.add(label);
                }
            }
        }

        for (final Map.Entry<Integer, Set<LabelPath>> entry : sectionToLabel.entrySet()) {
            Set<LabelPath> labels = entry.getValue();
            LabelPath label;
            if (labels.size() != 1 || (label = labels.iterator().next()).getEdgePath().size() == 1)
                continue;

            List<ShiftableLabel> chain = chainCreator.createChain(entry.getKey(), label);
            shiftLabelsLeft(chain);
            shiftLabelsRight(chain);

            for (final Iterator<ShiftableLabel> it = chain.listIterator(1); it.hasNext();)
                this.labeling.add(it.next());
        }
    }

    private void shiftLabelsRight(List<ShiftableLabel> chain) {
        final ShiftableLabel last = chain.get(chain.size() - 1);
        IntList lastEdgePath = last.getEdgePath();
        int lastEdge = lastEdgePath.get(lastEdgePath.size() - 1);
        double lastEdgeLength = graph.edgeLength(lastEdge);

        PriorityQueue<LabelDistance> queue = new PriorityQueue<>();
        for (int i = 0; i < chain.size() - 1; ++i)
            queue.add(new LabelDistance(i, chain.get(i + 1).getHeadPosition() - chain.get(i).getTailPosition()));
        queue.add(new LabelDistance(-1, Double.MAX_VALUE));

        TreeSet<Integer> changingDistances = new TreeSet<Integer>();

        while (queue.peek().distance < lastEdgeLength - last.getTailPosition()) {
            final LabelDistance ld = queue.poll();

            changingDistances.add(ld.index);

            while (true) {
                double shiftDistance = getShiftDistance(chain, changingDistances);
                if (shiftDistance <= 1E-10) {
                    changingDistances.pollFirst();
                    if (changingDistances.isEmpty())
                        break;
                } else {
                    final double maxShiftDistance = Math.min(queue.peek().distance - ld.distance,
                            (lastEdgeLength - last.getTailPosition() - ld.distance) / (1 + changingDistances.size()));
                    shift(chain, changingDistances, Math.min(shiftDistance, maxShiftDistance));
                    break;
                }
            }
        }
    }

    private void shiftLabelsLeft(final List<ShiftableLabel> chain) {
        for (int i = 1; i < chain.size(); ++i) {
            final ShiftableLabel label = chain.get(i);
            final double distance = Math.max(chain.get(i - 1).getTailPosition(), label.getHeadMin())
                    - label.getHeadPosition();
            label.shift(distance);

        }
    }

    private double getShiftDistance(final List<ShiftableLabel> chain, final TreeSet<Integer> changingDistances) {
        double minDistance = Double.MAX_VALUE;
        int risingDistances = 0;
        for (int i = changingDistances.first(); i < chain.size() - 1; ++i) {
            if (changingDistances.contains(i))
                ++risingDistances;
            minDistance = Math.min(minDistance, chain.get(i + 1).maxRightShiftableDistance() / risingDistances);
        }

        return minDistance;
    }

    private void shift(final List<ShiftableLabel> chain, final TreeSet<Integer> changingDistances,
            final double distance) {
        double currentDistance = changingDistances.size() * distance;
        for (int i = chain.size() - 1; i > changingDistances.first(); --i) {
            chain.get(i).shift(currentDistance);
            currentDistance -= changingDistances.contains(i - 1) ? distance : 0;
        }
    }

    private void fillMap(final RoadGraph graph, final LabelPath label, final int edgeIdx) {
        final int edge = label.getEdgePath().get(edgeIdx);
        final int section = graph.sectionId(edge);

        Set<LabelPath> list = sectionToLabel.get(section);
        if (list == null) {
            list = new HashSet<>();
            sectionToLabel.put(section, list);
        }
        list.add(label);
    }

    private static class LabelDistance implements Comparable<LabelDistance> {
        private final int index;
        private final double distance;

        public LabelDistance(int index, double distance) {
            super();
            this.index = index;
            this.distance = distance;
        }

        @Override
        public int compareTo(LabelDistance o) {
            return Double.compare(distance, o.distance);
        }

    }
}
