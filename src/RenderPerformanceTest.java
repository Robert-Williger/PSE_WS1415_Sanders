import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import model.IReader;
import model.Reader;
import model.map.IMapManager;
import model.renderEngine.AbstractImageFetcher;
import model.renderEngine.IImageFetcher;
import model.renderEngine.IRenderer;
import model.renderEngine.StorageBackgroundRenderer;

public class RenderPerformanceTest {

    private static final String RAW_DATA = "Performance.txt";
    private static final String TEST_DATA = "Test.txt";
    private static final int TEST_RUNS = 10;
    private static final int WORKER_COUNT = 20;

    public static void main(String[] args) {

        try {
            performTest();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static void performTest() throws IOException {
        final IReader reader = new Reader();

        System.out.println("Read map data");

        reader.read(new File("default.map"));
        final IMapManager manager = reader.getMapManager();
        final IImageFetcher fetcher = new ImageFetcher(new StorageBackgroundRenderer(manager.getConverter()), manager);

        System.out.println("Read test data");

        final long[] testData = readTestData();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Perform test");

        int timeCount = 0;
        for (int i = 0; i < TEST_RUNS; i++) {
            final long start = System.currentTimeMillis();

            for (final long tile : testData) {
                fetcher.loadImage(tile, Integer.MAX_VALUE);
            }
            timeCount += System.currentTimeMillis() - start;

            fetcher.flush();
        }
        System.out.println(timeCount / TEST_RUNS);
    }

    private static long[] readTestData() throws IOException {
        final DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                TEST_DATA))));
        final long[] tiles = new long[stream.readInt()];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = stream.readLong();
        }
        stream.close();

        return tiles;
    }

    private static void translateTestData() throws NumberFormatException, IOException {
        BufferedReader input = new BufferedReader(new FileReader(new File(RAW_DATA)));
        String line;
        final Set<Long> set = new TreeSet<Long>();

        while ((line = input.readLine()) != null) {
            set.add(Long.parseLong(line));
        }
        input.close();

        set.remove(-1L);

        final DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(
                TEST_DATA))));
        stream.writeInt(set.size());
        for (final long value : set) {
            stream.writeLong(value);
        }
        stream.close();
    }

    private static class ImageFetcher extends AbstractImageFetcher {

        public ImageFetcher(final IRenderer renderer, final IMapManager manager) {
            super(renderer, manager);
        }

        @Override
        protected int getCacheSize() {
            return 1024;
        }

        @Override
        protected int getWorkerThreads() {
            return WORKER_COUNT;
        }
    }

}
