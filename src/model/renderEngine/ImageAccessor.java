package model.renderEngine;

import java.awt.Image;

import javax.swing.event.ChangeListener;

import model.AbstractModel;
import model.map.IMapManager;

public class ImageAccessor extends AbstractModel implements IImageAccessor {

    private IMapManager mapManager;
    private final IImageFetcher imageFetcher;

    private boolean isVisible;

    public ImageAccessor(final IMapManager manager, final IImageFetcher fetcher) {
        mapManager = manager;
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
    public int getRows() {
        return mapManager.getVisibleRows();
    }

    @Override
    public int getColumns() {
        return mapManager.getVisibleColumns();
    }

    @Override
    public Image getImage(final int row, final int column) {
        final int zoom = mapManager.getState().getZoomStep();
        final long tileID = mapManager.getID(row + mapManager.getRow(), column + mapManager.getColumn(), zoom);
        return imageFetcher.getImage(tileID);
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;
    }

}