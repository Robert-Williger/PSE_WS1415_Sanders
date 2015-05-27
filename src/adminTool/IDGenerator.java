package adminTool;

public class IDGenerator {

    // private int idCount;
    // private final int[][] ids;
    // private final int buckets;
    // private final int bucketLength;
    //
    // public IDGenerator(final long from, final long to) {
    // final long size = (to - from);
    // buckets = (int) ((size + Integer.MAX_VALUE - 6) / (Integer.MAX_VALUE -
    // 5));
    // bucketLength = (int) ((size + buckets - 1) / buckets);
    // System.out.println(buckets);
    // ids = new int[buckets][bucketLength];
    // }
    //
    // public void createID(long key) {
    // ids[(int) (key / bucketLength)][(int) (key % bucketLength)] = ++idCount;
    // if (idCount % 1000000 == 0) {
    // System.out.println(idCount);
    // }
    // }
    //
    // public int getId(long key) {
    // return ids[(int) (key / bucketLength)][(int) (key % bucketLength)];
    // }

    private final int[] ids;
    private final int[] values;
    private int idCount;
    private long misses;

    public IDGenerator(final int size) {
        ids = new int[4 * size];
        values = new int[4 * size];
    }

    public void createID(final long key) {
        int current = (int) (key % ids.length);
        while (ids[current] != 0) {
            current = (current + 1) % ids.length;
            ++misses;
        }

        ids[current] = (int) key;
        values[current] = ++idCount;

        if (idCount % 1000000 == 0) {
            System.out.println(misses / (double) idCount + " (" + idCount + ")");
        }
    }

    public int getId(final long key) {
        int current = (int) (key % ids.length);
        final int lookFor = (int) key;
        while (ids[current] != lookFor) {
            current = (current + 1) % ids.length;
        }

        return values[current];
    }
}