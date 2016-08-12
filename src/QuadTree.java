import model.ByteSource;
import model.IByteSource;

public class QuadTree {

    private static final long START_ADDRESS = 0;
    private final IByteSource treeStructure;

    public QuadTree() {
        treeStructure = new ByteSource(42);
        // 0 (Root)
        treeStructure.putByte(0, (byte) 0b00000010);
        treeStructure.putByte(1, (byte) 2);

        // 0 (Inner Node)
        treeStructure.putByte(2, (byte) 0b00000010);
        treeStructure.putByte(3, (byte) 10);

        // 0.1 (Leaf)
        treeStructure.putByte(4, (byte) 0b00000001);
        treeStructure.putByte(5, (byte) 0);

        // 0.2 (Leaf)
        treeStructure.putByte(6, (byte) 0b00000001);
        treeStructure.putByte(7, (byte) 1);

        // 0.3 (Inner Node)
        treeStructure.putByte(8, (byte) 0b00000010);
        treeStructure.putByte(9, (byte) 18);

        // 0.0.0 (Leaf)
        treeStructure.putByte(10, (byte) 0b00000001);
        treeStructure.putByte(11, (byte) 2);

        // 0.0.1 (Leaf)
        treeStructure.putByte(12, (byte) 0b00000001);
        treeStructure.putByte(13, (byte) 3);

        // 0.0.2 (Leaf)
        treeStructure.putByte(14, (byte) 0b00000001);
        treeStructure.putByte(15, (byte) 4);

        // 0.0.3 (Inner Node)
        treeStructure.putByte(16, (byte) 0b00000010);
        treeStructure.putByte(17, (byte) 26);

        // 0.3.0 (Leaf)
        treeStructure.putByte(18, (byte) 0b00000001);
        treeStructure.putByte(19, (byte) 5);

        // 0.3.1 (Leaf)
        treeStructure.putByte(20, (byte) 0b00000001);
        treeStructure.putByte(21, (byte) 6);

        // 0.3.2 (Leaf)
        treeStructure.putByte(22, (byte) 0b00000001);
        treeStructure.putByte(23, (byte) 7);

        // 0.3.3 (Inner Node)
        treeStructure.putByte(24, (byte) 0b00000010);
        treeStructure.putByte(25, (byte) 34);

        // 0.0.3.0 (Leaf)
        treeStructure.putByte(26, (byte) 0b00000001);
        treeStructure.putByte(27, (byte) 8);

        // 0.0.3.1 (Leaf)
        treeStructure.putByte(28, (byte) 0b00000001);
        treeStructure.putByte(29, (byte) 9);

        // 0.0.3.2 (Leaf)
        treeStructure.putByte(30, (byte) 0b00000001);
        treeStructure.putByte(31, (byte) 10);

        // 0.0.3.3 (Leaf)
        treeStructure.putByte(32, (byte) 0b00000001);
        treeStructure.putByte(33, (byte) 11);

        // 0.3.3.0 (Leaf)
        treeStructure.putByte(34, (byte) 0b00000001);
        treeStructure.putByte(35, (byte) 12);

        // 0.3.3.1 (Leaf)
        treeStructure.putByte(36, (byte) 0b00000001);
        treeStructure.putByte(37, (byte) 13);

        // 0.3.3.2 (Leaf)
        treeStructure.putByte(38, (byte) 0b00000001);
        treeStructure.putByte(39, (byte) 14);

        // 0.3.3.3 (Leaf)
        treeStructure.putByte(40, (byte) 0b00000001);
        treeStructure.putByte(41, (byte) 15);
    }

    public int getHeight() {
        return 3;
    }

    public long getAddress(final byte[] choices) {
        long address = START_ADDRESS;

        int addressLength = 1;

        for (final int choice : choices) {
            int headerByte = treeStructure.getByte(address);

            if ((headerByte & 1) == 1) { // leaf check
                return address;
            }

            address = readAddress(address + 1, addressLength);

            headerByte >>= 1;
            addressLength = headerByte & 0b111;
            headerByte >>= 3;
            for (int c = 0; c < choice; c++) {
                final int offset = headerByte & 1;
                address += offset * addressLength + addressLength + 1;
                headerByte >>= 1;
            }
        }
        return address;
    }

