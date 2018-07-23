package adminTool.projection;

public class MercatorProjection implements IProjection {

    @Override
    public double getX(final double lat, final double lon) {
        return (lon / 180 + 1) / 2;
    }

    @Override
    public double getY(final double lat, final double lon) {
        return (1 - Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 360)) / Math.PI) / 2;
    }

}
