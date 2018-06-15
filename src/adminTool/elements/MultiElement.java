package adminTool.elements;

import java.util.PrimitiveIterator;

import util.Arrays;

public class MultiElement implements Typeable {

    private final int[] indices;
    private int type;

    public MultiElement(final int[] indices, final int type) {
        this.indices = indices;
        this.type = type;
    }

    public int size() {
        return indices.length;
    }

    public int getNode(int index) {
        return indices[index];
    }

    @Override
    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public MultiElement subElement(final int from, final int to) {
        return new SubElement(indices, type, from, to);
    }

    public MultiElement reverse() {
        final int[] indices = new int[this.indices.length];
        System.arraycopy(this.indices, 0, indices, 0, indices.length);
        Arrays.reverse(indices);
        return new MultiElement(indices, type);
    }

    public PrimitiveIterator.OfInt iterator() {
        return Arrays.iterator(indices);
    }

    private static class SubElement extends MultiElement {
        private final int from;
        private final int size;

        public SubElement(final int[] indices, final int type, final int from, final int to) {
            super(indices, type);
            this.from = from;
            this.size = to - from;
        }

        public int size() {
            return size;
        }

        public int getNode(int index) {
            return super.getNode(index + from);
        }
    }

}