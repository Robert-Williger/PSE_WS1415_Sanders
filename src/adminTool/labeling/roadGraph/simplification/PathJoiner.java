package adminTool.labeling.roadGraph.simplification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import util.IntList;

public class PathJoiner {
    private HashMap<Integer, IntList> map;
    private List<Way> processedPaths;
    private boolean[] marks;

    public List<Way> getProcessedPaths() {
        return processedPaths;
    }

    public void join(final List<Way> equalWays) {
        map = new HashMap<>();
        processedPaths = new ArrayList<Way>();

        int wayCount = 0;
        for (final Way way : equalWays) {
            appendMap(wayCount, way.getNode(0));
            appendMap(wayCount, way.getNode(way.size() - 1));
            ++wayCount;
        }

        marks = new boolean[equalWays.size()];
        for (int i = 0; i < equalWays.size(); ++i) {
            final Way way = equalWays.get(i);
            tryJoin(equalWays, way.getName(), i, way);
            tryJoin(equalWays, way.getName(), i, way.reverse());
        }
        for (int i = 0; i < equalWays.size(); ++i) {
            if (!marks[i]) {
                processedPaths.add(equalWays.get(i));
            }
        }
    }

    private void tryJoin(final List<Way> equalWays, final String name, int wayIndex, MultiElement way) {
        if (!marks[wayIndex] && !hasNext(map.get(way.getNode(0)), wayIndex)) {
            final IntList join = new IntList();
            for (int j = 0; j < way.size(); ++j) {
                join.add(way.getNode(j));
            }
            marks[wayIndex] = true;
            int junction = way.getNode(way.size() - 1);
            IntList ways = map.get(junction);

            while (hasNext(ways, wayIndex) && !marks[wayIndex = getNext(ways, wayIndex)]) {
                way = equalWays.get(wayIndex);
                way = way.getNode(0) == junction ? way : way.reverse();
                junction = way.getNode(way.size() - 1);
                ways = map.get(junction);
                marks[wayIndex] = true;
                for (int j = 1; j < way.size(); ++j) {
                    join.add(way.getNode(j));
                }
            }

            processedPaths.add(new Way(join.toArray(), way.getType(), name, false));
        }
    }

    private int getNext(final IntList ways, final int way) {
        return ways.get(0) != way ? ways.get(0) : ways.get(1);
    }

    private boolean hasNext(final IntList ways, final int way) {
        return ways.size() == 2 && ways.get(0) != ways.get(1);

    }

    private void appendMap(final int way, final int node) {
        IntList ways = map.get(node);
        if (ways == null) {
            ways = new IntList();
            map.put(node, ways);
        }
        ways.add(way);
    }
}
