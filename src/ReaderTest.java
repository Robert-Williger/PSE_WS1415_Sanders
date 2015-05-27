import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import model.elements.Building;
import model.elements.Node;
import model.elements.Street;

public class ReaderTest {

    public static void main(final String[] args) {
        new ReaderTest();
    }

    public ReaderTest() {
        DataInputStream nodeStream = null;
        DataInputStream wayStream = null;
        DataInputStream streetStream = null;
        DataInputStream buildingStream = null;
        DataInputStream areaStream = null;

        try {
            nodeStream = new DataInputStream(new BufferedInputStream(new FileInputStream("_nodes.tmp")));
            wayStream = new DataInputStream(new BufferedInputStream(new FileInputStream("_ways.tmp")));
            streetStream = new DataInputStream(new BufferedInputStream(new FileInputStream("_streets.tmp")));
            buildingStream = new DataInputStream(new BufferedInputStream(new FileInputStream("_buildings.tmp")));
            areaStream = new DataInputStream(new BufferedInputStream(new FileInputStream("_areas.tmp")));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        final Node[] nodes = new Node[207743058];
        final Street[] streets = new Street[8034407];
        final Building[] buildings = new Building[19399113];

        int fail = 0;
        try {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = new Node(nodeStream.readInt(), nodeStream.readInt());
                if (i % 1000000 == 0) {
                    System.out.println(i);
                }
            }
            for (int i = 0; i < 8034407; i++) {
                fail = i;
                final String name = streetStream.readUTF();
                final int type = streetStream.readByte();
                final int size = streetStream.readShort();
                final ArrayList<Node> nodeList = new ArrayList<Node>(size);
                for (int j = 0; j < size; j++) {
                    nodeList.add(nodes[streetStream.readInt()]);
                }
                streets[i] = new Street(nodeList, type, name, 0);

                if (i % 1000000 == 0) {
                    System.out.println(i);
                }

            }
            for (int i = 0; i < buildings.length; i++) {
                final String name = buildingStream.readUTF();
                final int size = buildingStream.readShort();

                final ArrayList<Node> nodeList = new ArrayList<Node>(size);
                for (int j = 0; j < size; j++) {
                    nodeList.add(nodes[buildingStream.readInt()]);
                }
                buildings[i] = new Building(nodeList, name, null);

                if (i % 1000000 == 0) {
                    System.out.println(i);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            System.out.println(fail);
        }
    }
}
