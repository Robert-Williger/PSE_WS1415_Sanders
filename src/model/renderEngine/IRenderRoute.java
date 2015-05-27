package model.renderEngine;

import java.awt.Rectangle;
import java.util.Collection;

import model.targets.IPointList;

public interface IRenderRoute {

    StreetUse getStreetUse(long id);

    Intervall getStreetPart(long id);

    Collection<Intervall> getStreetMultiPart(long id);

    int getLength();

    Rectangle getBounds();

    IPointList getPointList();

}