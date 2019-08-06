package adminTool;

import java.awt.geom.Dimension2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.PointAccess;
import adminTool.elements.Street;
import adminTool.elements.Way;
import adminTool.labeling.DrawInfo;
import adminTool.labeling.QualityMeasure;
import adminTool.labeling.StringWidthInfo;
import adminTool.labeling.roadGraph.Road;
import adminTool.labeling.roadGraph.RoadGraphCreator;
import adminTool.labeling.roadGraph.filter.ForestFilter;
import adminTool.labeling.roadGraph.filter.QualityFilter;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;
import util.IntList;

public class CreateTest {
    private static int BUFFER_SIZE = 65536;

    public static void main(final String[] args) {
        new CreateTest();
    }

    public CreateTest() {
        long start = System.currentTimeMillis();

        ZipOutputStream zipOutput = null;
        try {
            zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("default.map"), BUFFER_SIZE));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        if (zipOutput != null) {
            zipOutput.setLevel(ZipOutputStream.STORED);
            IOSMParser parser = new OSMParser();
            try {
                parser.read(new File("default.pbf"));
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }

            final Collection<Way> ways = parser.getWays();
            final Collection<Label> labels = parser.getLabels();
            final Collection<POI> pois = parser.getPOIs();
            final Collection<Building> buildings = parser.getBuildings();
            final Collection<MultiElement> areas = parser.getAreas();
            IPointAccess points = parser.getPoints();
            parser = null;
            System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            start = System.currentTimeMillis();
            GraphWriter graphWriter = new GraphWriter(ways, points, zipOutput);
            try {
                graphWriter.write();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final List<Street> streets = graphWriter.getStreets();
            graphWriter = null;
            System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            Projector projector = new Projector(new MercatorProjection());
            projector.performProjection(points);
            projector = null;

            Aligner aligner = new Aligner();
            aligner.performAlignment(points, ways, areas);
            final Dimension2D size = aligner.getSize();
            aligner = null;

            final int zoom = 17;
            final int zoomOffset = 21 - zoom;
            final double shift = 1 << 29;

            final double maxWayWidth = (18 << zoomOffset) / shift;
            final double fuzzyThreshold = (1 << zoomOffset) / shift;
            final double tThreshold = (2 << zoomOffset) / shift;
            final double lengthThreshold = (350 << zoomOffset) / shift;
            final double lMax = (20 << zoomOffset) / shift;
            final double stubThreshold = maxWayWidth;
            final double junctionThreshold = 2 * maxWayWidth;

            final double alphaMax = 22.5 / 180 * Math.PI; // 22.5°

            RoadGraphCreator roadGraphCreator = new RoadGraphCreator(new DrawInfo(), new StringWidthInfo(),
                    stubThreshold, tThreshold, fuzzyThreshold, junctionThreshold, lMax, alphaMax, lengthThreshold);
            roadGraphCreator.createRoadGraph(ways, points, size);
            List<Road> roads = roadGraphCreator.getRoads();
            points = roadGraphCreator.getPoints();

            ForestFilter forestFilter = new ForestFilter(points.size());
            forestFilter.filter(roads);
            roads = forestFilter.getRoads();

            addRoads(streets, roads);

            start = System.currentTimeMillis();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), labels, points, size, zipOutput);
            // MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, areas, buildings, pois, labels, points,
            // aligner.getSize(), zipOutput);

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

    private void addRoads(final List<Street> streets, final List<Road> roads) {
        streets.clear();
        for (final Road element : roads) {
            final IntList indices = new IntList(element.size());
            for (int i = 0; i < element.size(); ++i) {
                indices.add(element.getNode(i));
            }
            final int type = element.getRoadType().getIndex() + 24;
            final String name = element.getRoadId() == -1 ? "junction edge"
                    : element.getRoadId() == -2 ? "blocked section" : "road section";
            streets.add(
                    new Street(indices, type, name + "(" + element.getType() + ", " + element.getRoadId() + ")", -1));
        }
    }
}