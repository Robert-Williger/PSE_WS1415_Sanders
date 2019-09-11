package model.map.accessors;

public class POIAccessor extends ElementAccessor implements IPointAccessor {

    private int id;

    public POIAccessor(final int[] distribution, final int[] data) {
        this(new String[0], distribution, data);
    }

    public POIAccessor(final String[] attributes, final int[] distribution, final int[] data) {
        super(attributes, distribution, data);
    }

    @Override
    public final int getX() {
        return data[address() + offset()];
    }

    @Override
    public final int getY() {
        return data[address() + offset() + 1];
    }

    @Override
    protected final int address() {
        return (offset() + 2) * (int) getId();
    }

    @Override
    public final void setId(int id) {
        this.id = id;
    }

    @Override
    public final int getId() {
        return id;
    }

}
