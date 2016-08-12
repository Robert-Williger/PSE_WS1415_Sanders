import model.ByteSource;
import model.IByteSource;

public class InnerNodedQuadTree {

    private static final long START_ADDRESS = 0;

    private final IByteSource treeData;
    private final IByteSource data;

    public InnerNodedQuadTree() {
        treeData = new ByteSource(47);
        // 0 (Root)
        treeData.putByte(0, (byte) 0b00010011);
        treeData.putByte(1, (byte) 3);
        treeData.putByte(2, (byte) 0);

        // 0 (Inner Node)
        treeData.putByte(3, (byte) 0b00000011);
        treeData.putByte(4, (byte) 13);
        treeData.putByte(5, (byte) 3);

        // 0.1 (Leaf)
        treeData.putByte(6, (byte) 0b00000001);
        treeData.putByte(7, (byte) 5);

        // 0.2 (Leaf)
        treeData.putByte(8, (byte) 0b00000001);
        treeData.putByte(9, (byte) 8);

        // 0.3 (Inner Node)
        treeData.putByte(10, (byte) 0b00000011);
        treeData.putByte(11, (byte) 22);
        treeData.putByte(12, (byte) 12);

        // 0.0.0 (Leaf)
        treeData.putByte(13, (byte) 0b00000001);
        treeData.putByte(14, (byte) 16);

        // 0.0.1 (Leaf)
        treeData.putByte(15, (byte) 0b00000001);
        treeData.putByte(16, (byte) 20);

        // 0.0.2 (Leaf)
        treeData.putByte(17, (byte) 0b00000001);
        treeData.putByte(18, (byte) 23);

        // 0.0.3 (Inner Node)
        treeData.putByte(19, (byte) 0b00000011);
        treeData.putByte(20, (byte) 31);
        treeData.putByte(21, (byte) 25);

        // 0.3.0 (Leaf)
        treeData.putByte(22, (byte) 0b00000001);
        treeData.putByte(23, (byte) 27);

        // 0.3.1 (Leaf)
        treeData.putByte(24, (byte) 0b00000001);
        treeData.putByte(25, (byte) 30);

        // 0.3.2 (Leaf)
        treeData.putByte(26, (byte) 0b00000001);
        treeData.putByte(27, (byte) 34);

        // 0.3.3 (Inner Node)
        treeData.putByte(28, (byte) 0b00000011);
        treeData.putByte(29, (byte) 39);
        treeData.putByte(30, (byte) 38);

        // 0.0.3.0 (Leaf)
        treeData.putByte(31, (byte) 0b00000001);
        treeData.putByte(32, (byte) 41);

        // 0.0.3.1 (Leaf)
        treeData.putByte(33, (byte) 0b00000001);
        treeData.putByte(34, (byte) 43);

        // 0.0.3.2 (Leaf)
        treeData.putByte(35, (byte) 0b00000001);
        treeData.putByte(36, (byte) 45);

        // 0.0.3.3 (Leaf)
        treeData.putByte(37, (byte) 0b00000001);
        treeData.putByte(38, (byte) 47);

        // 0.3.3.0 (Leaf)
        treeData.putByte(39, (byte) 0b00000001);
        treeData.putByte(40, (byte) 51);

        // 0.3.3.1 (Leaf)
        treeData.putByte(41, (byte) 0b00000001);
        treeData.putByte(42, (byte) 54);

        // 0.3.3.2 (Leaf)
        treeData.putByte(43, (byte) 0b00000001);
        treeData.putByte(44, (byte) 58);

        // 0.3.3.3 (Leaf)
        treeData.putByte(45, (byte) 0b00000001);
        treeData.putByte(46, (byte) 62);

        data = new ByteSource(67);
        // TODO compress
        data.putByte(0, (byte) -1);
        data.putByte(1, (byte) 1);
        data.putByte(2, (byte) 2);
        data.putByte(3, (byte) -1);

        data.putByte(4, (byte) 1);
        data.putByte(5, (byte) -1);

        data.putByte(6, (byte) 2);
        data.putByte(7, (byte) -1);

        data.putByte(8, (byte) 9);
        data.putByte(9, (byte) -1);
        data.putByte(10, (byte) 2);
        data.putByte(11, (byte) -1);

        data.putByte(12, (byte) 10);
        data.putByte(13, (byte) -1);
        data.putByte(14, (byte) 1);
        data.putByte(15, (byte) -1);

        data.putByte(16, (byte) 3);
        data.putByte(17, (byte) 4);
        data.putByte(18, (byte) -1);
        data.putByte(19, (byte) -1);

        data.putByte(20, (byte) 5);
        data.putByte(21, (byte) 6);
        data.putByte(22, (byte) -1);
        data.putByte(23, (byte) -1);

        data.putByte(24, (byte) 1);
        data.putByte(25, (byte) -1);

        data.putByte(26, (byte) 1);
        data.putByte(27, (byte) -1);

        data.putByte(28, (byte) 10);
        data.putByte(29, (byte) -1);

        data.putByte(30, (byte) 11);
        data.putByte(31, (byte) -1);
        data.putByte(32, (byte) 10);
        data.putByte(33, (byte) -1);

        data.putByte(34, (byte) 12);
        data.putByte(35, (byte) -1);
        data.putByte(36, (byte) 1);
        data.putByte(37, (byte) -1);

        data.putByte(38, (byte) 13);
        data.putByte(39, (byte) -1);
        data.putByte(40, (byte) 1);
        data.putByte(41, (byte) -1);

        data.putByte(42, (byte) 1);
        data.putByte(43, (byte) -1);

        data.putByte(44, (byte) 1);
        data.putByte(45, (byte) -1);

        data.putByte(46, (byte) 1);
        data.putByte(47, (byte) -1);

        data.putByte(48, (byte) 7);
        data.putByte(49, (byte) 8);
        data.putByte(50, (byte) -1);
        data.putByte(51, (byte) -1);

        data.putByte(52, (byte) 1);
        data.putByte(53, (byte) 13);
        data.putByte(54, (byte) -1);

        data.putByte(55, (byte) 1);
        data.putByte(56, (byte) 13);
        data.putByte(57, (byte) -1);

        data.putByte(58, (byte) 14);
        data.putByte(59, (byte) -1);
        data.putByte(60, (byte) 13);
        data.putByte(61, (byte) -1);

        data.putByte(62, (byte) 15);
        data.putByte(63, (byte) 16);
        data.putByte(64, (byte) 17);
        data.putByte(65, (byte) -1);
        data.putByte(66, (byte) -1);
    }

