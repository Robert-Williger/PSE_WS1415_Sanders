package model.renderEngine;

import java.awt.Image;

import javax.swing.event.ChangeListener;

import model.AbstractModel;
import model.map.IMapManager;
import model.map.IMapState;
import model.map.IPixelConverter;

public class ImageAccessor extends AbstractModel implements IImageAccessor {

    private IMapManager mapManager;
    private IPixelConverter converter;
    private IMapState state;

    private final IImageFetcher imageFetcher;

    private boolean isVisible;

    public ImageAccessor(final IMapManager manager, final IImageFetcher fetcher) {
        setMapManager(manager);
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
        final long tileID = mapManager.getID(row, column, zoom);
        return imageFetcher.getImage(tileID);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
        state = manager.getState();
        converter = state.getConverter();
    }

    @Override
    public int getWidth() {
        return state.getPixelSectionWidth();
    }

    @Override
    public int getHeight() {
        return state.getPixelSectionHeight();
    }

    @Override
    public int getX(final int zoom) {
        // TODO Auto-generated method stub
        return (int) converter.getPixelDistance(state.getX(), zoom);
    }

    @Override
    public int getY(final int zoom) {
        // TODO Auto-generated method stub
        return (int) converter.getPixelDistance(state.getY(), zoom);
    }
}