package model.map.accessors;

public class BuildingAccessor extends CollectiveAccessor {

    public BuildingAccessor(final int[] distribution, final int[] data, final int[] addresses, final int[] points) {
        super(distribution, data, addresses, points);
    }

    @Override
    protected int getOffset() {
        return 3;
    }

    @Override
    public int getAttribute(final String identifier) {
        switch (identifier) {
            case "street":
                return data[getAddress()];
            case "number":
                return data[getAddress() + 1];
            case "name":
                return data[getAddress() + 2];
            default:
                return super.getAttribute(identifier);
        }
    }

}
