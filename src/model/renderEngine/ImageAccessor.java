package model.renderEngine;

import java.awt.Image;

import javax.swing.event.ChangeListener;

import model.AbstractModel;
import model.map.accessors.ITileConversion;

public class ImageAccessor extends AbstractModel implements IImageAccessor {

    private ITileConversion conversion;

    private final IImageFetcher imageFetcher;

    private boolean isVisible;

    public ImageAccessor(final ITileConversion conversion, final IImageFetcher fetcher) {
        setTileConversion(conversion);
        imageFetcher = fetcher;

        isVisible = true;
    }

    @Override
    public void removeChangeListener(final ChangeListener listener) {
        super.removeChangeListener(listener);
        imageFetcher.removeChangeListener(listener);
    }

    @Override
    public void addChangeListener(final ChangeListener listener) {
        super.addChangeListener(listener);
        imageFetcher.addChangeListener(listener);
    }

    @Override
    public void setVisible(final boolean visible) {
        if (isVisible != visible) {
            isVisible = visible;
            fireChange();
        }
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public Image getImage(final int row, final int column, final int zoom) {
        final long tileID = conversion.getId(row, column, zoom);
        return imageFetcher.getImage(tileID);
    }

    @Override
    public void setTileConversion(final ITileConversion conversion) {
        this.conversion = conversion;
    }

    @Override
    public void addTileListener(ITileListener listener) {
        imageFetcher.addTileListener(listener);
    }

    @Override
    public void removeTileListener(ITileListener listener) {
        imageFetcher.removeTileListener(listener);
    }

}