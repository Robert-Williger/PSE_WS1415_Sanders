package adminTool.quadtree;

import java.io.IOException;
import java.util.PrimitiveIterator.OfInt;
import java.util.zip.ZipOutputStream;

import adminTool.AbstractMapFileWriter;
import util.IntList;

public class QuadtreeWriter extends AbstractMapFileWriter {

    private final IntList addresses;

    private final IQuadtree quadtree;
    private final String name;

    public QuadtreeWriter(final IQuadtree quadtree, final String name, final ZipOutputStream zipOutput) {
        super(zipOutput);
        this.name = name;
        this.quadtree = quadtree;
        this.addresses = new IntList();
    }

    @Override
    public void write() throws IOException {
        putNextEntry(name + "Tree");

        fillRec(quadtree);

        for (final OfInt iterator = addresses.iterator(); iterator.hasNext();) {
            dataOutput.writeInt(iterator.nextInt());
        }
        closeEntry();
    }

    private void fillRec(final IQuadtree quadtree) {
        final int startIndex = addresses.size();
        if (quadtree.isLeaf()) {
            addresses.add(-1);
            addresses.addAll(quadtree.getElements());
            // end of elements
            addresses.add(-1);
        } else {
            // allocate children pointer.
            for (int i = 0; i < 4; i++) {
                addresses.add(0);
            }
            addresses.addAll(quadtree.getElements());
            // end of elements
            addresses.add(-1);

            for (int i = 0; i < IQuadtree.NUM_CHILDREN; ++i) {
                final IQuadtree tree = quadtree.getChild(i);
                addresses.set(startIndex + i, addresses.size());
                fillRec(tree);
            }
        }
    }
}
