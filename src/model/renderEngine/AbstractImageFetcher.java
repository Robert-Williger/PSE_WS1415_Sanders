package model.renderEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

import model.AbstractModel;
import model.map.IMapManager;

public abstract class AbstractImageFetcher extends AbstractModel implements IImageFetcher {

    private final LRUCache cache;
    private final ConcurrentLinkedQueue<Image> freeList;
    // private final GraphicsConfiguration config;

    protected Image defaultImage;
    protected int imageSize;

    public AbstractImageFetcher(final IMapManager manager) {
        freeList = new ConcurrentLinkedQueue<>();
        cache = new LRUCache(getCacheSize(), true, freeList);

        // config =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        updateTileSize(manager.getState().getPixelTileSize());
    }

    protected abstract int getCacheSize();

    protected ICache getCache() {
        return cache;
    }

    protected void recycle(final Image image) {
        freeList.offer(image);
    }

    @Override
    public void flush() {
        cache.reset();
    }

    @Override
    public Image getImage(final long id) {
        if (!cache.contains(id)) {
            return defaultImage;
        }

        final Image img = cache.get(id);

        return img;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        flush();

        if (manager.getState().getPixelTileSize() != imageSize) {
            updateTileSize(manager.getState().getPixelTileSize());
        }
    }

    private void updateTileSize(final int size) {
        freeList.clear();
        imageSize = size;

        defaultImage = getNewImage();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < getCacheSize(); i++) {
                    // TODO is this an improvement?
                    // TODO BITMASK instead of TRANSCLUENT?
                    freeList.add(createImage());
                }
            }

        }.start();
    }

    protected Image getNewImage() {
        final Image ret = freeList.isEmpty() ? createImage() : freeList.poll();

        final Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imageSize, imageSize);
        g.dispose();

        return ret;
    }

    protected Image createImage() {
        // TODO improve this
        return new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        // return config.createCompatibleImage(imageSize, imageSize, Transparency.TRANSLUCENT);
        // return new SpecialImage(imageSize, imageSize);
    }
}