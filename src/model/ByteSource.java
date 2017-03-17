package model;

public class ByteSource implements IByteSource {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private final byte[][]   data;

    public ByteSource(long size) {
        data = new byte[(int) ((size + MAX_ARRAY_SIZE - 1) / MAX_ARRAY_SIZE)][];
        for (int i = 0; i < data.length - 1; i++) {
            data[i] = new byte[MAX_ARRAY_SIZE];
            size -= MAX_ARRAY_SIZE;
        }
        data[data.length - 1] = new byte[(int) (size % MAX_ARRAY_SIZE)];
    }

    public ByteSource(final byte[] data) {
        this(new byte[][]{data});
    }

    public ByteSource(final byte[][] data) {
        this.data = data;
    }

    @Override
    public byte getByte(final long adress) {
        return data[(int) (adress / MAX_ARRAY_SIZE)][(int) (adress % MAX_ARRAY_SIZE)];
    }

    @Override
    public int getUnsignedByte(final long adress) {
        return Byte.toUnsignedInt(data[(int) (adress / MAX_ARRAY_SIZE)][(int) (adress % MAX_ARRAY_SIZE)]);
    }

    @Override
    public void putByte(final long adress, final byte value) {
        data[(int) (adress / MAX_ARRAY_SIZE)][(int) (adress % MAX_ARRAY_SIZE)] = value;
    }

    @Override
    public short getShort(final long adress) {
        return (short) getUnsignedShort(adress);
    }

    @Override
    public int getUnsignedShort(final long adress) {
        int byte1 = getUnsignedByte(adress);
        int byte2 = getUnsignedByte(adress + 1);
        return ((byte1 << 8) | (byte2 << 0));
    }

    @Override
    public void putShort(final long adress, final short value) {
        putByte(adress, (byte) ((value >>> 8) & 255));
        putByte(adress + 1, (byte) (value & 255));
    }

    @Override
    public int getInt(final long adress) {
        int byte1 = getUnsignedByte(adress);
        int byte2 = getUnsignedByte(adress + 1);
        int byte3 = getUnsignedByte(adress + 2);
        int byte4 = getUnsignedByte(adress + 3);
        return ((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | (byte4 << 0));
    }

    @Override
    public long getUnsignedInt(final long adress) {
        return Integer.toUnsignedLong(getInt(adress));
    }

    public int getCompressedInt(long adress) {
        int ret = 0;

        byte in;

        while (((in = getByte(adress++)) & 0x80) == 0) {
            ret = (ret << 7) | in;
        }

        in = (byte) (in & 0x7F);
        ret = (ret << 7) | in;

        return ret;
    }

    @Override
    public void putInt(final long adress, final int value) {
        putShort(adress, (short) ((value >>> 16) & 0xFFFF));
        putShort(adress + 2, (short) (value & 0xFFFF));
    }

    public void putCompressedInt(long adress, final int value) {
        int temp = value >>> 28;

        if (temp == 0) {
            temp = (value >> 21) & 0x7F;
            if (temp == 0) {
                temp = (value >> 14) & 0x7F;
                if (temp == 0) {
                    temp = (value >> 7) & 0x7F;
                    if (temp != 0) {
                        putByte(adress++, (byte) temp);
                    }
                } else {
                    putByte(adress++, (byte) temp);
                    putByte(adress++, (byte) (value >> 7 & 0x7F));
                }
            } else {
                putByte(adress++, (byte) temp);
                putByte(adress++, (byte) ((value >> 14) & 0x7F));
                putByte(adress++, (byte) ((value >> 7) & 0x7F));
            }
        } else {
            putByte(adress++, (byte) temp);
            putByte(adress++, (byte) ((value >> 21) & 0x7F));
            putByte(adress++, (byte) ((value >> 14) & 0x7F));
            putByte(adress++, (byte) ((value >> 7) & 0x7F));
        }
        putByte(adress, (byte) ((value & 0x7F) | 0x80));
    }

    @Override
    public long getLong(final long adress) {
        long int1 = getUnsignedInt(adress);
        long int2 = getUnsignedInt(adress + 4);
        return (int1 << 32) | int2;
    }

    @Override
    public void putLong(final long adress, final long value) {
        putInt(adress, (int) ((value >>> 32) & 0xFFFFFFFF));
        putInt(adress + 4, (int) (value & 0xFFFFFFFF));
    }
}
