package model.map.accessors;

public class CollectiveAccessor extends ElementAccessor implements ICollectiveAccessor {

    protected final int[] data;
    private final int[] x;
    private final int[] y;

    public CollectiveAccessor(final int[] data, final int[] x, final int[] y, final int[] distribution) {
        super(distribution);
        this.data = data;
        this.x = x;
        this.y = y;
    }

    protected int getOffset() {
        return 0;
    }

    @Override
    public int getX(int index) {
        return x[data[getOffset() + getIntID() + index + 1]];
    }

    @Override
    public int getY(int index) {
        return y[data[getOffset() + getIntID() + index + 1]];
    }

    @Override
    public int size() {
        return data[getOffset() + getIntID()];
    }

}
