package model.map.accessors;

public class POIAccessor extends ElementAccessor implements IPointAccessor {

    protected final int[] data;

    public POIAccessor(final int[] distribution, final int[] data) {
        super(distribution);
        this.data = data;
    }

    @Override
    public int getX() {
        return data[getIntID()];
    }

    @Override
    public int getY() {
        return data[getIntID() + 1];
    }

}
