package model.map;

public class PixelConverter implements IPixelMapping {

    private final int conversionBits;

    public PixelConverter(final int conversionBits) {
        this.conversionBits = conversionBits;
    }

    @Override
    public int getCoordDistance(final int pixelDistance, final int zoomStep) {
        return pixelDistance << (conversionBits - zoomStep);
    }

    @Override
    public int getPixelDistance(final int coordDistance, final int zoomStep) {
        return coordDistance >> (conversionBits - zoomStep);
    }

    @Override
    public float getCoordDistance(float pixelDistance, int zoomStep) {
        return pixelDistance * (1 << (conversionBits - zoomStep));
    }

    @Override
    public float getPixelDistance(final float coordDistance, final int zoomStep) {
        return coordDistance / (1 << (conversionBits - zoomStep));
    }

    @Override
    public double getCoordDistance(double pixelDistance, int zoomStep) {
        return pixelDistance * (1 << (conversionBits - zoomStep));
    }

    @Override
    public double getPixelDistance(double coordDistance, int zoomStep) {
        return coordDistance / (1 << (conversionBits - zoomStep));
    }
}
