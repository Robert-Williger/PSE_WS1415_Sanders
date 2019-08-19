package adminTool.labeling.algorithm.milp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import adminTool.labeling.ILabelInfo;
import adminTool.labeling.LabelPath;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.algorithm.HeadIntervalFinder;
import adminTool.labeling.algorithm.IRoadMapLabelAlgorithm;
import adminTool.labeling.roadMap.RoadGraph;
import adminTool.labeling.roadMap.RoadMap;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import util.DoubleInterval;
import util.IntList;

public class MILPSolver implements IRoadMapLabelAlgorithm {
    private static int run = 0;

    private final LinearProgramSolver solver;
    private final QualityMeasure measure;

    private RoadGraph graph;
    private ILabelInfo labelInfo;
    private List<LabelCandidate> candidates;
    private List<LabelPath> labeling;
    private HeadIntervalFinder intervalFinder;
    private LPWizard lpw;

    public MILPSolver(final QualityMeasure measure) {
        this(measure, SolverFactory.newDefault());
    }

    public MILPSolver(final QualityMeasure measure, final LinearProgramSolver solver) {
        this.measure = measure;
        this.solver = solver;
    }

    @Override
    public void calculateLabeling(RoadMap roadMap) {
        System.out.println(run);
        ++run;
        init(roadMap);
        createCandidates();
        if (!findTrivialSolution()) {
            createProgram();
            System.out.println(candidates.size());
            retrieveSolution();
        }
    }

    @Override
    public Collection<LabelPath> getLabeling() {
        return labeling;
    }

    private void init(RoadMap roadMap) {
        this.graph = roadMap.getGraph();
        this.labelInfo = roadMap.getLabelInfo();
        this.intervalFinder = new HeadIntervalFinder(roadMap, measure);
        this.candidates = new ArrayList<>();
        this.labeling = new ArrayList<>();
        this.lpw = new LPWizard();
    }

