package model.renderEngine;

import java.awt.Rectangle;
import java.util.Collection;

public interface IRenderRoute {

    StreetUse getStreetUse(int id);

    Intervall getStreetPart(int id);

    Collection<Intervall> getStreetMultiPart(int id);

    int getLength();

    Rectangle getBounds();

}