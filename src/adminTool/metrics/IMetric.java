package adminTool.metrics;

@FunctionalInterface
public interface IMetric {

    double distance(double x1, double y1, double x2, double y2);

}