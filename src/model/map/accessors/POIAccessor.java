package model.map.accessors;

public class POIAccessor extends ElementAccessor implements IPointAccessor {

    public POIAccessor(final int[] distribution, final int[] data) {
        this(new String[0], distribution, data);
    }

    public POIAccessor(final String[] attributes, final int[] distribution, final int[] data) {
        super(attributes, distribution, data);
    }

    @Override
    public int getX() {
        return data[address() + offset()];
    }

    @Override
    public int getY() {
        return data[address() + offset() + 1];
    }

    @Override
    protected int address() {
        return (offset() + 2) * (int) getID();
    }

}
