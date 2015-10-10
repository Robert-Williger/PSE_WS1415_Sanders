package adminTool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class AbstractMapCreator {

    private final File file;
    protected DataOutputStream stream;

    public AbstractMapCreator(final File file) {
        this.file = file;
    }

    protected void createOutputStream(final boolean append) throws FileNotFoundException {
        stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, append)));
    }

    protected void writeCompressedInt(final int value) throws IOException {
        int temp = value >>> 28;

        if (temp == 0) {
            temp = (value >> 21) & 0x7F;
            if (temp == 0) {
                temp = (value >> 14) & 0x7F;
                if (temp == 0) {
                    temp = (value >> 7) & 0x7F;
                    if (temp != 0) {
                        stream.write(temp);
                    }
                } else {
                    stream.write(temp);
                    stream.write((value >> 7 & 0x7F));
                }
            } else {
                stream.write(temp);
                stream.write((value >> 14) & 0x7F);
                stream.write((value >> 7) & 0x7F);
            }
        } else {
            stream.write(temp);
            stream.write((value >> 21) & 0x7F);
            stream.write((value >> 14) & 0x7F);
            stream.write((value >> 7) & 0x7F);
        }
        stream.write((value & 0x7F) | 0x80);
    }

    public abstract void create();

}