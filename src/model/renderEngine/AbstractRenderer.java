package model.renderEngine;

import java.awt.Image;

import model.AbstractModel;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.accessors.ITileAccessor;

public abstract class AbstractRenderer extends AbstractModel implements IRenderer {

    protected IPixelConverter converter;
    protected ITileAccessor tileAccessor;
    protected boolean rendered;

    public AbstractRenderer(final IMapManager manager) {
        setMapManager(manager);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        this.converter = manager.getState().getConverter();
        this.tileAccessor = manager.createTileAccessor();
    }

    // TODO describe default behaviour: returns false unless rendered is set to true by subclass
    @Override
    public final boolean render(final long tileID, final Image image) {
        rendered = false;
        tileAccessor.setID(tileID);
        render(image);
        return rendered;
    }

    protected abstract void render(final Image image);
}
