package model.renderEngine;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import util.ByteList;
import util.ShortList;

public class RunLengthEncoding {

    private static final int BYTE_MAX_VALUE = 1 << Byte.SIZE;

    private final ShortList colors;
    private final ByteList runs;
    private int[] colorDict;
    private final HashMap<Integer, Short> colorMap;
    private short colorId;

    public RunLengthEncoding() {
        colors = new ShortList();
        runs = new ByteList();
        colorMap = new HashMap<>();
    }

    public void encode(final BufferedImage image) {
        colors.clear();
        runs.clear();
        colorMap.clear();
        colorId = 0;

        short last = -1;
        int runLength = 0;
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                short current = colorId(image.getRGB(x, y));
                if (current == last && runLength < BYTE_MAX_VALUE)
                    ++runLength;
                else {
                    runs.add((byte) runLength);
                    colors.add(last);
                    last = current;
                    runLength = 1;
                }
            }
        }

        colorDict = new int[colorMap.size()];
        for (final Map.Entry<Integer, Short> entry : colorMap.entrySet()) {
            colorDict[entry.getValue()] = entry.getKey();
        }
    }

    private short colorId(final int rgb) {
        Short id = colorMap.get(rgb);
        if (id == null) {
            colorMap.put(rgb, colorId);
            return colorId++;
        }

        return id;
    }

    public short[] colors() {
        return colors.toArray();
    }

    public byte[] runs() {
        return runs.toArray();
    }

    public int[] colorDict() {
        return colorDict;
    }
}
