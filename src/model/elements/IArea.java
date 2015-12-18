package model.elements;

public interface IArea extends IMultiElement {

    int getType();

    boolean contains(int x, int y);

}