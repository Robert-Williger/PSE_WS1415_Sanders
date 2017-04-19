package adminTool;

import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class MapManagerCreatorTest {

    private static IOSMParser parser;
    private static GraphWriter gc;
    private static MapManagerWriter mmc;
    private static File gcFile;
    private static File mmcFile;

    @BeforeClass
    public static void setUp() {

        parser = new OSMParser();
        try {
            parser.read(new File("Resources/osmtest.pbf"));
        } catch (final Exception e) {
        }

        gcFile = new File("Resources/gcFile.tsk");
        gc = new GraphWriter(parser.getStreets(), gcFile);
        gc.create();

        mmcFile = new File("Resources/mmctest.tsk");
        try {
            mmcFile.createNewFile();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        mmc = new MapManagerWriter(parser.getBuildings(), gc.getStreets(), parser.getPOIs(), parser.getWays(),
                parser.getTerrain(), parser.getLabels(), parser.getBoundingBox(), mmcFile);

    }

    @After
    public void cleanUp() {
        if (mmcFile.exists()) {
            mmcFile.delete();
        }
        if (gcFile.exists()) {
            gcFile.delete();
        }
    }

    @Test
    public void testWrite() throws IOException {
        mmc.create();

        boolean equals = true;

        InputStream is1 = null;
        DataInputStream dis1 = null;
        InputStream is2 = null;
        DataInputStream dis2 = null;

        try {
            // written file
            is1 = new FileInputStream(mmcFile);
            // comparison-file
            is2 = new FileInputStream(new File("Resources/mmc.tsk"));

            dis1 = new DataInputStream(is1);
            dis2 = new DataInputStream(is2);

            while (dis1.available() > 0) {
                final Byte a = dis1.readByte();
                final Byte b = dis2.readByte();
                if (!a.equals(b)) {
                    equals = false;

                }
            }
        } catch (final Exception e) {
            equals = false;
        } finally {
            if (is1 != null) {
                is1.close();
            }
            if (dis1 != null) {
                dis1.close();
            }
            if (is2 != null) {
                is2.close();
            }
            if (dis2 != null) {
                dis2.close();
            }
        }

        assertTrue(equals);
    }
}
