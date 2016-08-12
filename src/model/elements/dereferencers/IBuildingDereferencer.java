package model.elements.dereferencers;

public interface IBuildingDereferencer extends IAreaDereferencer {

    String getAddress();

    String getStreet();

    String getHouseNumber();

    int getAccessPoint();

    boolean hasAccessPoint();

    IAccessPointDereferencer getAccessPointDereferencer();

}
