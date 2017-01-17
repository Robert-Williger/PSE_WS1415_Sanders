package model.map.accessors;

public class BuildingAccessor extends CollectiveAccessor {

    public BuildingAccessor(final int[] x, final int[] y, final int[] data, final int[] distribution) {
        super(x, y, distribution, data);
    }

    @Override
    protected int getOffset() {
        return 2;
    }

    @Override
    public int getAttribute(final String identifier) {
        switch (identifier) {
            case "street":
                return data[getIntID()];
            case "number":
                return data[getIntID() + 1];
            default:
                return super.getAttribute(identifier);
        }
    }

}
