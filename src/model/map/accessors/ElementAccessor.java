package model.map.accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ElementAccessor extends Accessor implements IElementAccessor {
    private final int[] distribution;
    protected final int[] data;
    private final Map<String, Integer> attributeMap;

    public ElementAccessor(final int[] distribution, final int[] data) {
        this(new String[0], distribution, data);
    }

    public ElementAccessor(final String[] attributes, final int[] distribution, final int[] data) {
        this.distribution = distribution;
        this.data = data;
        attributeMap = new HashMap<String, Integer>();
        for (int id = 0; id < attributes.length; ++id) {
            attributeMap.put(attributes[id], id);
        }
    }

    protected abstract int address();

    @Override
    public int getType() {
        // TODO implement binary search?
        int type = 0;

        // TODO >= ?
        while (getID() >= distribution[type]) {
            ++type;
        }
        return type;
    }

    @Override
    public int getAttribute(int identifier) {
        return data[address() + identifier];
    }

    @Override
    public Set<String> getAttributes() {
        return attributeMap.keySet();
    }

    @Override
    public int getAttributeId(String attribute) {
        return attributeMap.get(attribute);
    }

    protected int offset() {
        return attributeMap.size();
    }
}
