package adminTool.labeling.roadMap.simplification;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.VisvalingamWhyatt;
import adminTool.elements.IPointAccess;
import adminTool.elements.PointAccess;
import util.IntList;

public class PathSimplifier {
    private final VisvalingamWhyatt simplifier;
    private PointAccess simplifiedPoints;
    private List<IntList> simplifiedPaths;
    private int[] map;

    public PathSimplifier(final int threshold) {
        simplifier = new VisvalingamWhyatt(threshold);
    }

    public PointAccess getPoints() {
        return simplifiedPoints;
    }

    public List<IntList> getPaths() {
        return simplifiedPaths;
    }

    public void simplify(final List<IntList> paths, final PointAccess points) {
        createMap(points);
        simplifiedPoints = new PointAccess();
        simplifiedPaths = new ArrayList<>(paths.size());

        for (final IntList path : paths) {
            final IntList simplifiedPath = simplifier.simplifyMultiline(points, path.iterator(), path.size());
            final IntList mappedPath = new IntList(simplifiedPath.size());
            final PrimitiveIterator.OfInt iterator = simplifiedPath.iterator();
            addShared(iterator.nextInt(), mappedPath, points);
            for (int i = 2; i < simplifiedPath.size(); ++i) {
                addUnshared(iterator.nextInt(), mappedPath, points);
            }
            addShared(iterator.nextInt(), mappedPath, points);
            simplifiedPaths.add(mappedPath);
        }
    }

    private void createMap(final IPointAccess points) {
        map = new int[points.size()];
        for (int i = 0; i < map.length; ++i) {
            map[i] = -1;
        }
    }

    private void addShared(final int index, final IntList mappedPath, final PointAccess points) {
        if (map[index] == -1) {
            map[index] = simplifiedPoints.size();
            simplifiedPoints.addPoint(points.getX(index), points.getY(index));
        }
        mappedPath.add(map[index]);
    }

    private void addUnshared(final int index, final IntList mappedPath, final PointAccess points) {
        mappedPath.add(simplifiedPoints.size());
        simplifiedPoints.addPoint(points.getX(index), points.getY(index));
    }

}
