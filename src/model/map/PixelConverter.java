package model.map;

public class PixelConverter implements IPixelConverter {

    private final double conversionFactor;

    public PixelConverter(final double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    @Override
    public int getCoordDistance(final int pixelDistance, final int zoomStep) {
        return (int) (pixelDistance * conversionFactor / (1 << zoomStep));
    }

    @Override
    public int getPixelDistance(final int coordDistance, final int zoomStep) {
        return (int) ((coordDistance / conversionFactor * (1 << zoomStep)));
    }

    @Override
    public float getPixelDistancef(final float coordDistance, final int zoomStep) {
        return (float) ((coordDistance / conversionFactor * (1 << zoomStep)));
    }
}
