package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.INameInfo;
import util.UnionFind;

public class RoadMapsCreator {

    private int[] repToComp;
    private int[] pointToNode;
    private UnionFind unionFind;
    private int components;
    private Collection<RoadMap> roadMaps;

    public Collection<RoadMap> getRoadMaps() {
        return roadMaps;
    }

    public void createMaps(final Collection<LabelSection> roadSections, final Collection<LabelSection> junctionSections,
            final IPointAccess points, final ILabelInfo labelInfo, final INameInfo nameInfo) {
        initMap(roadSections, junctionSections, points.size());
        repToComp = new int[components];
        unionFind = new UnionFind(components);

        linkRoadSections(roadSections);
        addJunctions(junctionSections);
        roadMaps = new ArrayList<>(components);
        List<Collection<LabelSection>> junctionSectionList = new ArrayList<>(components);
        List<Collection<LabelSection>> roadSectionList = new ArrayList<>(components);
        int compId = 0;
        for (int node = 0; node < repToComp.length; ++node) {
            junctionSectionList.add(new ArrayList<LabelSection>());
            roadSectionList.add(new ArrayList<LabelSection>());
            if (unionFind.find(node) == node)
                repToComp[node] = compId++;
        }
        splitSections(junctionSections, junctionSectionList);
        splitSections(roadSections, roadSectionList);

        RoadMapCreator creator = new RoadMapCreator(points, labelInfo, nameInfo);
        for (int i = 0; i < components; ++i) {
            creator.createRoadMap(roadSectionList.get(i), junctionSectionList.get(i));
            roadMaps.add(creator.getRoadMap());
        }
    }

    private void splitSections(final Collection<LabelSection> sections,
            final List<Collection<LabelSection>> sectionList) {
        for (final LabelSection section : sections) {
            int node = pointToNode[section.getPoint(0)];
            int rep = unionFind.find(node);
            sectionList.get(repToComp[rep]).add(section);
        }
    }

    private void linkRoadSections(final Collection<LabelSection> roads) {
        for (final LabelSection road : roads)
            unionFind.link(pointToNode[road.getPoint(0)], pointToNode[road.getPoint(road.size() - 1)]);

        components -= roads.size();
    }

    private void addJunctions(final Collection<LabelSection> junctions) {
        for (final LabelSection r : junctions)
            components -= unionFind.union(pointToNode[r.getPoint(0)], pointToNode[r.getPoint(r.size() - 1)]) ? 1 : 0;
    }

    private void initMap(final Collection<LabelSection> roadSections, final Collection<LabelSection> junctionSections,
            final int points) {
        pointToNode = new int[points];
        Arrays.fill(pointToNode, -1);

        for (final LabelSection road : roadSections)
            addSection(road);

        for (final LabelSection road : junctionSections)
            addSection(road);
    }

    private void addSection(final LabelSection road) {
        addNode(road.getPoint(0));
        addNode(road.getPoint(road.size() - 1));
    }

    private void addNode(final int node) {
        if (pointToNode[node] == -1)
            pointToNode[node] = components++;
    }

}
