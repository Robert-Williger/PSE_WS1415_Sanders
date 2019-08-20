package model.map.accessors;

public class CollectiveAccessor extends ElementAccessor implements ICollectiveAccessor {

    private final int[] addresses;

    public CollectiveAccessor(final String[] attributes, final int[] distribution, final int[] data,
            final int[] addresses) {
        super(attributes, distribution, data);
        this.addresses = addresses;
    }

    @Override
    public int getX(final int index) {
        return data[offset() + address() + (index << 1) + 1];
    }

    @Override
    public int getY(final int index) {
        return data[offset() + address() + (index << 1) + 1 + 1];
    }

    @Override
    public int size() {
        return data[offset() + address()];
    }

    protected int address() {
        return addresses[(int) getID()];
    }

}
