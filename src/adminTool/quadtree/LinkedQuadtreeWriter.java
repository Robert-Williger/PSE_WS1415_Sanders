package adminTool.quadtree;

import java.io.IOException;
import java.util.PrimitiveIterator;
import java.util.zip.ZipOutputStream;

import adminTool.AbstractMapFileWriter;
import util.IntList;

public class LinkedQuadtreeWriter extends AbstractMapFileWriter {
    private final int mapSize;
    private final int totalElements;
    private final String name;
    private final IQuadtreePolicy policy;

    public LinkedQuadtreeWriter(final IQuadtreePolicy policy, final ZipOutputStream zipOutput, final String name,
            final int totalElements, final int mapSize) {
        super(zipOutput);

        this.policy = policy;
        this.mapSize = mapSize;
        this.totalElements = totalElements;
        this.name = name;
    }

    @Override
    public void write() throws IOException {
        final boolean[] duplicates = new boolean[totalElements];

        final int maxZoomSteps = policy.getMaxZoomSteps();
        final int[][] xPos = new int[maxZoomSteps][4];
        final int[][] yPos = new int[maxZoomSteps][4];

        final boolean[][] leafs = createChildren();
        final IntList[][] indices = createIndices();

        final int[] choices = new int[maxZoomSteps];
        final int[] treeIndices = new int[maxZoomSteps];

        final IntList duplicateList = new IntList();
        final IntList dataList = new IntList();

        final IntList treeList = new IntList();

        putNextEntry(name + "Data");

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
        closeEntry();

        putNextEntry(name + "Tree");
        writeIntList(treeList);
        closeEntry();
    }

    private boolean[][] createChildren() {
        final boolean[][] children = new boolean[policy.getMaxZoomSteps()][4];
        for (int i = 0; i < 4; i++) {
            children[children.length - 1][i] = true;
        }
        return children;
    }

    private IntList[][] createIndices() {
        final IntList[][] indices = new IntList[policy.getMaxZoomSteps()][];
        indices[0] = new IntList[] { createInitialList() };
        for (int i = 1; i < indices.length; i++) {
            indices[i] = new IntList[] { new IntList(), new IntList(), new IntList(), new IntList() };
        }

        return indices;
    }

    private IntList createInitialList() {
        final IntList indices = new IntList(totalElements);
        for (int i = 0; i < totalElements; i++) {
            indices.add(i);
        }
        return indices;
    }

    private void allocateRoot(final IntList treeList, final boolean[] leafs) {
        treeList.add(0);
        leafs[0] = policy.getMaxZoomSteps() == 1 || totalElements <= policy.getMaxElementsPerTile();
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
                if (policy.intersects(index, zoom, distXPos[i], distYPos[i], size)) {
                    distIndices[i].add(index);
                    ++matches;
                }
            }

            if (matches > 1 && !duplicates[index]) {
                dataList.add(index);
            }
        }

        if (zoom + 1 != policy.getMaxZoomSteps() - 1) {
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
            leafs[i] = indices[i].size() <= policy.getMaxElementsPerTile();
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