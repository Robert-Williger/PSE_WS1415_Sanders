package model.map.accessors;

public class POIAccessor extends ElementAccessor implements IPointAccessor {

    protected final int[] data;
    private final int elementSize;

    public POIAccessor(final int[] distribution, final int[] data, final int elementSize) {
        super(distribution);
        this.data = data;
        this.elementSize = elementSize;
    }

    @Override
    public int getX() {
        return data[getAddress()];
    }

    @Override
    public int getY() {
        return data[getAddress() + 1];
    }

    protected int getAddress() {
        return elementSize * (int) getID();
    }

}
