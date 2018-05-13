package adminTool.projection;

public class MercatorProjection implements IProjection {
    private final static int DEFAULT_CONVERSION = 1 << 29;

    private final int conversionFactor;

    public MercatorProjection() {
        this(DEFAULT_CONVERSION);
    }

    public MercatorProjection(final int conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    @Override
    public int getX(final double lat, final double lon) {
        return (int) (getXCoord(lon) * conversionFactor);
    }

    @Override
    public int getY(final double lat, final double lon) {
        return (int) (getYCoord(lat) * conversionFactor);
    }

    private double getXCoord(final double lon) {
        return (lon / 180 + 1) / 2;
    }

    private double getYCoord(final double lat) {
        return (1 - Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360)) / Math.PI) / 2;
    }
}
