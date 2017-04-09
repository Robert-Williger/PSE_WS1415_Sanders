package model.map.accessors;

public class BuildingAccessor extends CollectiveAccessor {

    public BuildingAccessor(final int[] data, final int[] x, final int[] y, final int[] distribution) {
        super(data, x, y, distribution);
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
            // TODO case "name" for public buildings...
            default:
                return super.getAttribute(identifier);
        }
    }

}
