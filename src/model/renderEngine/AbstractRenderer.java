package model.renderEngine;

import model.AbstractModel;
import model.elements.dereferencers.ITileDereferencer;
import model.map.IMapManager;
import model.map.IPixelConverter;

public abstract class AbstractRenderer extends AbstractModel implements IRenderer {

    protected IPixelConverter converter;
    protected ITileDereferencer tile;

    protected int zoom;
    protected int x;
    protected int y;

    public AbstractRenderer(final IMapManager manager) {
        setMapManager(manager);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        this.converter = manager.getConverter();
        this.tile = manager.createTileDereferencer();
    }

    protected void setTileID(final long id) {
        tile.setID(id);
        this.zoom = tile.getZoomStep();
        this.x = tile.getX();
        this.y = tile.getY();
    }
}
