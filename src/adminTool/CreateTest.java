package adminTool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Boundary;
import adminTool.elements.Street;
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

            GraphWriter graphWriter = new GraphWriter(parser.getWays(), parser.getOneways(), parser.getNodes(),
                    zipOutput);
            try {
                graphWriter.write();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            Projector projector = new Projector(parser.getNodes(), new MercatorProjection());
            projector.performProjection();
            List<Street> streets = graphWriter.getStreets();
            graphWriter = null;

            start = System.currentTimeMillis();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, parser.getTerrain(),
                    parser.getBuildings(), parser.getPOIs(), parser.getLabels(), projector.getPoints(),
                    projector.getSize(), zipOutput);

            try {
                mapManagerWriter.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            System.out.println("map manager creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            List<List<Boundary>> boundaries = parser.getBoundaries();
            parser = null;
            IndexWriter indexWriter = new IndexWriter(boundaries, mapManagerWriter.streetSorting, projector.getPoints(),
                    zipOutput);
            try {
                indexWriter.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            System.out.println("index creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            try {
                zipOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}