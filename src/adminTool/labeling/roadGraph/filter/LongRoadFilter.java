package adminTool.labeling.roadGraph.filter;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adminTool.elements.PointAccess;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadGraph.CutPerformer.Cut;
import adminTool.labeling.roadGraph.ElementAdapter;
import adminTool.labeling.roadGraph.CutPerformer;
import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadType;
import util.IntList;

public class LongRoadFilter {
    private final ILabelInfo labelInfo;
    private final QualityMeasure qualityTest;
    private final PointAccess points;
    private final CutPerformer cutPerformer;

    private final ElementAdapter adapter;
    private List<Road> originalRoads;
    private List<Road> filteredRoads;

    public LongRoadFilter(final QualityMeasure qualityTest, final ILabelInfo labelInfo, final PointAccess points) {
        this.labelInfo = labelInfo;
        this.qualityTest = qualityTest;
        this.points = points;
        this.cutPerformer = new CutPerformer();
        this.adapter = new ElementAdapter(points);
    }

    public List<Road> getRoads() {
        return filteredRoads;
    }

    public void filter(final List<Road> roads) {
        this.originalRoads = roads;
        this.filteredRoads = new ArrayList<>();
        filterLongRoads();
    }

    private void filterLongRoads() {
        for (final Road road : originalRoads) {
            if (road.getRoadType() == RoadType.Road) {                
                final double labelLength = labelInfo.getWidth(road.getRoadId());
                adapter.setMultiElement(road);
                final double roadLength = adapter.getLength();
                if (!qualityTest.hasWellShapedPiece(road, labelLength))
                    filteredRoads.add(road);
                else if (roadLength >= 2 * labelLength)
                    appendTwoHalves(road, roadLength);
                else
                    //TODO apply rule 4
                    filteredRoads.add(road);
            }
            else
                filteredRoads.add(road);
        }
    }

    private void appendTwoHalves(final Road road, final double roadLength) {
        final double halfRoadLength = roadLength / 2;
        final Point2D last = new Point2D.Double(points.getX(road.getNode(0)), points.getY(road.getNode(0)));
        final Point2D current = new Point2D.Double();
        double totalLength = 0;

        for (int i = 1; i < road.size(); i++) {
            current.setLocation(points.getX(road.getNode(i)), points.getY(road.getNode(i)));
            final double distance = last.distance(current);

            if (totalLength + distance >= halfRoadLength) {
                final double s = (halfRoadLength - totalLength) / distance;
                final List<Cut> cuts = Collections.singletonList(new Cut(points.size(), i - 1, s));
                points.addPoint(last.getX() + s * (current.getX() - last.getX()),
                        last.getY() + s * (current.getY() - last.getY()));
                for (final IntList indices : cutPerformer.performCuts(road, cuts))
                    filteredRoads.add(new Road(indices, road.getType(), road.getRoadId()));
                return;
            }

            totalLength += distance;
            last.setLocation(current);
        }
    }
}
