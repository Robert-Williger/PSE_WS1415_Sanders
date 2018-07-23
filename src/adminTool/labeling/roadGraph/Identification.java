package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.IIntPointAccess;
import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import util.IntList;

public class Identification {
    private List<Road> roads;
    private PointAccess.OfDouble points;

    public void identify(final Collection<Way> ways, final IIntPointAccess origPoints) {
        HashMap<String, List<Way>> wayMap = new HashMap<>();

        for (final Way way : ways) {
            if (way.getName() == null || way.getName().isEmpty())
                continue;
            List<Way> equalWays = wayMap.get(way.getType() + way.getName());
            if (equalWays == null) {
                equalWays = new ArrayList<Way>();
                wayMap.put(way.getType() + way.getName(), equalWays);
            }
            equalWays.add(way);
        }

        points = new PointAccess.OfDouble(origPoints.size());
        roads = new ArrayList<>(ways.size());
        for (int i = 0; i < origPoints.size(); ++i) {
            points.addPoint(origPoints.getX(i), origPoints.getY(i));
        }

        PathJoiner joiner = new PathJoiner();
        int roadId = 0;
        for (final List<Way> w : wayMap.values()) {
            final Way way = w.get(0);
            for (final IntList indices : joiner.join(w)) {
                roads.add(new Road(indices, way.getType(), way.getName(), roadId));
            }
            ++roadId;
        }
    }

    public PointAccess.OfDouble size() {
        return points;
    }

    public List<Road> getRoads() {
        return roads;
    }

    private class PathJoiner {
        private HashMap<Integer, IntList> map;
        private List<IntList> paths;
        private List<IntList> processedPaths;
        private boolean[] marks;

        public List<IntList> join(final List<Way> equalWays) {
            processedPaths = new ArrayList<>();

            map = new HashMap<>();
            paths = new ArrayList<>();
            marks = new boolean[equalWays.size()];

            int wayCount = 0;
            for (final Way way : equalWays) {
                final IntList list = appendPath(way);
                if (!isCycle(list)) {
                    appendMap(wayCount, way.getNode(0));
                    appendMap(wayCount, way.getNode(way.size() - 1));
                } else {
                    processedPaths.add(paths.get(wayCount));
                    marks[wayCount] = true;
                }
                ++wayCount;
            }

            for (int i = 0; i < equalWays.size(); ++i) {
                while (join(i)) {}
                paths.get(i).reverse();
                while (join(i)) {}
            }
            for (int i = 0; i < equalWays.size(); ++i) {
                if (!marks[i])
                    processedPaths.add(paths.get(i));
            }

            return processedPaths;
        }

        private boolean isCycle(final IntList list) {
            return list.get(0) == list.get(list.size() - 1);
        }

        private IntList appendPath(final Way way) {
            final IntList list = new IntList();
            for (int i = 0; i < way.size(); ++i) {
                list.add(way.getNode(i));
            }
            paths.add(list);
            return list;
        }

        private void appendMap(final int way, final int node) {
            IntList ways = map.get(node);
            if (ways == null) {
                ways = new IntList();
                map.put(node, ways);
            }
            ways.add(way);
        }

        private boolean join(final int way) {
            final IntList path = paths.get(way);
            final int next;

            if (marks[way] || (next = next(way, path.get(path.size() - 1))) == -1)
                return false;

            append(path, next);
            marks[next] = true;

            if (!isCycle(path)) {
                final IntList nextNeighbors = map.get(path.get(path.size() - 1));
                nextNeighbors.set(nextNeighbors.indexOf(next), way);
            } else {
                processedPaths.add(path);
                marks[way] = true;
            }

            return true;
        }

        private void append(final IntList first, final int next) {
            final int junction = first.get(first.size() - 1);
            first.removeIndex(first.size() - 1);
            final IntList second = paths.get(next);
            if (second.get(0) != junction)
                second.reverse();
            first.addAll(second);
        }

        private int next(final int way, final int junction) {
            final IntList neighbors = map.get(junction);
            return neighbors.size() != 2 ? -1 : neighbors.get(0) != way ? neighbors.get(0) : neighbors.get(1);
        }
    }

}
