package adminTool.labeling.roadGraph.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadType;
import util.IntList;

public class JunctionFilter {
    private List<Road> originalRoads;
    private List<Road> filteredRoads;

    private boolean[] active;
    private HashMap<Integer, IntList> map;

    public JunctionFilter() {}

    public List<Road> getRoads() {
        return filteredRoads;
    }

    public void filter(final List<Road> roads) {
        this.originalRoads = roads;
        this.filteredRoads = new ArrayList<>();
        this.active = new boolean[roads.size()];
        Arrays.fill(active, true);

        createRoadMap();
        filterJunctionEdges();
        createFilteredList();
    }

    private void createRoadMap() {
        map = new HashMap<>();
        int roadIdx = 0;
        for (final Road road : originalRoads) {
            if (road.getRoadType() == RoadType.Junction || road.getRoadType() == RoadType.Road) {
                appendOccurance(road.getNode(0), roadIdx);
                appendOccurance(road.getNode(road.size() - 1), roadIdx);
            }
            ++roadIdx;
        }
    }

    private void appendOccurance(final int node, final int id) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(id);
    }

    private void filterJunctionEdges() {
        int roadIdx = 0;
        for (final Road road : originalRoads) {
            if (road.getRoadType() == RoadType.Junction && isActive(roadIdx))
                filterJunctionEdges(road, roadIdx);
            ++roadIdx;
        }
    }

    private void filterJunctionEdges(final Road road, final int roadIdx) {
        updateMultipleJunctions(road.getRoadId(), roadIdx, road.getNode(0), road.getNode(road.size() - 1));
        updateMultipleJunctions(road.getRoadId(), roadIdx, road.getNode(road.size() - 1), road.getNode(0));
    }

    private void updateMultipleJunctions(final int roadId, int roadIdx, int first, int second) {
        if (isConnected(roadId, roadIdx, first))
            return;

        setActive(roadIdx, false);
        while ((roadIdx = nextUnconnectedJunctionSegment(roadId, second)) != -1) {
            setActive(roadIdx, false);
            final Road road = getRoad(roadIdx);
            second = road.getNode(0) == second ? road.getNode(road.size() - 1) : road.getNode(0);
        }
    }

    private boolean isConnected(final int roadId, int roadIdx, int endNode) {
        final IntList neighbors = getNeighbors(endNode);
        for (int i = 0; i < neighbors.size(); ++i) {
            final int idx = neighbors.get(i);
            final Road other = getRoad(idx);
            if (idx != roadIdx && other.getRoadId() == roadId
                    && (other.getRoadType() == RoadType.Road || isActive(idx)))
                return true;
        }
        return false;
    }

    private int nextUnconnectedJunctionSegment(final int roadId, final int second) {
        int count = 0;
        int next = -1;
        final IntList secondNeighbors = getNeighbors(second);
        for (int i = 0; i < secondNeighbors.size(); ++i) {
            final int id = secondNeighbors.get(i);

            final Road other = getRoad(id);
            if (other.getRoadType() == RoadType.Road)
                return -1;
            if (other.getRoadId() == roadId && isActive(id)) {
                ++count;
                next = id;
            }
        }

        return count == 1 ? next : -1;
    }

    private void createFilteredList() {
        int roadIdx = 0;
        for (final Road road : originalRoads) {
            if (!isActive(roadIdx))
                road.setRoadType(RoadType.UnconnectedJunction);
            filteredRoads.add(road);
            ++roadIdx;
        }
    }

    private Road getRoad(final int id) {
        return originalRoads.get(id);
    }

    private IntList getNeighbors(final int node) {
        return map.get(node);
    }

    private void setActive(final int roadIdx, final boolean active) {
        this.active[roadIdx] = active;
    }

    private boolean isActive(int roadIdx) {
        return active[roadIdx];
    }
}