    public void traverse(long address) {
        int addressLength = 1;

        int headerByte = treeStructure.getByte(address);

        if ((headerByte & 1) == 1) { // leaf check
            System.out.println(readAddress(address + 1, addressLength));
            return;
        }

        address = readAddress(address + 1, addressLength);

        headerByte >>= 1;
        addressLength = headerByte & 0b111;
        headerByte >>= 3;

        traverse(address);
        for (int c = 0; c < 3; c++) {
            final int offset = headerByte & 1;
            address += offset * addressLength + addressLength + 1;
            traverse(address);
            headerByte >>= 1;
        }
    }

    public ZellIterator iterator(final long address, final int addressLength, final int height) {
        return new ZellIterator(address, addressLength, height);
    }

    private class ZellIterator {
        private final long[] addresses;
        private final int[] choices;
        private final int[] addressLengths;
        private final int[] headerBytes;

        private long address;
        private int choice;
        private int addressLength;
        private int headerByte;

        private int fillLevel;
        private int remainingCount;

        public ZellIterator(final long address, final int addressLength, final int height) {
            this.addresses = new long[height];
            this.choices = new int[height];
            this.addressLengths = new int[height];
            this.headerBytes = new int[height];
            this.address = address;
            this.addressLength = addressLength;
        }

        public boolean hasNext() {
            return remainingCount != -1;
        }

        private void restore() {
            --fillLevel;
            while (choices[fillLevel] == 4) {
                --fillLevel;
            }
            address = addresses[fillLevel];
            headerByte = headerBytes[fillLevel];
            addressLength = addressLengths[fillLevel];
            choice = choices[fillLevel];
        }

        private void store() {
            addresses[fillLevel] = address;
            choices[fillLevel] = choice;
            addressLengths[fillLevel] = addressLength;
            headerBytes[fillLevel] = headerByte;
            ++fillLevel;
        }

        private void applyHeader(final int header) {
            headerByte = header;
            headerByte >>= 1;
            addressLength = headerByte & 0b111;
            headerByte >>= 3;

            address = readAddress(address + 1, addressLength);
        }

        public long next() {
            int header = treeStructure.getByte(address);

            while ((header & 1) != 1) { // leaf check
                ++choice;

                store();
                choice = 0;

                applyHeader(header);
                remainingCount += 3;

                header = treeStructure.getByte(address);
            }

            long next = readAddress(address + 1, addressLength);
            --remainingCount;

            if (hasNext()) {
                if (++choice == 4) {
                    restore();
                }

                int offset = headerByte & 1;
                address += offset * addressLength + addressLength + 1;
                headerByte >>= 1;
            }

            return next;
        }
    }

    private long readAddress(final long address, final int bytes) {
        long ret = 0;

        for (int i = 0; i < bytes; i++) {
            ret = (ret << 8) | treeStructure.getUnsignedByte(address + i);
        }

        return ret;
    }

    // public static long convert(final byte[] choices) {
    // long ret = 0;
    // for (int i = choices.length - 1; i >= 0; i--) {
    // ret = (ret << 2) | choices[i];
    // }
    // ret <<= 6;
    // ret |= choices.length;
    //
    // return ret;
    // }

    public static void main(final String[] args) {
        QuadTree tree = new QuadTree();
        final byte[] choices = new byte[]{};
        // final long id = convert(choices);
        final long address = tree.getAddress(choices);
        System.out.println("Content for node at address " + address + ": ");
        // tree.traverse(address);
        final ZellIterator iterator = tree.iterator(address, 1, 3);
        while (iterator.hasNext()) {
            // iterator.next();
            System.out.println(iterator.next());
        }
    }
}