    public int getHeight() {
        return 3;
    }

    public long getAddress(final byte[] choices) {
        long address = START_ADDRESS;

        int addressLength = 1;

        for (final int choice : choices) {
            int headerByte = treeData.getByte(address);

            headerByte >>= 1;
            int newAddressLength = headerByte & 0b111;
            if (newAddressLength == 0) { // leaf check
                return address;
            }

            headerByte >>= 3;
            address = readAddress(address + 1, addressLength);
            addressLength = newAddressLength;

            for (int c = 0; c < choice; c++) {
                final int offset = headerByte & 1;
                address += offset * addressLength + addressLength + 1;
                headerByte >>= 1;
            }
        }
        return address;
    }

    public CellIterator cellIterator(final long address, final int addressLength, final int height) {
        return new CellIterator(address, addressLength, height);
    }

    public ElementIterator elementIterator(final long address, final int addressLength, final int height) {
        return new ElementIterator(address, addressLength, height);
    }

    private class CellIterator {
        private final long[] headerAddresses;
        private final int[] choices;
        private final int[] headerBytes;
        private final int[] addressLengths;

        private long headerAddress;
        private long dataAddress;
        private int choice;
        private int addressLength;
        private int headerByte;

        private boolean leaf;
        private long remainingCount;
        private int fillLevel;

