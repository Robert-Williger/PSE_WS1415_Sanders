package adminTool.projection;

public interface IProjection {
    int getX(double lat, double lon);
    int getY(double lat, double lon);
}
