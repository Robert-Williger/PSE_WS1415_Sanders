package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.labeling.Embedding;
import adminTool.labeling.ILabelInfo;
import adminTool.labeling.INameInfo;
import adminTool.util.ElementAdapter;

public class RoadMapCreator {

    private final int[] pointToNode;
    private final int[] run;
    private int runs;

    private final IPointAccess points;
    private final ILabelInfo labelInfo;
    private final INameInfo nameInfo;

    private int[] firstOut;
    private int[] roads;
    private int[] head;
    private int[] pathId;
    private double[] lengths;

    private RoadGraph graph;
    private Embedding embedding;
    private List<LabelSection> sections;
    private RoadMap roadMap;

    public RoadMapCreator(final IPointAccess points, final ILabelInfo labelInfo, final INameInfo nameInfo) {
        this.points = points;
        this.labelInfo = labelInfo;
        this.nameInfo = nameInfo;
        this.pointToNode = new int[points.size()];
        this.run = new int[points.size()];
        Arrays.fill(pointToNode, -1);
    }

    public RoadMap getRoadMap() {
        return roadMap;
    }

    public void createRoadMap(final Collection<LabelSection> roadSections,
            final Collection<LabelSection> junctionSections) {
        init(roadSections, junctionSections);
        createGraph(roadSections, junctionSections);
        roadMap = new RoadMap(graph, embedding, labelInfo, nameInfo);
    }

    private void createGraph(final Collection<LabelSection> roadSections,
            final Collection<LabelSection> junctionSections) {
        initRoads(roadSections);
        countOutgoingEdges(sections);
        for (int i = 1; i < firstOut.length; ++i)
            firstOut[i] += firstOut[i - 1];

        addEdges(sections, points);

        graph = new RoadGraph(firstOut, head, lengths, pathId, roads, roads.length, head.length);
    }

    private void initRoads(final Collection<LabelSection> roadSections) {
        Arrays.fill(roads, -1);
        for (final LabelSection roadSection : roadSections) {
            roads[convertToNode(roadSection.getPoint(0))] = roadSection.getRoadId();
            roads[convertToNode(roadSection.getPoint(roadSection.size() - 1))] = roadSection.getRoadId();
        }
    }

    private void addEdge(final int u, final int v, final int id, final double length) {
        int pos = firstOut[u + 1];
        lengths[pos] = length;
        head[pos] = v;
        pathId[pos] = id;
        ++firstOut[u + 1];
    }

    private void init(final Collection<LabelSection> roadSections, final Collection<LabelSection> junctionSections) {
        ++runs;
        sections = new ArrayList<>(2 * (roadSections.size() + junctionSections.size()));
        addSections(roadSections);
        addSections(junctionSections);
        int nodes = initMap();
        firstOut = new int[nodes + 2];
        roads = new int[nodes];
        head = new int[sections.size()];
        pathId = new int[head.length];
        lengths = new double[head.length];
        embedding = new Embedding(sections, pathId, points);
    }

    private void addSections(final Collection<LabelSection> roadSections) {
        for (LabelSection section : roadSections) {
            sections.add(section);
            sections.add(new LabelSection(section.reverse(), section.getType(), section.getRoadId()));
        }
    }

    private int initMap() {
        int nodeId = 0;
        for (final LabelSection road : sections) {
            nodeId = addNode(road.getPoint(0), nodeId);
            nodeId = addNode(road.getPoint(road.size() - 1), nodeId);
        }

        return nodeId;
    }

    private int addNode(final int point, int nodeId) {
        if (run[point] != runs) {
            pointToNode[point] = nodeId++;
            run[point] = runs;
        }
        return nodeId;
    }

    private int convertToNode(final int point) {
        return pointToNode[point];
    }

    private void countOutgoingEdges(final Collection<LabelSection> sections) {
        for (final LabelSection section : sections)
            ++firstOut[convertToNode(section.getPoint(0)) + 2];
    }

    private void addEdges(final List<LabelSection> sections, final IPointAccess points) {
        final ElementAdapter adapter = new ElementAdapter(points);
        for (int id = 0; id < sections.size(); ++id) {
            final LabelSection section = sections.get(id);
            adapter.setMultiElement(section);
            int u = convertToNode(section.getPoint(0));
            int v = convertToNode(section.getPoint(section.size() - 1));
            double length = adapter.getLength();

            addEdge(u, v, id, length);
        }
    }

}
