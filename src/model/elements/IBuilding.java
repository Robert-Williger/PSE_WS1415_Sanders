package model.elements;

public interface IBuilding extends IArea {

    String getAddress();

    String getStreet();

    String getHouseNumber();

    StreetNode getStreetNode();

}