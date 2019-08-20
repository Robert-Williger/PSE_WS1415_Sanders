package model.map.accessors;

import java.util.Set;

public interface IElementAccessor extends IAccessor {

    int getType();

    int getAttribute(int attributeId);

    default int getAttribute(String attribute) {
        return getAttribute(getAttributeId(attribute));
    }

    Set<String> getAttributes();

    int getAttributeId(String attribute);

}
