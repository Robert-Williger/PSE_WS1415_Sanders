package model;

public interface IByteSource {

    byte getByte(long address);

    int getUnsignedByte(long address);

    void putByte(long address, byte value);

    short getShort(long address);

    int getUnsignedShort(long address);

    void putShort(long address, short value);

    int getInt(long address);

    long getUnsignedInt(long address);

    void putInt(long address, int value);

    long getLong(long address);

    void putLong(long address, long value);
}
