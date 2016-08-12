package model.elements.dereferencers;

import java.util.Iterator;

public interface ITileDereferencer {

    long getID();

    int getRow();

    int getColumn();

    int getX();

    int getY();

    int getZoomStep();

    boolean istEmpty();

    Iterator<Integer> getStreets();

    Iterator<Integer> getWays();

    Iterator<Integer> getBuildings();

    Iterator<Integer> getTerrain();

    Iterator<Integer> getPOIs();

    Iterator<Integer> getLabels();

    void setID(long id);

    IBuildingDereferencer getBuildingDereferencer();

    IAreaDereferencer getAreaDereferencer();

    IStreetDereferencer getStreetDereferencer();

    IWayDereferencer getWayDereferencer();

    IPOIDereferencer getPOIDereferencer();

    ILabelDereferencer getLabelDereferencer();
}
