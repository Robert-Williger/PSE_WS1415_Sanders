package model.routing;

import java.util.Iterator;

import util.Arrays;

public class Route implements Iterable<Path> {

    private final int[] targetIndices;
    private final Path[] paths;

    public Route(final Path[] paths, final int[] targetIndices) {
        this.targetIndices = targetIndices;
        this.paths = paths;
    }

    public Path[] getPaths() {
        return paths;
    }

    public int getTargetIndex(final int index) {
        return targetIndices[index];
    }

    @Override
    public Iterator<Path> iterator() {
        return Arrays.iterator(getPaths());
    }
}
