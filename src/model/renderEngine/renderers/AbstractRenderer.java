package model.renderEngine.renderers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Path2D;

import model.map.IMapManager;
import model.map.IPixelMapping;
import model.map.accessors.ICollectiveAccessor;
import model.map.accessors.ITileIdConversion;
import model.map.accessors.TileConversion;

abstract class AbstractRenderer implements IRenderer {

    protected IPixelMapping converter;
    protected ITileIdConversion conversion;
    protected IMapManager manager;

    @Override
    public void setMapManager(final IMapManager manager) {
        this.manager = manager;
        this.converter = manager.getPixelMapping();
        this.conversion = new TileConversion();
    }

    @Override
    public final boolean render(final long tileId, final Image image) {
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));

        final int row = conversion.getRow(tileId);
        final int column = conversion.getColumn(tileId);
        final int zoom = conversion.getZoom(tileId);
        final int coordTileSize = converter.getCoordDistance(manager.getTileState().getTileSize(), zoom);
        final int x = column * coordTileSize;
        final int y = row * coordTileSize;

        boolean ret = render(g, row, column, zoom, x, y);
        g.dispose();

        return ret;
    }

    protected void appendPath(final Path2D path, final ICollectiveAccessor accessor, final int x, final int y,
            final int zoom) {
        final int size = accessor.size();

        path.moveTo(converter.getPixelDistance(accessor.getX(0) - x, zoom),
                converter.getPixelDistance(accessor.getY(0) - y, zoom));

        for (int i = 1; i < size; i++) {
            path.lineTo(converter.getPixelDistance(accessor.getX(i) - x, zoom),
                    converter.getPixelDistance(accessor.getY(i) - y, zoom));
        }
    }

    protected abstract boolean render(final Graphics2D g, int row, int column, int zoom, int x, int y);
}
