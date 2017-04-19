package adminTool.map;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import adminTool.GraphCreator;
import adminTool.IOSMParser;
import adminTool.OSMParser;

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
            }

            System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            GraphCreator graphCreator = new GraphCreator(parser.getStreets(), zipOutput);
            graphCreator.create();

            System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            MapManagerCreator mapCreator = new MapManagerCreator(parser.getBuildings(), graphCreator.getStreets(),
                    parser.getPOIs(), parser.getWays(), parser.getTerrain(), parser.getLabels(),
                    parser.getBoundingBox(), zipOutput);

            try {
                mapCreator.create();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            System.out.println("map manager creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            try {
                zipOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}