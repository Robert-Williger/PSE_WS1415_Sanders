package adminTool.map;

import java.io.File;
import java.io.IOException;

import adminTool.GraphCreator;
import adminTool.IOSMParser;
import adminTool.OSMParser;

public class CreateTest {
    public static void main(final String[] args) {
        new CreateTest();
    }

    public CreateTest() {
        long start = System.currentTimeMillis();

        IOSMParser parser = new OSMParser();
        try {
            parser.read(new File("default.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");
        start = System.currentTimeMillis();

        final File outputDir = new File("quadtree");

        GraphCreator graph = new GraphCreator(parser.getStreets(), new File("quadtree/graph"));
        graph.create();

        System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
        start = System.currentTimeMillis();

        MapManagerCreator creator = new MapManagerCreator(parser.getBuildings(), graph.getStreets(), parser.getPOIs(),
                parser.getWays(), parser.getTerrain(), parser.getLabels(), parser.getBoundingBox(),
                outputDir.getAbsolutePath());

        try {
            creator.create();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("map manager creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
    }
}