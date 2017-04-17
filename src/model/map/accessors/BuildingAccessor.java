package model.map.accessors;

public class BuildingAccessor extends CollectiveAccessor {

    public BuildingAccessor(final int[] data, final int[] x, final int[] y, final int[] distribution) {
        super(data, x, y, distribution);
    }

    @Override
    protected int getOffset() {
        return 3;
    }

    @Override
    public int getAttribute(final String identifier) {
        switch (identifier) {
            case "street":
                return data[getIntID()];
            case "number":
                return data[getIntID() + 1];
            case "name":
                return data[getIntID() + 2];
            default:
                return super.getAttribute(identifier);
        }
    }

}
