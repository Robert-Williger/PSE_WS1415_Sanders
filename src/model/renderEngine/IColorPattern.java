package model.renderEngine;

import model.map.IPixelConverter;

public interface IColorPattern {

    WayStyle getWayStyle(final int type);

    ShapeStyle getAreaStyle(final int type);

    ShapeStyle getBuildingStyle(final int type);

    void setConverter(final IPixelConverter converter);

}
