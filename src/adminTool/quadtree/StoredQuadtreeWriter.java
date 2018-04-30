package adminTool.quadtree;

import java.io.IOException;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.zip.ZipOutputStream;

import adminTool.AbstractMapFileWriter;
import util.IntList;

public class StoredQuadtreeWriter extends AbstractMapFileWriter {
    private final int mapSize;
    private final int elements;
    private final String name;
    private final int maxHeight;
    private final int maxElementsPerTile;
    private final IQuadtreePolicy policy;

    public StoredQuadtreeWriter(final IQuadtreePolicy policy, final ZipOutputStream zipOutput, final String name,
            final int elements, final int maxHeight, final int maxElementsPerTile, final int mapSize) {
        super(zipOutput);

        this.policy = policy;
        this.mapSize = mapSize;
        this.elements = elements;
        this.maxHeight = maxHeight;
        this.maxElementsPerTile = maxElementsPerTile;
        this.name = name;
    }

    @Override
    public void write() throws IOException {
        final IntList data = new IntList();
        distribute(data, createElements(), 0, 0, 0);

        putNextEntry(name + "Tree");

        for (final OfInt iterator = data.iterator(); iterator.hasNext();) {
            dataOutput.writeInt(iterator.nextInt());
        }
        closeEntry();
    }

    private IntList[] createElements() {
        final IntList[] elements = new IntList[maxHeight];
        elements[0] = createInitialList();
        for (int i = 1; i < elements.length; i++) {
            elements[i] = new IntList();
        }

        return elements;
    }

    private IntList createInitialList() {
        final IntList indices = new IntList(elements);
        for (int i = 0; i < elements; i++) {
            indices.add(i);
        }
        return indices;
    }

    private void distribute(final IntList data, final IntList[] elements, final int x, final int y, final int height)
            throws IOException {
        final IntList cElements = elements[height];
        final int startIndex = data.size();
        final int nHeight = height + 1;
        // allocate children pointer.
        for (int i = 0; i < 4; i++) {
            data.add(-1);
        }
        data.addAll(cElements);
        // end of elements
        data.add(-1);

        if (cElements.size() > maxElementsPerTile && nHeight < maxHeight) {
            final int nSize = mapSize >> nHeight;
            final IntList nElements = elements[nHeight];

            for (int i = 0; i < 4; i++) {
                final int nx = x + (i % 2) * nSize;
                final int ny = y + (i / 2) * nSize;
                nElements.clear();
                for (final PrimitiveIterator.OfInt iterator = cElements.iterator(); iterator.hasNext();) {
                    final int element = iterator.nextInt();
                    if (policy.intersects(element, nHeight, nx, ny, nSize)) {
                        nElements.add(element);
                    }
                }
                if (!nElements.isEmpty()) {
                    data.set(startIndex + i, data.size());
                    distribute(data, elements, nx, ny, nHeight);
                }
            }
        }
    }
}