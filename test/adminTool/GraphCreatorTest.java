package adminTool;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GraphCreatorTest {

    private static GraphCreator gc;
    private static File outFile;

    @BeforeClass
    public static void setUp() {

        final IOSMParser parser = new OSMParser();
        try {
            parser.read(new File("Resources/osmtest.pbf"));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        outFile = new File("Resources/osmtest.tsk");
        gc = new GraphCreator(parser.getStreets(), outFile);
    }

    @AfterClass
    public static void cleanUp() {
        if (outFile.exists()) {
            outFile.delete();
        }

    }

    @Test
    public void testGraphCreation() {
        gc.create();
        /*
         * Tests the correct cutting of streets via number of streets.
         */

        assertEquals(30, gc.getStreets().size());

    }

}
