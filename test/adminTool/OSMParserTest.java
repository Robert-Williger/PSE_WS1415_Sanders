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
        final Rectangle bbox = parser.getBoundingBox();

        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        assertEquals(6635, bbox.height);
        assertEquals(10656, bbox.width);

    }
}
