package adminTool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Boundary;
import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.elements.UnboundedPointAccess;
import adminTool.labeling.roadGraph.DrawInfo;
import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadGraphCreator;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;
import util.IntList;

public class CreateTest {
    public static void main(final String[] args) {
        new CreateTest();
    }

    public CreateTest() {
        long start = System.currentTimeMillis();

        ZipOutputStream zipOutput = null;
        try {
            zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("default.map")));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        if (zipOutput != null) {
            // zipOutput.setLevel(ZipOutputStream.STORED);
            IOSMParser parser = new OSMParser();
            try {
                parser.read(new File("default.pbf"));
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }

            System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            GraphWriter graphWriter = new GraphWriter(parser.getWays(), parser.getNodes(), zipOutput);
            try {
                graphWriter.write();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            Projector projector = new Projector(new MercatorProjection());
            projector.performProjection(parser.getNodes());
            final List<Street> streets = graphWriter.getStreets();
            final Collection<Label> labels = parser.getLabels();
            final Collection<POI> pois = parser.getPOIs();
            final Collection<Building> buildings = parser.getBuildings();
            final Collection<MultiElement> areas = parser.getTerrain();

            graphWriter = null;

            final Aligner aligner = new Aligner(projector.getPoints());
            aligner.performAlignment(streets, areas);

            final int zoom = 17;
            final int zoomOffset = 21 - zoom;
            final int maxWayWidth = 18;

            final int fuzzyThreshold = 1 << zoomOffset;
            final int tThreshold = 2 << zoomOffset;
            final int stubThreshold = maxWayWidth << zoomOffset;
            final int junctionThreshold = 2 * maxWayWidth << zoomOffset;

            RoadGraphCreator roadGraphCreator = new RoadGraphCreator(new DrawInfo(), stubThreshold, tThreshold,
                    fuzzyThreshold, junctionThreshold);
            roadGraphCreator.createRoadGraph(parser.getWays(), aligner.getPoints(), aligner.getSize());

            addRoadGraph(streets, aligner.getPoints(), roadGraphCreator);

            start = System.currentTimeMillis();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, areas, buildings, pois, labels,
                    aligner.getPoints(), aligner.getSize(), zipOutput);

            try {
                mapManagerWriter.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            System.out.println("map manager creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            // Collection<Boundary> boundaries = parser.getBoundaries();
            // parser = null;
            //
            // IndexWriter indexWriter = new IndexWriter(boundaries, mapManagerWriter.streetSorting,
            // aligner.getPoints(),
            // zipOutput);
            // try {
            // indexWriter.write();
            // } catch (final IOException e) {
            // e.printStackTrace();
            // }
            System.out.println("index creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            try {
                zipOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addRoadGraph(final List<Street> streets, final UnboundedPointAccess alignedPoints,
            final RoadGraphCreator roadGraphCreator) {
        for (final Road element : roadGraphCreator.getRoads()) {
            final IntList indices = new IntList(element.size());
            for (int i = 0; i < element.size(); ++i) {
                indices.add(element.getNode(i) + alignedPoints.getPoints());
            }
            final int type = element.getRoadId() == -1 ? 24 : element.getRoadId() == -2 ? 26 : 25;
            final String name = element.getRoadId() == -1 ? "junction edge"
                    : element.getRoadId() == -2 ? "blocked section" : "road section";
            streets.add(new Street(indices, type, name, -1));
        }

        IPointAccess points = roadGraphCreator.getPoints();
        for (int i = 0; i < points.getPoints(); ++i) {
            alignedPoints.addPoint(points.getX(i), points.getY(i));
        }
    }
}