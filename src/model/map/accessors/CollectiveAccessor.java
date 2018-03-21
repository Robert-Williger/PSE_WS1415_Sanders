package model.map.accessors;

public class CollectiveAccessor extends ElementAccessor implements ICollectiveAccessor {

    private final int[] addresses;
    protected final int[] data;
    private final int[] points;

    public CollectiveAccessor(final int[] distribution, final int[] data, final int[] addresses, final int[] points) {
        super(distribution);
        this.addresses = addresses;
        this.data = data;
        this.points = points;
    }

    protected int getOffset() {
        return 0;
    }

    @Override
    public int getX(final int index) {
        return points[data[getOffset() + getAddress() + index + 1] << 1];
    }

    @Override
    public int getY(final int index) {
        return points[(data[getOffset() + getAddress() + index + 1] << 1) + 1];
    }

    @Override
    public int size() {
        return data[getOffset() + getAddress()];
    }

    protected int getAddress() {
        return addresses[(int) getID()];
    }

}
