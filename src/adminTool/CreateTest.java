package adminTool;

import java.io.File;

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
        final File output = new File("default.map");

        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        GraphCreator graph = new GraphCreator(parser.getStreets(), output);
        graph.create();

        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        final MapManagerCreator creator = new MapManagerCreator(parser.getBuildings(), graph.getStreets(),
                parser.getPOIs(), parser.getWays(), parser.getTerrain(), parser.getBoundingBox(), output);

        parser = null;
        graph = null;

        creator.create();

        System.out.println(System.currentTimeMillis() - start);
    }
}