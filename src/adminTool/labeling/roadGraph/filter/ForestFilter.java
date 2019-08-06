package adminTool.labeling.roadGraph.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadType;
import util.UnionFind;

public class ForestFilter {
    private final int[] map;
    private UnionFind unionFind;
    private List<Road> filteredRoads;

    public ForestFilter(final int points) {
        this.map = new int[points];
    }

    public void filter(final List<Road> roads) {
        final int nodes = initMap(roads);
        unionFind = new UnionFind(nodes);
        filteredRoads = new ArrayList<>(roads.size());

        addRoadSections(roads);
        addJunctions(roads);
        addRest(roads);
        int[] counts = new int[nodes];
        for (int i = 0; i < counts.length; ++i) {
            ++counts[unionFind.find(i)];
        }
        Arrays.sort(counts);
        System.out.println(Arrays.toString(Arrays.copyOfRange(counts, counts.length - 100, counts.length)));
    }

    public List<Road> getRoads() {
        return filteredRoads;
    }

    private void addRoadSections(final List<Road> roads) {
        for (final Road road : roads) {
            if (road.getRoadType() == RoadType.Road) {
                unionFind.link(map[road.getNode(0)], map[road.getNode(road.size() - 1)]);
                filteredRoads.add(road);
            }
        }
    }

    private void addJunctions(final List<Road> roads) {
        for (final Road road : roads) {
            if (road.getRoadType() == RoadType.Junction
                    && unionFind.union(map[road.getNode(0)], map[road.getNode(road.size() - 1)]))
                filteredRoads.add(road);
        }
    }

    private void addRest(List<Road> roads) {
        for (final Road road : roads) {
            if (road.getRoadType() != RoadType.Junction && road.getRoadType() != RoadType.Road)
                filteredRoads.add(road);
        }
    }

    private int initMap(final List<Road> roads) {
        Arrays.fill(map, -1);
        int nodeId = 0;
        for (final Road road : roads) {
            if (road.getRoadType() == RoadType.Junction || road.getRoadType() == RoadType.Road) {
                nodeId = addNode(road.getNode(0), nodeId);
                nodeId = addNode(road.getNode(road.size() - 1), nodeId);
            }
        }
        return nodeId;
    }

    private int addNode(final int node, int nodeId) {
        if (map[node] == -1)
            map[node] = nodeId++;
        return nodeId;
    }
}
