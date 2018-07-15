package adminTool.elements;

import java.util.PrimitiveIterator;

import util.IntList;

public class MultiElement implements Typeable {

    private final IntList indices;
    private int type;

    public MultiElement(final IntList indices, final int type) {
        this.indices = indices;
        this.type = type;
    }

    public int size() {
        return indices.size();
    }

    public int getNode(int index) {
        return indices.get(index);
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
        final IntList list = new IntList(indices);
        list.reverse();
        return new MultiElement(list, type);
    }

    public PrimitiveIterator.OfInt iterator() {
        return indices.iterator();
    }

    private static class SubElement extends MultiElement {
        private final int from;
        private final int size;

        public SubElement(final IntList indices, final int type, final int from, final int to) {
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