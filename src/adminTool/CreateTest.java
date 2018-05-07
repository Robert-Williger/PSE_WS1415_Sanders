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
import adminTool.elements.Label;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.Street;
import adminTool.labeling.roadGraph.RoadGraphCreator;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;

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

            final int maxWayCoordWidth = 18 << 3;
            final int simplificationThreshold = 18 << 2;
            final int stubThreshold = 30 << 2;
            final int tThreshold = 2 << 2;
            final int junctionThreshold = 18 << 4;
            RoadGraphCreator roadGraphCreator = new RoadGraphCreator(maxWayCoordWidth, simplificationThreshold,
                    stubThreshold, tThreshold, junctionThreshold);
            roadGraphCreator.createRoadGraph(parser.getWays(), aligner.getPoints(), aligner.getSize());

            for (final MultiElement element : roadGraphCreator.getPaths()) {
                final int[] indices = new int[element.size()];
                for (int i = 0; i < indices.length; ++i) {
                    indices[i] = element.getNode(i);
                }
                final int type = element.getType() == -1 ? 24 : 25;
                final String name = element.getType() == -1 ? "junction edge" : "road section";
                streets.add(new Street(indices, type, name, -1));
            }

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
}