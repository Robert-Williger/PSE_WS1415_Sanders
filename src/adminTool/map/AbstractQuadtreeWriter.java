package adminTool.map;

import java.io.DataOutput;
import java.io.IOException;
import java.util.PrimitiveIterator;

import util.IntList;

public abstract class AbstractQuadtreeWriter extends CompressedWriter {
    private final DataOutput treeOutput;
    private final DataOutput elementOutput;
    private final int mapSize;
    private final int maxElementsPerTile;
    private final int maxZoomSteps;
    private final int[] addresses;

    public AbstractQuadtreeWriter(final int[] addresses, final DataOutput elementOutput, final DataOutput treeOutput,
            final int maxElementsPerTile, final int maxZoomSteps, final int mapSize) {
        this.elementOutput = elementOutput;
        this.treeOutput = treeOutput;
        this.maxElementsPerTile = maxElementsPerTile;
        this.maxZoomSteps = maxZoomSteps;
        this.mapSize = mapSize;
        this.addresses = addresses;
    }

    protected abstract boolean intersects(final int index, final int zoom, final int x, final int y, final int size);

    public void write() throws IOException {
        final boolean[] duplicates = new boolean[addresses.length];

        final int[][] xPos = new int[maxZoomSteps][4];
        final int[][] yPos = new int[maxZoomSteps][4];

        final boolean[][] leafs = createChildren();
        final IntList[][] indices = createIndices();

        final int[] choices = new int[maxZoomSteps];
        final int[] treeIndices = new int[maxZoomSteps];

        final IntList duplicateList = new IntList();
        final IntList dataList = new IntList();
        final IntList treeList = new IntList();

        int writtenDataInts = 0;
        int zoom = 0;

        allocateRoot(treeList, leafs[0]);

        while (zoom >= 0) {
            int choice = choices[zoom];

            if (leafs[zoom][choice]) {
                treeIndices[zoom] += registerChild(treeList, treeIndices[zoom], writtenDataInts);

                setLists(dataList, duplicateList, indices[zoom][choice], duplicates);

                zoom = updateStateByChild(choices, zoom);
            } else {
                treeIndices[zoom] += registerInnerNode(treeList, treeIndices[zoom], writtenDataInts);

                distribute(leafs[zoom + 1], dataList, duplicates, xPos, yPos, indices, mapSize >> (zoom + 1), zoom,
                        choice);

                setDuplicateList(duplicateList, duplicates, indices[zoom][choice]);
                setDuplactes(duplicates, dataList);
                setDataList(dataList);

                allocateChildren(treeIndices, treeList, leafs[zoom + 1], zoom);

                zoom = updateStateByInnerNode(choices, zoom);
            }
            writtenDataInts += writeNodeElements(dataList, duplicateList);
        }

        writeIntList(treeList, treeOutput);
    }

    private boolean[][] createChildren() {
        final boolean[][] children = new boolean[maxZoomSteps][4];
        for (int i = 0; i < 4; i++) {
            children[maxZoomSteps - 1][i] = true;
        }
        return children;
    }

    private IntList[][] createIndices() {
        final IntList[][] indices = new IntList[maxZoomSteps][];
        indices[0] = new IntList[]{createInitialList()};
        for (int i = 1; i < indices.length; i++) {
            indices[i] = new IntList[]{new IntList(), new IntList(), new IntList(), new IntList()};
        }

        return indices;
    }

    private IntList createInitialList() {
        final IntList indices = new IntList(addresses.length);
        for (int i = 0; i < addresses.length; i++) {
            indices.add(i);
        }
        return indices;
    }

    private void allocateRoot(final IntList treeList, final boolean[] leafs) {
        treeList.add(0);
        leafs[0] = maxZoomSteps == 1 || addresses.length <= maxElementsPerTile;
        if (!leafs[0]) {
            treeList.add(0);
        }
    }

