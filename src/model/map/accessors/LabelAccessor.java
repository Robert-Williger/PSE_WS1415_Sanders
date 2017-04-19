package model.map.accessors;

public class LabelAccessor extends POIAccessor {

    public LabelAccessor(final int[] distribution, final int[] data, final int elementSize) {
        super(distribution, data, elementSize);
    }

    @Override
    public int getAttribute(final String identifier) {
        if (identifier.equals("name")) {
            return data[getAddress() + 2];
        }

        return super.getAttribute(identifier);
    }
}
