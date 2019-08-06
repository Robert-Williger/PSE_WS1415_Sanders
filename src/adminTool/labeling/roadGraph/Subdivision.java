package adminTool.labeling.roadGraph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import adminTool.elements.PointAccess;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.util.IntersectionUtil;
import util.IntList;

public class Subdivision {

    private final double lengthThreshold;
    private List<Road> processedRoads;

    public Subdivision(final double lengthThreshold) {
        this.lengthThreshold = lengthThreshold;
    }

    public List<Road> getRoads() {
        return processedRoads;
    }

    public void subdivide(final List<Road> roads, final PointAccess points) {
        processedRoads = new ArrayList<>(roads.size());

        final List<Cut> cuts = new ArrayList<>();
        final CutPerformer cutPerformer = new CutPerformer();

        for (final Road road : roads) {
            if (road.getRoadType() == RoadType.Road) {
                final Point2D last = new Point2D.Double(points.getX(road.getNode(0)), points.getY(road.getNode(0)));
                final Point2D current = new Point2D.Double();

                double remainingDistance = lengthThreshold;
                int i = 1;
                do {
                    current.setLocation(points.getX(road.getNode(i)), points.getY(road.getNode(i)));

                    final double distance = current.distance(last);
                    if (distance > remainingDistance) {
                        double s = remainingDistance / distance - IntersectionUtil.EPSILON;
                        for (int j = 0; j < 3; ++j) {
                            cuts.add(new Cut(points.size(), i - 1, s));
                            points.addPoint(last.getX() + s * (current.getX() - last.getX()),
                                    last.getY() + s * (current.getY() - last.getY()));
                            s += IntersectionUtil.EPSILON;
                        }

                        remainingDistance = lengthThreshold;
                        last.setLocation(points.getX(points.size() - 1), points.getY(points.size() - 1));
                    } else {
                        remainingDistance -= distance;

                        last.setLocation(current);
                        ++i;
                    }

                } while (i < road.size());

                if (!cuts.isEmpty()) {
                    int count = 0;
                    for (final IntList path : cutPerformer.performSortedCuts(road, cuts)) {
                        processedRoads.add(new Road(path, road.getType(), road.getRoadId(), count++ % 3 != 0 ? RoadType.Junction : RoadType.Road));
                    }
                    cuts.clear();
                    continue;
                }
            }

            processedRoads.add(road);
        }
    }
}
