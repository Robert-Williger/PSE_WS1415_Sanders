package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.IntList;

public class Fusion {
    private HashMap<Integer, IntList> map;
    private List<LabelSection> processedRoads;

    public List<LabelSection> getRoads() {
        return processedRoads;
    }

    public void fuse(final List<LabelSection> roads) {
        processedRoads = new ArrayList<>();
        createMap(roads);
        final boolean[] marks = new boolean[roads.size()];

        for (int i = 0; i < roads.size(); ++i) {
            final LabelSection road = roads.get(i);
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

            processedRoads.add(new LabelSection(first, road.getType(), road.getRoadId()));
        }
    }

    private void createMap(final List<LabelSection> roads) {
        map = new HashMap<>();

        int wayCount = 0;
        for (final LabelSection road : roads) {
            appendMap(wayCount, road.getPoint(0));
            appendMap(wayCount, road.getPoint(road.size() - 1));
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

    private void fuse(final IntList first, final LabelSection second) {
        if (second.getPoint(0) == first.get(first.size() - 1))
            for (int i = 1; i < second.size(); ++i)
                first.add(second.getPoint(i));
        else
            for (int i = second.size() - 2; i >= 0; --i)
                first.add(second.getPoint(i));
    }

    private int next(final List<LabelSection> roads, final int way, final int junction) {
        final IntList neighbors = map.get(junction);
        if (neighbors.size() != 2)
            return -1;

        final LabelSection first = roads.get(neighbors.get(0));
        final LabelSection second = roads.get(neighbors.get(1));
        if (first.getRoadId() != second.getRoadId())
            return -1;

        return neighbors.get(0) != way ? neighbors.get(0) : neighbors.get(1);
    }
}