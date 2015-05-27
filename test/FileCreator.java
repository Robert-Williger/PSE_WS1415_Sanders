import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCreator {

    public static void main(final String[] args) {
        new FileCreator();
    }

    private DataOutputStream stream;

    public FileCreator() {
        try {
            stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("map.tst")));

            writeHeader();
            writeElements();
            writeTiles();

            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeader() throws IOException {
        stream.writeInt(15); // nodes
        stream.writeInt(1); // pois
        stream.writeInt(3); // names
        stream.writeInt(1); // numbers
        stream.writeInt(1); // streets
        stream.writeInt(1); // ways
        stream.writeInt(1); // street nodes
        stream.writeInt(1); // buildings
        stream.writeInt(1); // terrains

        stream.writeInt(1); // zoom steps
        stream.writeInt(1); // rows
        stream.writeInt(1); // columns
        stream.writeDouble(1); // conversion factor
        stream.writeInt(256); // tile width
        stream.writeInt(256); // tile height
    }

    private void writeElements() throws IOException {
        // ////////
        // NODES //
        // ////////

        // street
        stream.writeInt(-10);
        stream.writeInt(-5);

        stream.writeInt(80);
        stream.writeInt(90);

        stream.writeInt(400);
        stream.writeInt(210);

        stream.writeInt(520);
        stream.writeInt(325);

        // way

        stream.writeInt(25);
        stream.writeInt(-5);

        stream.writeInt(150);
        stream.writeInt(15);

        stream.writeInt(350);
        stream.writeInt(540);

        // building

        stream.writeInt(100);
        stream.writeInt(100);

        stream.writeInt(150);
        stream.writeInt(100);

        stream.writeInt(150);
        stream.writeInt(150);

        stream.writeInt(100);
        stream.writeInt(150);

        // area

        stream.writeInt(250);
        stream.writeInt(200);

        stream.writeInt(450);
        stream.writeInt(200);

        stream.writeInt(450);
        stream.writeInt(400);

        stream.writeInt(250);
        stream.writeInt(400);

        // ///////
        // POIs //
        // ///////

        stream.writeInt(125);
        stream.writeInt(375);
        stream.writeInt(2);

        // ////////
        // NAMES //
        // ////////

        stream.writeUTF("");
        stream.writeUTF("Teststraße");
        stream.writeUTF("Testweg");

        // //////////
        // NUMBERS //
        // //////////

        stream.writeUTF(" 2b");

        // //////////
        // STREETS //
        // //////////

        stream.writeShort(4); // node number
        stream.writeInt(0); // node ID
        stream.writeInt(1); // node ID
        stream.writeInt(2); // node ID
        stream.writeInt(3); // node ID
        stream.writeInt(0); // street type
        stream.writeInt(1); // name ID
        stream.writeLong(0); // edge ID

        // ///////
        // WAYS //
        // ///////

        stream.writeShort(3); // node number
        stream.writeInt(4); // node ID
        stream.writeInt(5); // node ID
        stream.writeInt(6); // node ID
        stream.writeInt(1); // way type
        stream.writeInt(2); // name ID

        // ///////////////
        // STREET NODES //
        // ///////////////

        stream.writeFloat(0.2f); // offset
        stream.writeInt(0); // street ID

        // ////////////
        // BUILDINGS //
        // ////////////

        stream.writeShort(4); // node number
        stream.writeInt(7); // node ID
        stream.writeInt(8); // node ID
        stream.writeInt(9); // node ID
        stream.writeInt(10); // node ID
        stream.writeInt(1); // name ID
        stream.writeInt(0); // number ID
        stream.writeInt(0); // street node ID

        // //////////
        // TERRAIN //
        // //////////

        stream.writeShort(4); // node number
        stream.writeInt(11); // node ID
        stream.writeInt(12); // node ID
        stream.writeInt(13); // node ID
        stream.writeInt(14); // node ID
        stream.writeInt(1); // terrain type
    }

    private void writeTiles() throws IOException {
        // tile header

        stream.writeInt(1); // pois
        stream.writeInt(1); // streets
        stream.writeInt(1); // ways
        stream.writeInt(1); // buildings
        stream.writeInt(1); // terrain

        // tile content

        stream.writeInt(0); // POI ID
        stream.writeInt(0); // Street ID
        stream.writeInt(0); // Way ID
        stream.writeInt(0); // Building ID
        stream.writeInt(0); // Terrain ID

    }
}