    private void createCandidates() {
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

    private boolean findTrivialSolution() {
        if (candidates.isEmpty())
            return true;

        if (candidates.size() == 1) {
            final LabelCandidate candidate = candidates.get(0);

            final double labelLength = labelInfo.getLength(candidate.getRoadId());
            final double headStart = candidate.getHeadInterval().getStart();
            if (candidate.isSingleEdge()) {
                final double tailEnd = headStart + labelLength;
                labeling.add(new LabelPath(candidate.getEdgePath(), headStart, tailEnd, candidate.getRoadId()));
            } else {
                final double headLength = graph.edgeLength(candidate.getHeadEdge());
                final double tailLength = graph.edgeLength(candidate.getTailEdge());
                final double missingTailLength = getInternalPathLength(candidate)
                        + (candidate.isHeadEdgeReversed() ? headStart : headLength - headStart) - labelLength;
                final double tailEnd = candidate.isTailEdgeReversed() ? missingTailLength
                        : tailLength - missingTailLength;
                labeling.add(new LabelPath(candidate.getEdgePath(), headStart, tailEnd, candidate.getRoadId()));
            }

            return true;
        }

        return false;
    }

    private void createProgram() {
        boolean[] roadSection = classifySections();

        addTargetFunction(roadSection);
        for (int labelIdx = 0; labelIdx < candidates.size(); ++labelIdx) {
            final LabelCandidate label = candidates.get(labelIdx);
            final double labelLength = labelInfo.getLength(label.getRoadId());
            final double internalLength = getInternalPathLength(label);
            final double headLength = graph.edgeLength(label.getHeadEdge());
            final double tailLength = graph.edgeLength(label.getTailEdge());

            addIntervalConstraints(labelIdx, label);
            if (label.isSingleEdge())
                addLengthConstraint(labelIdx, labelLength);
            else
                addLengthConstraint(labelIdx, label, labelLength, internalLength, headLength, tailLength);
            addOverlapConstraints(labelIdx, label, headLength, tailLength);
        }
        addSectionConstraints(roadSection);

    }

    private boolean[] classifySections() {
        boolean[] roadSection = new boolean[graph.numEdges() / 2];
        for (int u = 0; u < graph.numNodes(); ++u)
            for (int edge = graph.beginEdge(u); edge < graph.endEdge(u); ++edge)
                roadSection[graph.sectionId(edge)] = graph.isRegular(u) && graph.isRegular(graph.edgeHead(edge));
        return roadSection;
    }

    private void addTargetFunction(boolean[] roadSection) {
        lpw.setMinProblem(false);
        for (int i = 0; i < roadSection.length; ++i) {
            if (roadSection[i])
                lpw.plus(sectionVar(i));
        }
    }

    private void addIntervalConstraints(int labelIdx, LabelCandidate label) {
        lpw.addConstraint("isc" + labelIdx, label.getHeadInterval().getStart(), "<=").plus(headVar(labelIdx));
        lpw.addConstraint("iec" + labelIdx, label.getHeadInterval().getEnd(), ">=").plus(headVar(labelIdx));
    }

    private void addLengthConstraint(int idx, final double labelLength) {
        lpw.addConstraint("lc" + idx, labelLength, "=").plus(tailVar(idx)).plus(headVar(idx), -1);
    }

    private void addLengthConstraint(int idx, LabelCandidate label, final double labelLength,
            final double internalLength, final double headLength, final double tailLength) {
        double leftSide = internalLength + (label.isHeadEdgeReversed() ? 0 : headLength)
                + (label.isTailEdgeReversed() ? tailLength : 0) - labelLength;
        double headSign = label.isHeadEdgeReversed() ? -1 : 1;
        double tailSign = label.isTailEdgeReversed() ? 1 : -1;
        lpw.addConstraint("lc" + idx, leftSide, "=").plus(headVar(idx), headSign).plus(tailVar(idx), tailSign);
    }

    private void addOverlapConstraints(int l1Idx, final LabelCandidate l1, final double headLength,
            final double tailLength) {
        for (int l2Idx = l1Idx + 1; l2Idx < candidates.size(); ++l2Idx) {
            final LabelCandidate l2 = candidates.get(l2Idx);
            if (overlapInternally(l1, l2))
                lpw.addConstraint("ioc" + l1Idx + "-" + l2Idx, 1, ">=").plus(labelUseVar(l1Idx))
                        .plus(labelUseVar(l2Idx));
            else {
                if (graph.sectionId(l1.getHeadEdge()) == graph.sectionId(l2.getHeadEdge()))
                    addExternalOverlapConstraint(l1, l1.isHeadEdgeReversed(), l2.isHeadEdgeReversed(), headVar(l1Idx),
                            headVar(l2Idx), l1Idx, l2Idx, headLength);
                if (graph.sectionId(l1.getHeadEdge()) == graph.sectionId(l2.getTailEdge()))
                    addExternalOverlapConstraint(l1, l1.isHeadEdgeReversed(), !l2.isTailEdgeReversed(), headVar(l1Idx),
                            tailVar(l2Idx), l1Idx, l2Idx, headLength);
                if (graph.sectionId(l1.getTailEdge()) == graph.sectionId(l2.getHeadEdge()))
                    addExternalOverlapConstraint(l1, !l1.isTailEdgeReversed(), l2.isHeadEdgeReversed(), tailVar(l1Idx),
                            headVar(l2Idx), l1Idx, l2Idx, tailLength);
                if (graph.sectionId(l1.getTailEdge()) == graph.sectionId(l2.getTailEdge()))
                    addExternalOverlapConstraint(l1, !l1.isTailEdgeReversed(), !l2.isTailEdgeReversed(), tailVar(l1Idx),
                            tailVar(l2Idx), l1Idx, l2Idx, tailLength);
            }
        }
    }

    private void addExternalOverlapConstraint(LabelCandidate l1, boolean l1reversed, boolean l2reversed, String ht1,
            String ht2, int l1Idx, int l2Idx, final double m) {
        final String name = "eoc" + ht1 + "-" + ht2;
        final int l1Sign = l1Sign(l1, l1reversed, l2reversed);

        lpw.addConstraint(name, 2 * m, ">=").plus(ht1, l1Sign).plus(ht2, -l1Sign).plus(labelUseVar(l1Idx), m)
                .plus(labelUseVar(l2Idx), m);
    }

    private int l1Sign(final LabelCandidate l1, boolean l1reversed, boolean l2reversed) {
        return !l1.isSingleEdge() ? (l1reversed ? 1 : -1) : (l2reversed ? -1 : 1);
    }

    private void addSectionConstraints(boolean[] roadSection) {
        List<Set<Integer>> labelsPerRoadSection = new ArrayList<Set<Integer>>(roadSection.length);
        for (int i = 0; i < roadSection.length; ++i)
            labelsPerRoadSection.add(new HashSet<Integer>());

        for (int i = 0; i < candidates.size(); ++i) {
            final LabelCandidate label = candidates.get(i);
            final int labelIdx = i;

            label.forEachEdge(e -> addSection(roadSection, labelsPerRoadSection, labelIdx, e));
        }

        for (int section = 0; section < roadSection.length; ++section) {
            LPWizardConstraint c = lpw.addConstraint("rsc" + section, 0, "<=").plus(sectionVar(section), -1);
            final Set<Integer> labels = labelsPerRoadSection.get(section);
            labels.forEach(labelIdx -> c.plus(labelUseVar(labelIdx)));
            c.setAllVariablesBoolean();
        }
    }

    private void addSection(boolean[] roadSection, List<Set<Integer>> labelsPerRoadSection, int labelIdx,
            final int edge) {
        final int section = graph.sectionId(edge);
        if (roadSection[section])
            labelsPerRoadSection.get(section).add(labelIdx);
    }

    private String headVar(int labelIdx) {
        return "h" + labelIdx;
    }

    private String tailVar(int labelIdx) {
        return "t" + labelIdx;
    }

    private String labelUseVar(int labelIdx) {
        return "x" + labelIdx;
    }

    private String sectionVar(int section) {
        return "y" + section;
    }

    private double getInternalPathLength(LabelCandidate label) {
        double internalLength = 0;
        for (int i = 1; i < label.getEdgePath().size() - 1; ++i)
            internalLength += graph.edgeLength(label.getEdge(i));
        return internalLength;
    }

    private boolean isReversed(final int start, final int end) {
        return start > end;
    }

    private boolean overlapInternally(final LabelCandidate l1, final LabelCandidate l2) {
        HashSet<Integer> nodes = new HashSet<Integer>();
        l1.forEachInternal(e -> nodes.add(graph.edgeHead(e)));
        for (int i = 1; i < l2.getEdgePath().size() - 1; ++i)
            if (nodes.contains(graph.edgeHead(l2.getEdge(i))))
                return true;

        return false;
    }

    private void retrieveSolution() {
        final LPSolution lpSolution = lpw.solve(solver);
        int usedLabels = 0;
        for (int labelIdx = 0; labelIdx < candidates.size(); ++labelIdx) {
            usedLabels += lpSolution.getBoolean(labelUseVar(labelIdx)) ? 1 : 0;
        }
        System.out.println(lpSolution.getObjectiveValue() + ", " + candidates.size() + ", " + usedLabels);

        for (int labelIdx = 0; labelIdx < candidates.size(); ++labelIdx) {
            if (!lpSolution.getBoolean(labelUseVar(labelIdx)))
                continue;
            final LabelCandidate label = candidates.get(labelIdx);

            double headStart = lpSolution.getDouble(headVar(labelIdx));
            if (label.isHeadEdgeReversed())
                headStart = graph.edgeLength(label.getHeadEdge()) - headStart;
            double tailEnd = lpSolution.getDouble(tailVar(labelIdx));
            if (label.isTailEdgeReversed())
                tailEnd = graph.edgeLength(label.getTailEdge()) - tailEnd;

            labeling.add(new LabelPath(label.getEdgePath(), headStart, tailEnd, label.getRoadId()));
        }
    }
}