    private void distribute(boolean[] leafs, IntList dataList, boolean[] duplicates, int[][] xPos, int[][] yPos,
            IntList[][] indices, int size, int zoom, int choice) {
        final int[] distXPos = xPos[zoom + 1];
        final int[] distYPos = yPos[zoom + 1];
        final IntList[] distIndices = indices[zoom + 1];

        setPositions(distXPos, distYPos, xPos[zoom][choice], yPos[zoom][choice], size);
        dataList.clear();
        clearIndices(distIndices);

        for (final PrimitiveIterator.OfInt iterator = indices[zoom][choice].iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();

            int matches = 0;
            for (int i = 0; i < 4; i++) {
                if (intersects(index, zoom, distXPos[i], distYPos[i], size)) {
                    distIndices[i].add(index);
                    ++matches;
                }
            }

            if (matches > 1 && !duplicates[index]) {
                dataList.add(index);
            }
        }

        if (zoom + 1 != maxZoomSteps - 1) {
            setLeafs(leafs, distIndices);
        }
    }

    private void setPositions(final int[] xPos, final int[] yPos, final int x, final int y, final int size) {
        for (int i = 0; i < 4; i++) {
            xPos[i] = x + (i % 2) * size;
            yPos[i] = y + (i / 2) * size;
        }
    }

    private void setLeafs(final boolean[] leafs, final IntList[] indices) {
        for (int i = 0; i < leafs.length; i++) {
            leafs[i] = indices[i].size() <= maxElementsPerTile;
        }
    }

    private void setDuplactes(final boolean[] duplicates, final IntList dataList) {
        for (final PrimitiveIterator.OfInt iterator = dataList.iterator(); iterator.hasNext();) {
            duplicates[iterator.nextInt()] = true;
        }
    }

    private void setDataList(final IntList dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            dataList.set(i, addresses[dataList.get(i)]);
        }
    }

    private void setLists(final IntList dataList, final IntList duplicateList, final IntList indices,
            final boolean[] duplicates) throws IOException {
        dataList.clear();
        duplicateList.clear();
        for (final PrimitiveIterator.OfInt iterator = indices.iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            (duplicates[index] ? duplicateList : dataList).add(addresses[index]);
        }
    }

    private void setDuplicateList(final IntList duplicateList, final boolean[] duplicates, final IntList indices)
            throws IOException {
        duplicateList.clear();

        for (final PrimitiveIterator.OfInt iterator = indices.iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            if (duplicates[index]) {
                duplicateList.add(addresses[index]);
            }
        }
    }

    private int updateStateByChild(final int[] choices, int zoom) {
        ++choices[zoom];
        while (choices[zoom] == 4) {
            if (--zoom == 0) {
                return -1;
            }
        }

        return zoom;
    }

    private int updateStateByInnerNode(final int[] choices, int zoom) {
        ++choices[zoom];
        choices[++zoom] = 0;

        return zoom;
    }

    private void allocateChildren(final int[] treeIndices, final IntList treeList, final boolean[] leafs, int zoom) {
        treeIndices[zoom + 1] = treeList.size();
        treeList.add(0);
        treeList.add(0);
        treeList.add(0);
        treeList.add(0);
        for (int i = 0; i < 4; i++) {
            if (!leafs[i]) {
                treeList.add(0);
            }
        }
    }

    private void clearIndices(final IntList[] indices) {
        for (final IntList list : indices) {
            list.clear();
        }
    }

    private int registerChild(final IntList treeData, final int treeIndex, final int writtenDataInts) {
        treeData.set(treeIndex, (writtenDataInts << 1) | 0);

        return 1;
    }

    private int registerInnerNode(final IntList treeList, final int treeIndex, final int writtenDataInts) {
        treeList.set(treeIndex, (writtenDataInts << 1) | 1);
        treeList.set(treeIndex + 1, treeList.size());

        return 2;
    }

    private int writeNodeElements(final IntList data, final IntList duplicates) throws IOException {
        elementOutput.writeInt((duplicates.size() << 16) | data.size());
        writeIntList(data, elementOutput);
        writeIntList(duplicates, elementOutput);
        return data.size() + duplicates.size() + 1;
    }

    private void writeIntList(final IntList list, final DataOutput output) throws IOException {
        for (final PrimitiveIterator.OfInt iterator = list.iterator(); iterator.hasNext();) {
            output.writeInt(iterator.nextInt());
        }
    }

}