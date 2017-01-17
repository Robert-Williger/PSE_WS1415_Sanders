package model.map;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class QuadtreeTest2 {

    private static IQuadtree readQuadtree(final String path, final String element) throws IOException {
        int[] elementData = readIntArray(path + "/" + element + "Data");
        int[] treeData = readIntArray(path + "/" + element + "Tree");
        return new Quadtree(treeData, elementData, 7);
    }

    private static int[] readIntArray(final String path) throws IOException {
        final DataInputStream reader = createInputStream(path);

        final int[] ret = new int[reader.available() / 4];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = reader.readInt();
        }

        reader.close();

        return ret;
    }

    private static DataInputStream createInputStream(final String path) throws IOException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(new File(path))));
    }

    public static void main(String[] args) {
        IQuadtree tree = null;
        try {
            tree = readQuadtree(new File("quadtree").getAbsolutePath(), "way");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tree != null) {
            for (final Iterator<Long> iterator = tree.iterator(0, 0, 7); iterator.hasNext();) {
                System.out.println(iterator.next());
            }
        }
    }
}
