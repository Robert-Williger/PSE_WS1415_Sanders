package adminTool.quadtree;

import java.io.IOException;
import java.util.PrimitiveIterator;
import java.util.zip.ZipOutputStream;

import adminTool.AbstractMapFileWriter;
import util.IntList;

public class LinkedQuadtreeWriter extends AbstractMapFileWriter {
    private final int mapSize;
    private final int elements;
    private final int maxHeight;
    private final int maxElementsPerTile;
    private final String name;
    private final IQuadtreePolicy policy;

    public LinkedQuadtreeWriter(final IQuadtreePolicy policy, final ZipOutputStream zipOutput, final String name,
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
        final boolean[] duplicates = new boolean[elements];

        final int[][] xPos = new int[maxHeight][4];
        final int[][] yPos = new int[maxHeight][4];

        final boolean[][] leafs = createChildren();
        final IntList[][] indices = createIndices();

        final int[] choices = new int[maxHeight];
        final int[] treeIndices = new int[maxHeight];

        final IntList duplicateList = new IntList();
        final IntList dataList = new IntList();

        final IntList treeList = new IntList();

        putNextEntry(name + "Data");

        int writtenDataInts = 0;
        int height = 0;

        allocateRoot(treeList, leafs[0]);

        while (height >= 0) {
            int choice = choices[height];

            if (leafs[height][choice]) {
                treeIndices[height] += registerChild(treeList, treeIndices[height], writtenDataInts);

                setLists(dataList, duplicateList, indices[height][choice], duplicates);

                height = updateStateByChild(choices, height);
            } else {
                treeIndices[height] += registerInnerNode(treeList, treeIndices[height], writtenDataInts);

                distribute(leafs[height + 1], dataList, duplicates, xPos, yPos, indices, mapSize >> (height + 1),
                        height, choice);

                setDuplicateList(duplicateList, duplicates, indices[height][choice]);
                setDuplactes(duplicates, dataList);
                setDataList(dataList);

                allocateChildren(treeIndices, treeList, leafs[height + 1], height);

                height = updateStateByInnerNode(choices, height);
            }
            writtenDataInts += writeNodeElements(dataList, duplicateList);
        }
        closeEntry();

        putNextEntry(name + "Tree");
        writeIntList(treeList);
        closeEntry();
    }

    private boolean[][] createChildren() {
        final boolean[][] children = new boolean[maxHeight][4];
        for (int i = 0; i < 4; i++) {
            children[children.length - 1][i] = true;
        }
        return children;
    }

    private IntList[][] createIndices() {
        final IntList[][] indices = new IntList[maxHeight][];
        indices[0] = new IntList[] { createInitialList() };
        for (int i = 1; i < indices.length; i++) {
            indices[i] = new IntList[] { new IntList(), new IntList(), new IntList(), new IntList() };
        }

        return indices;
    }

    private IntList createInitialList() {
        final IntList indices = new IntList(elements);
        for (int i = 0; i < elements; i++) {
            indices.add(i);
        }
        return indices;
    }

    private void allocateRoot(final IntList treeList, final boolean[] leafs) {
        treeList.add(0);
        leafs[0] = maxHeight == 1 || elements <= maxElementsPerTile;
        if (!leafs[0]) {
            treeList.add(0);
        }
    }

    private void distribute(boolean[] leafs, IntList dataList, boolean[] duplicates, int[][] xPos, int[][] yPos,
            IntList[][] indices, int size, int height, int choice) {
        final int[] distXPos = xPos[height + 1];
        final int[] distYPos = yPos[height + 1];
        final IntList[] distIndices = indices[height + 1];

        setPositions(distXPos, distYPos, xPos[height][choice], yPos[height][choice], size);
        dataList.clear();
        clearIndices(distIndices);

        for (final PrimitiveIterator.OfInt iterator = indices[height][choice].iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();

            int matches = 0;
            for (int i = 0; i < 4; i++) {
                if (policy.intersects(index, height, distXPos[i], distYPos[i], size)) {
                    distIndices[i].add(index);
                    ++matches;
                }
            }

            if (matches > 1 && !duplicates[index]) {
                dataList.add(index);
            }
        }

        if (height + 1 != maxHeight - 1) {
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
            dataList.set(i, dataList.get(i));
        }
    }

    private void setLists(final IntList dataList, final IntList duplicateList, final IntList indices,
            final boolean[] duplicates) {
        dataList.clear();
        duplicateList.clear();
        for (final PrimitiveIterator.OfInt iterator = indices.iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            (duplicates[index] ? duplicateList : dataList).add(index);
        }
    }

    private void setDuplicateList(final IntList duplicateList, final boolean[] duplicates, final IntList indices) {
        duplicateList.clear();

        for (final PrimitiveIterator.OfInt iterator = indices.iterator(); iterator.hasNext();) {
            final int index = iterator.nextInt();
            if (duplicates[index]) {
                duplicateList.add(index);
            }
        }
    }

    private int updateStateByChild(final int[] choices, int height) {
        ++choices[height];
        while (choices[height] == 4) {
            if (--height == 0) {
                return -1;
            }
        }

        return height;
    }

    private int updateStateByInnerNode(final int[] choices, int height) {
        ++choices[height];
        choices[++height] = 0;

        return height;
    }

    private void allocateChildren(final int[] treeIndices, final IntList treeList, final boolean[] leafs, int height) {
        treeIndices[height + 1] = treeList.size();
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
        dataOutput.writeInt((duplicates.size() << 16) | data.size());
        writeIntList(data);
        writeIntList(duplicates);
        return data.size() + duplicates.size() + 1;
    }

    private void writeIntList(final IntList list) throws IOException {
        for (final PrimitiveIterator.OfInt iterator = list.iterator(); iterator.hasNext();) {
            dataOutput.writeInt(iterator.nextInt());
        }
    }

}