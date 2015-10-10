package adminTool;

import java.io.File;
import java.util.List;

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

        MapManagerCreator creator = new MapManagerCreator(parser.getBuildings(), graph.getStreets(), parser.getPOIs(),
                parser.getWays(), parser.getTerrain(), parser.getBoundingBox(), output);

        // TODO take street list of mapManagerCreator instead of graph creator
        // [already sorted]

        List<List<Boundary>> boundaries = parser.getBoundaries();
        parser = null;
        graph = null;

        creator.create();

        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();

        IndexCreator indexCreator = new IndexCreator(boundaries, creator.getOrderedStreets(), output);
        creator = null;
        boundaries = null;

        indexCreator.create();

        System.out.println(System.currentTimeMillis() - start);
    }
}