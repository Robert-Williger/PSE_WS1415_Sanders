package model.renderEngine.renderers;

import java.awt.Rectangle;
import java.util.Collection;

import util.FloatInterval;

public interface IRenderRoute {

    StreetUse getStreetUse(int id);

    FloatInterval getStreetPart(int id);

    Collection<FloatInterval> getStreetMultiPart(int id);

    int getLength();

    Rectangle getBounds();

}