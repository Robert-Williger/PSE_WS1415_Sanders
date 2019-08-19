package adminTool.labeling.roadMap.decomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import adminTool.labeling.roadMap.LabelSection;
import util.IntList;

public class JunctionConnectionFilter extends AbstractFilter {
    private boolean[] active;
    private HashSet<Integer> roadSet;
    private HashMap<Integer, IntList> junctionMap;

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        this.filteredSections = new ArrayList<>(junctionSections.size());
        this.junctionSections = new ArrayList<>(junctionSections.size());
        this.roadSections = roadSections;

        active = new boolean[junctionSections.size()];
        Arrays.fill(active, true);

        roadSet = createOccuranceSet(roadSections);
        junctionMap = createOccuranceMap(junctionSections);

        filterJunctionEdges(junctionSections);
    }

    private HashSet<Integer> createOccuranceSet(final List<LabelSection> roads) {
        final HashSet<Integer> set = new HashSet<>();
        for (final LabelSection road : roads) {
            set.add(road.getPoint(0));
            set.add(road.getPoint(road.size() - 1));
        }
        return set;
    }

    private HashMap<Integer, IntList> createOccuranceMap(final List<LabelSection> roads) {
        final HashMap<Integer, IntList> map = new HashMap<>();
        int index = 0;
        for (final LabelSection road : roads) {
            appendOccurance(road.getPoint(0), index, map);
            appendOccurance(road.getPoint(road.size() - 1), index, map);
            ++index;
        }
        return map;
    }

    private void appendOccurance(final int node, final int id, final HashMap<Integer, IntList> map) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(id);
    }

    private void filterJunctionEdges(final List<LabelSection> junctionSections) {
        int roadIdx = 0;
        for (final LabelSection road : junctionSections) {
            if (isActive(roadIdx))
                filterJunctionEdges(road, roadIdx, junctionSections);
            ++roadIdx;
        }
        roadIdx = 0;
        for (final LabelSection road : junctionSections) {
            (isActive(roadIdx) ? this.junctionSections : filteredSections).add(road);
            ++roadIdx;
        }
    }

    private void filterJunctionEdges(final LabelSection road, final int roadIdx, final List<LabelSection> junctionSections) {
        updateMultipleJunctions(road.getRoadId(), roadIdx, road.getPoint(0), road.getPoint(road.size() - 1),
                junctionSections);
        updateMultipleJunctions(road.getRoadId(), roadIdx, road.getPoint(road.size() - 1), road.getPoint(0),
                junctionSections);
    }

    private void updateMultipleJunctions(final int roadId, int roadIdx, int first, int second,
            final List<LabelSection> junctionSections) {
        if (isConnected(roadId, roadIdx, first, junctionSections))
            return;

        setActive(roadIdx, false);
        while ((roadIdx = nextUnconnectedJunctionSegment(roadId, second, junctionSections)) != -1) {
            setActive(roadIdx, false);
            final LabelSection road = junctionSections.get(roadIdx);
            second = road.getPoint(0) == second ? road.getPoint(road.size() - 1) : road.getPoint(0);
        }
    }

    private boolean isConnected(final int roadId, final int roadIdx, int endNode, final List<LabelSection> junctionSections) {
        final IntList junctions = junctionMap.get(endNode);
        for (int i = 0; i < junctions.size(); ++i) {
            final int idx = junctions.get(i);
            if (idx != roadIdx && isActive(idx) && roadId == junctionSections.get(idx).getRoadId())
                return true;
        }

        return roadSet.contains(endNode);
    }

    private int nextUnconnectedJunctionSegment(final int roadId, final int second, final List<LabelSection> junctionSections) {
        int count = 0;
        int next = -1;
        final IntList secondNeighbors = junctionMap.get(second);
        for (int i = 0; i < secondNeighbors.size(); ++i) {
            final int id = secondNeighbors.get(i);
            final LabelSection other = junctionSections.get(id);

            if (other.getRoadId() == roadId && isActive(id)) {
                ++count;
                next = id;
            }
        }

        return count == 1 ? next : -1;
    }

    private void setActive(final int roadIdx, final boolean active) {
        this.active[roadIdx] = active;
    }

    private boolean isActive(int roadIdx) {
        return active[roadIdx];
    }
}
