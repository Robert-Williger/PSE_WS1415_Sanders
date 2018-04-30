package model.reader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import model.IProgressListener;
import model.IReader;
import model.ITextProcessor;
import model.map.IMapManager;
import model.routing.IRouteManager;

public class Reader implements IReader {
    private final ReaderContext readerContext;
    private final ProgressHandler progressHandler;

    private final MapManagerReader mapManagerReader;
    private final RouteManagerReader routeManagerReader;
    private final TextProcessorReader textProcessorReader;

    public Reader() {
        progressHandler = new ProgressHandler();
        readerContext = new ReaderContext(progressHandler);
        routeManagerReader = new RouteManagerReader();
        mapManagerReader = new MapManagerReader();
        textProcessorReader = new TextProcessorReader();
    }

    @Override
    public boolean read(final File file) {
        final long size = readerContext.open(file);
        if (size == -1) {
            progressHandler.fireErrorOccured("Beim Öffnen der Karte ist ein Fehler aufgetreten.");
            return false;
        }

        progressHandler.reset(size);
        try {
            progressHandler.fireStepCommenced("Lade Kartendaten...");
            mapManagerReader.readMapManager(readerContext);

            progressHandler.fireStepCommenced("Lade Graph...");
            routeManagerReader.readRouteManager(readerContext, mapManagerReader.getMapManager());

            progressHandler.fireStepCommenced("Lade Index...");
            textProcessorReader.readIndex(readerContext, mapManagerReader.getMapManager());
        } catch (final NullPointerException | IOException e2) {
            if (!progressHandler.isCanceled()) {
                progressHandler.fireErrorOccured("Beim Lesen der Karte ist ein Fehler aufgetreten.");
            }
            return false;
        } finally {
            readerContext.close();
        }

        return true;
    }

    @Override
    public IMapManager getMapManager() {
        return mapManagerReader.getMapManager();
    }

    @Override
    public IRouteManager getRouteManager() {
        return routeManagerReader.getRouteManager();
    }

    @Override
    public ITextProcessor getTextProcessor() {
        return textProcessorReader.getTextProcessor();
    }

    @Override
    public void cancelCalculation() {
        progressHandler.cancelCalculation();
        readerContext.close();
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        progressHandler.addProgressListener(listener);
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        progressHandler.removeProgressListener(listener);
    }

    public class ReaderContext {
        private ZipFile zipFile;
        private final ProgressHandler progressHandler;

        public ReaderContext(final ProgressHandler progressHandler) {
            this.progressHandler = progressHandler;
        }

        public long open(final File file) {
            try {
                zipFile = new ZipFile(file);
            } catch (final IOException e) {
                return -1;
            }
            return getTotalBytes();
        }

        public boolean close() {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException e) {
                    return false;
                }
            }
            return true;
        }

        private long getTotalBytes() {
            return zipFile.stream().mapToInt((e) -> (int) e.getSize()).sum();
        }

        public DataInputStream createInputStream(final String entryName) throws IOException {
            final ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            return createInputStream(entry);
        }

        public long getSize(final String entryName) {
            final ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null) {
                return -1;
            }
            return entry.getSize();
        }

        private DataInputStream createInputStream(final ZipEntry entry) throws IOException {
            return new DataInputStream(new BufferedInputStream(
                    new ProgressableInputStream(zipFile.getInputStream(entry), progressHandler)));
        }
    }

    static class ProgressableInputStream extends FilterInputStream {
        private final ProgressHandler progressHandler;

        public ProgressableInputStream(final InputStream inputStream, final ProgressHandler progressHandler)
                throws IOException {
            super(inputStream);
            this.progressHandler = progressHandler;
        }

        @Override
        public int read() throws IOException {
            read(1);

            return super.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            final int nr = super.read(b, off, len);

            read(nr);
            return nr;
        }

        @Override
        public int read(final byte[] b) throws IOException {
            final int nr = super.read(b);

            read(nr);

            return nr;
        }

        private void read(final int bytes) {
            final int progress = progressHandler.getProgress();
            progressHandler.add(bytes);
            if (progress != progressHandler.getProgress()) {
                progressHandler.fireProgressDone(progressHandler.getProgress());
            }
        }
    }
}