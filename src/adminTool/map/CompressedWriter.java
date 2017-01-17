package adminTool.map;

import java.io.DataOutput;
import java.io.IOException;

public class CompressedWriter {
    // TODO
    protected int writeCompressedInt(final int value, final DataOutput stream) throws IOException {
        int ret = 1;
        int temp = value >>> 28;

        if (temp == 0) {
            temp = (value >> 21) & 0x7F;
            if (temp == 0) {
                temp = (value >> 14) & 0x7F;
                if (temp == 0) {
                    temp = (value >> 7) & 0x7F;
                    if (temp != 0) {
                        stream.write(temp);
                        ret = 2;
                    }
                } else {
                    stream.write(temp);
                    stream.write((value >> 7 & 0x7F));
                    ret = 3;
                }
            } else {
                stream.write(temp);
                stream.write((value >> 14) & 0x7F);
                stream.write((value >> 7) & 0x7F);
                ret = 4;
            }
        } else {
            stream.write(temp);
            stream.write((value >> 21) & 0x7F);
            stream.write((value >> 14) & 0x7F);
            stream.write((value >> 7) & 0x7F);
            ret = 5;
        }
        stream.write((value & 0x7F) | 0x80);

        return ret;
    }

}
