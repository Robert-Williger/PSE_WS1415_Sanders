package adminTool;

import java.io.File;

public class TestAdminMain {

    public static void main(final String args[]) {

        final OSMParser sp = new OSMParser();

        // http://download.geofabrik.de/europe/germany/baden-wuerttemberg/karlsruhe-regbez-latest.osm.pbf
        // + umbennenen
        try {
            sp.read(new File("resources/karlsruhe.osm.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // GraphCreator gc = new GraphCreator(sp, new
        // File("resources/test.tsk"));
        //
        // gc.createGraph();
        //
        // gc.write();
        // System.out.println("Graph geschrieben");

    }

}