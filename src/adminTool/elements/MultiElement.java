package adminTool.elements;

import java.util.PrimitiveIterator;

import util.IntList;

public class MultiElement implements Typeable {

    private final IntList indices;
    private int type;

    public MultiElement(final MultiElement element, final int type) {
        this.indices = element.indices;
        this.type = type;
    }

    public MultiElement(final IntList indices, final int type) {
        this.indices = indices;
        this.type = type;
    }

    public IntList toList() {
        return new IntList(indices);
    }

    public int size() {
        return indices.size();
    }

    public int getPoint(int index) {
        return indices.get(index);
    }

    @Override
    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public MultiElement reverse() {
        final IntList list = new IntList(indices);
        list.reverse();
        return new MultiElement(list, type);
    }

    public PrimitiveIterator.OfInt iterator() {
        return indices.iterator();
    }

}