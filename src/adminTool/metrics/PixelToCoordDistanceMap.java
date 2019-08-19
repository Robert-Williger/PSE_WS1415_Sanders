package adminTool.metrics;

public class PixelToCoordDistanceMap implements IDistanceMap {
    private static final int MAX_ZOOM = 21;
    private static final int SHIFT_BITS = 29;

    private final double zoomOffset;

    public PixelToCoordDistanceMap(final int zoom) {
        zoomOffset = 1 << (SHIFT_BITS - MAX_ZOOM + zoom);
    }

    public double map(final double pixelDistance) {
        return pixelDistance / zoomOffset;
    }

}
