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
import adminTool.elements.Street;
import adminTool.elements.Way;
import adminTool.labeling.INameInfo;
import adminTool.labeling.RoadLabelCreator;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.metrics.PixelToCoordDistanceMap;
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
            final Dimension2D mapSize = aligner.getSize();
            aligner = null;

            RoadLabelCreator labelCreator = new RoadLabelCreator(ways, points, mapSize);
            labelCreator.createLabels(new PixelToCoordDistanceMap(17));
            for (final adminTool.labeling.Label label : labelCreator.getLabeling()) {
                streets.add(new Street(label, 26, label.getName(), 0));
            }
            points = labelCreator.getPoints();
            System.out.println("label creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            start = System.currentTimeMillis();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), labels, points, mapSize, zipOutput);
            // MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, areas, buildings, pois, labels, points,
            // size, zipOutput);

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

    private void addRoadMap(final List<Street> streets, final List<LabelSection> roadSections,
            final List<LabelSection> junctionSections, final INameInfo info) {
        for (final LabelSection roadSection : roadSections) {
            final IntList indices = new IntList(roadSection.size());
            for (int i = 0; i < roadSection.size(); ++i) {
                indices.add(roadSection.getPoint(i));
            }
            streets.add(new Street(indices, 24,
                    "road section (" + roadSection.getRoadId() + ", " + info.getName(roadSection.getRoadId()) + ")",
                    -1));
        }
        for (final LabelSection junctionSection : junctionSections) {
            final IntList indices = new IntList(junctionSection.size());
            for (int i = 0; i < junctionSection.size(); ++i) {
                indices.add(junctionSection.getPoint(i));
            }
            streets.add(new Street(indices, 25, "junction section (" + junctionSection.getRoadId() + ", "
                    + info.getName(junctionSection.getRoadId()) + ")", -1));
        }
    }
}