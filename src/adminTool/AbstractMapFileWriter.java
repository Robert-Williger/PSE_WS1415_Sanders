package adminTool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class AbstractMapFileWriter {

    protected final ZipOutputStream zipOutput;
    protected final DataOutputStream dataOutput;

    public AbstractMapFileWriter(final ZipOutputStream zipOutput) {
        this.zipOutput = zipOutput;
        dataOutput = new DataOutputStream(zipOutput);
    }

    protected void putNextEntry(final String name) throws IOException {
        zipOutput.putNextEntry(new ZipEntry(name));
    }

    protected void closeEntry() throws IOException {
        zipOutput.closeEntry();
    }

    public abstract void write() throws IOException;

}