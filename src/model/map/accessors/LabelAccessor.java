package model.map.accessors;

public class LabelAccessor extends POIAccessor {

    public LabelAccessor(final int[] distribution, final int[] data) {
        super(distribution, data);
    }

    @Override
    public int getAttribute(final String identifier) {System.out.println(identifier);
        if (identifier.equals("name")) {
            return data[getIntID() + 2];
        }

        return super.getAttribute(identifier);
    }
}
