package adminTool.labeling.roadMap.decomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adminTool.labeling.ILabelInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.roadMap.LabelSection;
import util.IntList;

public class RoadConnectionFilter extends AbstractFilter {

    private boolean[] active;

    private final ILabelInfo labelInfo;
    private final QualityMeasure qualityTest;

    private HashMap<Integer, IntList> junctionMap;
    private HashMap<Integer, LabelSection> roadMap;

    public RoadConnectionFilter(final QualityMeasure qualityTest, final ILabelInfo labelInfo) {
        this.labelInfo = labelInfo;
        this.qualityTest = qualityTest;
    }

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        this.filteredSections = new ArrayList<>(junctionSections.size());
        this.junctionSections = new ArrayList<>(junctionSections.size());
        this.roadSections = roadSections;

        active = new boolean[junctionSections.size()];
        Arrays.fill(active, true);

        junctionMap = createJunctionMap(junctionSections);
        roadMap = createRoadMap(roadSections);
        filterJunctions(junctionSections);
    }

    private void filterJunctions(final List<LabelSection> junctionSections) {
        for (final Map.Entry<Integer, IntList> entry : junctionMap.entrySet()) {
            final IntList junctionIds = entry.getValue();
            final int start = entry.getKey();
            if (junctionIds.size() > 1) {
                HashMap<Integer, Boolean> stubMap = new HashMap<>();
                for (int i = 0; i < junctionIds.size(); ++i) {
                    final int junctionId = junctionIds.get(i);
                    final LabelSection junction = junctionSections.get(junctionId);
                    final int end = getEnd(junction, start);
                    final LabelSection road = roadMap.get(end);
                    Boolean allStubs = stubMap.get(junction.getRoadId());
                    if (road == null)
                        stubMap.put(junction.getRoadId(), false);
                    else {
                        boolean stub = qualityTest.hasWellShapedPiece(road, labelInfo.getLength(road.getRoadId()));
                        if (allStubs == null)
                            stubMap.put(junction.getRoadId(), stub);
                        else if (allStubs && !stub)
                            stubMap.put(junction.getRoadId(), false);
                    }
                }

                for (int i = 0; i < junctionIds.size(); ++i) {
                    final int junctionId = junctionIds.get(i);
                    active[junctionId] &= !stubMap.get(junctionSections.get(junctionId).getRoadId());
                }
            }
        }
        for (int i = 0; i < junctionSections.size(); ++i) {
            final LabelSection junction = junctionSections.get(i);
            (active[i] ? this.junctionSections : this.filteredSections).add(junction);
        }
    }

    private int getEnd(final LabelSection road, final int start) {
        return road.getPoint(0) == start ? road.getPoint(road.size() - 1) : road.getPoint(0);
    }

    private HashMap<Integer, LabelSection> createRoadMap(final List<LabelSection> roads) {
        final HashMap<Integer, LabelSection> map = new HashMap<>();
        for (final LabelSection road : roads) {
            map.put(road.getPoint(0), road);
            map.put(road.getPoint(road.size() - 1), road);
        }
        return map;
    }

    private HashMap<Integer, IntList> createJunctionMap(final List<LabelSection> roads) {
        final HashMap<Integer, IntList> map = new HashMap<>();
        int id = 0;
        for (final LabelSection road : roads) {
            appendOccurance(road.getPoint(0), id, map);
            appendOccurance(road.getPoint(road.size() - 1), id, map);
            ++id;
        }
        return map;
    }

    private void appendOccurance(final int node, final int roadId, final HashMap<Integer, IntList> map) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(roadId);
    }
}
