package model.map.accessors;

public class ElementAccessor extends Accessor implements IElementAccessor {
    private final int[] distribution;

    public ElementAccessor(final int[] distribution) {
        this.distribution = distribution;
    }

    @Override
    public int getAttribute(final String identifier) {
        return 0;
    }

    @Override
    public int getType() {
        // TODO implement binary search?
        int type = 0;

        while (getID() > distribution[type]) {
            ++type;
        }
        return type;
    }

    protected int getIntID() {
        return (int) getID();
    }
}
