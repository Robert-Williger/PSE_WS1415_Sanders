package model.elements.dereferencers;

public interface IStreetDereferencer extends IWayDereferencer {

    int getLength();

    int getGraphID();

    boolean isOneWay();

}
