package model.elements.dereferencers;

public interface IMultiElementDereferencer extends IDereferencer {

    int getType();

    int getX(int index);

    int getY(int index);

    int size();

}
