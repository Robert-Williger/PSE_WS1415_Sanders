package adminTool.projection;

public class MercatorProjection implements IProjection {
    private final int SHIFT = 1 << 29;

    @Override
    public int getX(final double lat, final double lon) {
        return (int) (getXCoord(lon) * SHIFT);
    }
    
    @Override
    public int getY(final double lat, final double lon) {
        return (int) (getYCoord(lat) * SHIFT);
    }
    
    private double getXCoord(final double lon) {
        return (lon / 180 + 1) / 2;
    }

    private double getYCoord(final double lat) {
        return (1 - Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360)) / Math.PI) / 2;
    }
}
