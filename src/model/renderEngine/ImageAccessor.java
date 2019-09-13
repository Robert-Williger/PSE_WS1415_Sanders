package model.renderEngine;

import java.awt.Image;

import javax.swing.event.ChangeListener;

import model.AbstractModel;
import model.map.accessors.ITileIdConversion;

public class ImageAccessor extends AbstractModel implements IImageAccessor {

    private final ITileIdConversion conversion;
    private final IImageFetcher imageFetcher;
    private final String name;

    private boolean isVisible;

    public ImageAccessor(final ITileIdConversion conversion, final IImageFetcher imageFetcher, final String name) {
        this.conversion = conversion;
        this.imageFetcher = imageFetcher;
        this.name = name;
        this.isVisible = true;
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
    public String getName() {
        return name;
    }

    @Override
    public Image getImage(final int row, final int column, final int zoom) {
        final long tileID = conversion.getId(row, column, zoom);
        return imageFetcher.getImage(tileID);
    }

}