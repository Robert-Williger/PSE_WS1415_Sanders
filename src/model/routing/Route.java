package model.routing;

public class Route {

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
}
