package model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CompressedInputStream extends DataInputStream {

    public CompressedInputStream(final InputStream in) {
        super(in);
    }

    public int readCompressedInt() throws IOException {
        int ret = 0;

        byte in;

        while (((in = readByte()) & 0x80) == 0) {
            ret = (ret << 7) | in;
        }

        in = (byte) (in & 0x7F);
        ret = (ret << 7) | in;

        return ret;
    }

}
