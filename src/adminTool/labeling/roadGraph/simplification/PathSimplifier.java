package adminTool.labeling.roadGraph.simplification;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.VisvalingamWhyatt;
import util.IntList;

public class PathSimplifier {
    private final VisvalingamWhyatt simplifier;
    private UnboundedPointAccess simplifiedPoints;
    private List<IntList> simplifiedPaths;
    private int[] map;

    public PathSimplifier(final int threshold) {
        simplifier = new VisvalingamWhyatt(threshold);
    }

    public IPointAccess getPoints() {
        return simplifiedPoints;
    }

    public List<IntList> getPaths() {
        return simplifiedPaths;
    }

    public void simplify(final List<IntList> paths, final IPointAccess points) {
        createMap(points);
        simplifiedPoints = new UnboundedPointAccess();
        simplifiedPaths = new ArrayList<>(paths.size());

        for (final IntList path : paths) {
            final IntList simplifiedPath = simplifier.simplifyMultiline(points, path.iterator());
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
        map = new int[points.getPoints()];
        for (int i = 0; i < map.length; ++i) {
            map[i] = -1;
        }
    }

    private void addShared(final int index, final IntList mappedPath, final IPointAccess points) {
        if (map[index] == -1) {
            map[index] = simplifiedPoints.getPoints();
            simplifiedPoints.addPoint(points.getX(index), points.getY(index));
        }
        mappedPath.add(map[index]);
    }

    private void addUnshared(final int index, final IntList mappedPath, final IPointAccess points) {
        mappedPath.add(simplifiedPoints.getPoints());
        simplifiedPoints.addPoint(points.getX(index), points.getY(index));
    }

}
