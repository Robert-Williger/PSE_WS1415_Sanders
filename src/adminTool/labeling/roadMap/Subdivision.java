package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.List;

import adminTool.PointLocator;
import adminTool.elements.CutPerformer;
import adminTool.elements.PointAccess;
import adminTool.elements.CutPerformer.Cut;
import adminTool.util.ElementAdapter;
import util.IntList;

public class Subdivision {

    private final double lengthThreshold;
    private final double dummyJunctionLength;
    private final CutPerformer cutPerformer;
    private final List<Cut> cuts;
    private List<LabelSection> roadSections;
    private List<LabelSection> junctionSections;

    public Subdivision(final double lengthThreshold, final double dummyJunctionLength) {
        this.lengthThreshold = lengthThreshold;
        this.dummyJunctionLength = dummyJunctionLength;
        this.cutPerformer = new CutPerformer();
        this.cuts = new ArrayList<>(3);
        for (int i = 0; i < 3; ++i)
            cuts.add(new Cut(0, 0, 0));
    }

    public Subdivision(final double lengthThreshold) {
        this(lengthThreshold, 0.01 * lengthThreshold);
    }

    public List<LabelSection> getRoadSections() {
        return roadSections;
    }

    public List<LabelSection> getJunctionSections() {
        return junctionSections;
    }

    public void subdivide(final List<LabelSection> roadSections, final List<LabelSection> junctionSections,
            final PointAccess points) {
        this.roadSections = new ArrayList<>(roadSections.size());
        this.junctionSections = junctionSections;

        final PointLocator locator = new PointLocator(points);
        final ElementAdapter adapter = new ElementAdapter(points);

        for (final LabelSection road : roadSections) {
            adapter.setMultiElement(road);
            subdivideRecursive(road, adapter.getLength(), locator, points);
        }
    }

    public void subdivideRecursive(final LabelSection road, final double length, final PointLocator locator,
            final PointAccess points) {
        if (length > lengthThreshold) {
            double displacedDistance = length / 2 - dummyJunctionLength;
            for (int j = 0; j < 3; ++j) {
                locator.locate(road, displacedDistance);
                cuts.set(j, new Cut(points.size(), locator.getSegment(), locator.getOffset()));
                points.addPoint(locator.getX(), locator.getY());
                displacedDistance += dummyJunctionLength;
            }

            final double newLength = length / 2 - 2 * dummyJunctionLength;
            List<IntList> paths = cutPerformer.performSortedCuts(road, cuts);
            subdivideRecursive(new LabelSection(paths.get(0), road.getType(), road.getRoadId()), newLength, locator,
                    points);
            this.junctionSections.add(new LabelSection(paths.get(1), road.getType(), road.getRoadId()));
            this.junctionSections.add(new LabelSection(paths.get(2), road.getType(), road.getRoadId()));
            subdivideRecursive(new LabelSection(paths.get(3), road.getType(), road.getRoadId()), newLength, locator,
                    points);
        } else
            this.roadSections.add(road);
    }
}
