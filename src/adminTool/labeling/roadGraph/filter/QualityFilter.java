package adminTool.labeling.roadGraph.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import adminTool.labeling.QualityMeasure;
import adminTool.labeling.QualityMeasure.Interval;
import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadType;
import util.IntList;

public class QualityFilter {
    private final QualityMeasure qualityTest;

    private List<Road> filteredRoads;

    public QualityFilter(final QualityMeasure qualityTest) {
        this.qualityTest = qualityTest;
    }

    public List<Road> getRoads() {
        return filteredRoads;
    }

    public void filter(final List<Road> roads) {
        filteredRoads = new ArrayList<Road>();

        for (final Road road : roads)
            switch (road.getRoadType()) {
                case Junction:
                    addJunction(road);
                    break;
                /*case Road:
                    addRoad(road);
                    break;*/
                default:
                    filteredRoads.add(road);
            }
    }

    private void addJunction(final Road road) {
        if (!qualityTest.isWellShaped(road))
            road.setRoadType(RoadType.BadShape);
        filteredRoads.add(road);
    }

    private void addRoad(final Road road) {
        final Iterator<Interval> iterator = qualityTest.getWellShapedIntervals(road).iterator();
        final IntList indices = road.toList();
        Interval merged = iterator.next();
        int last = 0;
        while (iterator.hasNext()) {
            Interval interval = iterator.next();
            if (interval.getStart() <= merged.getEnd())
                merged.setEnd(interval.getEnd());
            else {
                filteredRoads.add(new Road(new IntList(indices, merged.getStart(), merged.getEnd() + 1), road.getType(),
                        road.getRoadId(), RoadType.Road));
                if (merged.getStart() != 0)
                    filteredRoads.add(new Road(new IntList(indices, last, merged.getStart()), road.getType(),
                            road.getRoadId(), RoadType.BadShape));
                last = merged.getEnd();
                merged = interval;
            }
        }
        filteredRoads.add(new Road(new IntList(indices, merged.getStart(), merged.getEnd() + 1), road.getType(),
                road.getRoadId()));
    }

}
