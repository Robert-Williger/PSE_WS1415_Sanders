package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adminTool.labeling.roadMap.decomposition.AbstractFilter;
import util.UnionFind;

public class ForestFilter extends AbstractFilter {
    private final int[] map;
    private UnionFind unionFind;

    public ForestFilter(final int points) {
        this.map = new int[points];
    }

    @Override
    public void filter(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        final int nodes = initMap(roadSections, junctionSections);
        this.unionFind = new UnionFind(nodes);
        this.filteredSections = new ArrayList<>(junctionSections.size());
        this.junctionSections = new ArrayList<>(junctionSections.size());
        this.roadSections = roadSections;

        linkRoadSections(roadSections);
        addJunctions(junctionSections);

        printUnionSizes(nodes);
    }

    private void linkRoadSections(final List<LabelSection> roads) {
        for (final LabelSection road : roads) {
            unionFind.link(map[road.getPoint(0)], map[road.getPoint(road.size() - 1)]);
        }
    }

    private void addJunctions(final List<LabelSection> roads) {
        for (final LabelSection road : roads) {
            (unionFind.union(map[road.getPoint(0)], map[road.getPoint(road.size() - 1)]) ? junctionSections
                    : filteredSections).add(road);
        }
    }

    private int initMap(final List<LabelSection> roadSections, final List<LabelSection> junctionSections) {
        Arrays.fill(map, -1);
        int nodeId = 0;
        for (final LabelSection road : roadSections) {
            nodeId = addNode(road.getPoint(0), nodeId);
            nodeId = addNode(road.getPoint(road.size() - 1), nodeId);
        }
        for (final LabelSection road : junctionSections) {
            nodeId = addNode(road.getPoint(0), nodeId);
            nodeId = addNode(road.getPoint(road.size() - 1), nodeId);
        }
        return nodeId;
    }

    private int addNode(final int node, int nodeId) {
        if (map[node] == -1)
            map[node] = nodeId++;
        return nodeId;
    }

    private void printUnionSizes(final int nodes) {
        int[] counts = new int[nodes];
        for (int i = 0; i < counts.length; ++i) {
            ++counts[unionFind.find(i)];
        }
        Arrays.sort(counts);
        System.out.println(Arrays.toString(Arrays.copyOfRange(counts, counts.length - 100, counts.length)));
    }
}
