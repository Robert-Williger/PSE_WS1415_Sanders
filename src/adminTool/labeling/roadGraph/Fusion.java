package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.IntList;

public class Fusion {
    private HashMap<Integer, IntList> map;
    private List<Road> processedRoads;

    public List<Road> getRoads() {
        return processedRoads;
    }

    public void fuse(final List<Road> roads) {
        processedRoads = new ArrayList<>();
        createMap(roads);
        final boolean[] marks = new boolean[roads.size()];

        for (int i = 0; i < roads.size(); ++i) {
            final Road road = roads.get(i);
            if (marks[i])
                continue;

            final IntList first = road.toList();
            for (int direction = 0; direction < 2; ++direction) {
                int next = next(roads, i, first.get(first.size() - 1));
                while (next != -1 && !isCycle(first)) {
                    marks[next] = true;
                    fuse(first, roads.get(next));
                    next = next(roads, next, first.get(first.size() - 1));
                }
                first.reverse();
            }

            processedRoads.add(new Road(first, road.getType(), road.getRoadId()));
        }
    }

    private void createMap(final List<Road> roads) {
        map = new HashMap<>();

        int wayCount = 0;
        for (final Road road : roads) {
            appendMap(wayCount, road.getNode(0));
            appendMap(wayCount, road.getNode(road.size() - 1));
            ++wayCount;
        }
    }

    private void appendMap(final int way, final int node) {
        IntList ways = map.get(node);
        if (ways == null) {
            ways = new IntList();
            map.put(node, ways);
        }
        ways.add(way);
    }

    private boolean isCycle(final IntList list) {
        return list.get(0) == list.get(list.size() - 1);
    }

    private void fuse(final IntList first, final Road second) {
        if (second.getNode(0) == first.get(first.size() - 1))
            for (int i = 1; i < second.size(); ++i)
                first.add(second.getNode(i));
        else
            for (int i = second.size() - 2; i >= 0; --i)
                first.add(second.getNode(i));
    }

    private int next(final List<Road> roads, final int way, final int junction) {
        final IntList neighbors = map.get(junction);
        if (neighbors.size() != 2)
            return -1;

        final Road first = roads.get(neighbors.get(0));
        final Road second = roads.get(neighbors.get(1));
        if (first.getRoadId() != second.getRoadId())
            return -1;

        return neighbors.get(0) != way ? neighbors.get(0) : neighbors.get(1);
    }
}