package model.map.accessors;

public interface IElementAccessor extends IAccessor {

    int getType();

    int getAttribute(String identifier);

}
