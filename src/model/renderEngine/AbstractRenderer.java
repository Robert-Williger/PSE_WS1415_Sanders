package model.renderEngine;

import java.awt.Image;
import java.awt.Point;

import model.AbstractModel;
import model.map.IPixelConverter;
import model.map.ITile;

public abstract class AbstractRenderer extends AbstractModel implements IRenderer {

    protected IPixelConverter converter;

    @Override
    public void setConverter(final IPixelConverter converter) {
        this.converter = converter;
    }

    protected Point getTileLocation(final ITile tile, final Image image) {
        final int zoom = tile.getZoomStep();
        return new Point(tile.getColumn() * converter.getCoordDistance(image.getWidth(null), zoom), tile.getRow()
                * converter.getCoordDistance(image.getHeight(null), zoom));
    }
}
