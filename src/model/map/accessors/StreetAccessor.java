package model.map.accessors;

import model.map.accessors.CollectiveAccessor;

public class StreetAccessor extends CollectiveAccessor {

    public StreetAccessor(final int[] distribution, final int[] data, final int[] addresses) {
        super(distribution, data, addresses);
    }

    @Override
    public int getAttribute(final String identifier) {
        switch (identifier) {
            case "name":
                return data[getAddress() + 1];
            case "graphId":
                // TODO how to use getID() here?
                return data[getAddress()] & 0x7FFFFFFF;
            case "oneway":
                return data[getAddress()] >>> 31;
            case "length":
                return CollectiveUtil.getLength(this, getID());
            default:
                return super.getAttribute(identifier);
        }
    }

    protected int getOffset() {
        return 2;
    }

}
