package model.renderEngine;

import java.awt.Image;

import model.AbstractModel;
import model.map.IMapManager;
import model.map.IPixelConverter;
import model.map.accessors.ITileAccessor;

public abstract class AbstractRenderer extends AbstractModel implements IRenderer {

    protected IPixelConverter converter;
    protected ITileAccessor tileAccessor;;

    public AbstractRenderer(final IMapManager manager) {
        setMapManager(manager);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        this.converter = manager.getConverter();
        this.tileAccessor = manager.createTileAccessor();
    }

    @Override
    public final boolean render(final long tileID, final Image image) {
        tileAccessor.setID(tileID);
        return render(image);
    }

    protected abstract boolean render(final Image image);
}