        public CellIterator(final long address, final int addressLength, final int height) {
            this.headerAddresses = new long[height];
            this.choices = new int[height];
            this.headerBytes = new int[height];
            this.addressLengths = new int[height];
            this.headerAddress = address;
            this.addressLength = addressLength;
            this.remainingCount = 1;
            this.choice = isLeaf(headerAddress) ? 3 : 0;

            nextDataCell();
            // leaf = isLeaf(headerAddress);
            // dataAddress = headerAddress + (leaf ? 1 : addressLength + 1);
        }

        public boolean hasNext() {
            return remainingCount != 0;
        }

        public long next() {
            long next = readAddress(dataAddress, addressLength);

            if (leaf) {
                --remainingCount;

                if (++choice == 4) {
                    if (!hasNext()) {
                        return next;
                    }
                    restore();
                }

                int offset = headerByte & 1;
                headerAddress += offset * addressLength + 1 + addressLength;
                headerByte >>= 1;
                offset = headerByte & 1;
                dataAddress = headerAddress + offset * addressLength + 1;

                nextDataCell();
            } else { // Inner Node
                nextDataCell(treeData.getByte(headerAddress) & 0b11111110);
            }

            return next;
        }

        private void nextDataCell() {
            nextDataCell(treeData.getByte(headerAddress));
        }

        private void nextDataCell(int header) {
            while ((header & 1) != 1) { // data check
                ++choice;
                store();
                choice = 0;

                headerByte = header;
                headerByte >>= 1;
                addressLength = headerByte & 0b111;
                headerByte >>= 3;
                headerAddress = readAddress(headerAddress + 1, addressLength);
                remainingCount += 3;

                header = treeData.getByte(headerAddress);
            }

            leaf = isLeaf(headerAddress);
            dataAddress = headerAddress + (leaf ? 1 : addressLength + 1);
        }

        private void store() {
            headerAddresses[fillLevel] = headerAddress;
            choices[fillLevel] = choice;
            headerBytes[fillLevel] = headerByte;
            addressLengths[fillLevel] = addressLength;
            ++fillLevel;
        }

        private void restore() {
            --fillLevel;
            while (choices[fillLevel] == 4) {
                --fillLevel;
            }
            headerAddress = headerAddresses[fillLevel];
            headerByte = headerBytes[fillLevel];
            addressLength = addressLengths[fillLevel];
            choice = choices[fillLevel];
        }
    }

    private class ElementIterator {
        private final CellIterator cellIterator;
        private long nextAddress;
        private boolean skip;

        public ElementIterator(final long address, final int addressLength, final int height) {
            cellIterator = cellIterator(address, addressLength, height);
            nextAddress = cellIterator.next();

            loadNext();
        }

        public boolean hasNext() {
            return nextAddress != -1;
        }

        public int next() {
            int value = data.getByte(nextAddress);
            ++nextAddress;
            loadNext();

            return value;
        }

        private void loadNext() {
            int nextValue = data.getByte(nextAddress);
            while (nextValue == -1) {
                if (skip) {
                    if (cellIterator.hasNext()) {
                        nextAddress = cellIterator.next();
                    } else {
                        nextAddress = -1;
                        return;
                    }
                } else {
                    skip = true;
                    ++nextAddress;
                }

                nextValue = data.getByte(nextAddress);
            }
        }
    }

    private long readAddress(final long address, final int bytes) {
        long ret = 0;

        for (int i = 0; i < bytes; i++) {
            ret = (ret << 8) | treeData.getUnsignedByte(address + i);
        }

        return ret;
    }

    private boolean isLeaf(final long address) {
        return (treeData.getByte(address) & 0b1110) == 0;
    }

    public static void main(final String[] args) {
        InnerNodedQuadTree tree = new InnerNodedQuadTree();
        final byte[] choices = new byte[]{};
        final long address = tree.getAddress(choices);
        System.out.println("Content for node at address " + address + ": ");
        // final CellIterator iterator = tree.cellIterator(address, 1, 3);
        // while (iterator.hasNext()) {
        // System.out.println(iterator.next());
        // }
        final ElementIterator iterator = tree.elementIterator(address, 1, 3);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
