package adminTool;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;
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
        assertEquals(12, parser.getStreets().size());
        assertEquals(4, parser.getWays().size());
        assertEquals(8, parser.getTerrain().size());
        assertEquals(1, parser.getBuildings().size());

    }

    @Test
    public void testBBox() {
        final Rectangle bbox = parser.getBoundingBox();System.out.println(bbox);

        assertEquals(281189122, bbox.x);
        assertEquals(184166043, bbox.y);
        assertEquals(1824, bbox.height);
        assertEquals(2718, bbox.width);

    }
}
