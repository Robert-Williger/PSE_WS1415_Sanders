import model.ByteSource;
import model.IByteSource;

public class QuadTreeTest {

    private static final long START_ADDRESS = 0;
    private final IByteSource source;

    public QuadTreeTest() {
        source = new ByteSource(21);
        source.putInt(0, 0);
        source.putInt(1, 1);
        source.putInt(5, 5);
        source.putInt(12, 12);
    }

    private void traverse(long id) {
        long level = id & 0b111111;
        long address = START_ADDRESS;

        id = id >> 6;
        for (int i = 0; i < level; i++) {
            int pointer = source.getInt(address);
            if (pointer > 0) { // not a child node
                address += pointer + id & 0b11;
                id = id >> 2;
            } else {
                System.out.println(-address);
            }
        }
    }

    public static long convert(int x, int y, final int zoom) {
        long ret = 0;
        for (int i = 0; i < zoom; i++) {
            int value = ((y % 2) << 1) | (x % 2);
            System.out.println(value);
            ret = (ret << 2) | value;
            x = x >> 1;
            y = y >> 1;
        }
        ret = ret << 6 | zoom;
        return ret;
    }

    public static void main(String[] args) {
        long convert = convert(1, 1, 2);
        long zoom = convert & 0b111111;
        System.out.println(zoom);
    }
}
