package adminTool.labeling.roadGraph.simplification.hull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.Way;

public class PathJoiner {
    private List<List<Way>> processedPaths;
    private int removeCount;

    public List<List<Way>> getProcessedPaths() {
        return processedPaths;
    }

    public void join(final List<Way> equalWays) {
        final HashMap<Integer, Path> map = new HashMap<Integer, Path>();

        removeCount = 0;
        processedPaths = new ArrayList<List<Way>>();
        for (final Way way : equalWays) {
            append(way, way.getNode(0), way.getNode(way.size() - 1), map);
        }

        final int size = processedPaths.size() - removeCount;
        int last = processedPaths.size() - 1;
        for (int i = 0; i < size; ++i) {
            if (processedPaths.get(i) == null) {
                while (processedPaths.get(last) == null && last > i) {
                    --last;
                }
                processedPaths.set(i, processedPaths.get(i));
            }
        }
    }

    private void append(final Way way, final int first, final int second, final HashMap<Integer, Path> map) {
        final Path firstPath = map.get(first);
        final Path secondPath = map.get(second);
        final List<Way> joinedPaths;
        if (firstPath != null) {
            joinedPaths = processedPaths.get(firstPath.pathIdx);
            if (secondPath != null) {
                if (secondPath != firstPath) {
                    joinedPaths.addAll(processedPaths.get(secondPath.pathIdx));
                    ++removeCount;
                    secondPath.pathIdx = firstPath.pathIdx;
                }
                map.put(second, firstPath);
            }
        } else if (secondPath != null) {
            joinedPaths = processedPaths.get(secondPath.pathIdx);
            map.put(first, secondPath);
        } else {
            joinedPaths = new ArrayList<>();
            processedPaths.add(joinedPaths);
            final Path path = new Path(processedPaths.size());

            map.put(first, path);
            map.put(second, path);
        }

        joinedPaths.add(way);
    }

    private static class Path {
        int pathIdx;

        public Path(final int pathIdx) {
            this.pathIdx = pathIdx;
        }
    }

}
