package adminTool;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class OSMParserTest {

    private IOSMParser parser;
    private File file;

    @Before
    public void setUp() {
        parser = new OSMParser();
        file = new File("resources/osmtest.pbf");
        try {
            parser.read(file);
        } catch (final Exception e) {
        }
    }

    @Test
    public void testParser1() {

        /*
         * Tests, if the correct amount of elements is parsed from the testfile.
         */
        assertEquals(7, parser.getPOIs().size());
        // assertEquals(12, parser.getStreets().size());
        assertEquals(16, parser.getWays().size());
        assertEquals(8, parser.getTerrain().size());
        assertEquals(1, parser.getBuildings().size());

    }

}
