package model.renderEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.util.concurrent.ConcurrentLinkedQueue;

import model.AbstractModel;
import model.map.IMapManager;

public abstract class AbstractImageFetcher extends AbstractModel implements IImageFetcher {

    private final LRUCache cache;
    private final ConcurrentLinkedQueue<Image> freeList;
    private final GraphicsConfiguration config;

    private Image defaultImage;
    private final Dimension imageSize;

    public AbstractImageFetcher(final IMapManager manager) {
        imageSize = new Dimension();

        freeList = new ConcurrentLinkedQueue<Image>();
        cache = new LRUCache(getCacheSize(), true, freeList);

        config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        updateTileSize(manager.getTileSize());
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

        if (!manager.getTileSize().equals(imageSize)) {
            updateTileSize(manager.getTileSize());
        }
    }

    private void updateTileSize(final Dimension size) {
        freeList.clear();
        imageSize.setSize(size);

        defaultImage = getNewImage();

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < getCacheSize(); i++) {
                    // TODO is this an improvement?
                    // TODO BITMASK instead of TRANSCLUENT?
                    freeList.add(config.createCompatibleImage(imageSize.width, imageSize.height,
                            Transparency.TRANSLUCENT));
                }
            }

        }.start();
    }

    protected Image getNewImage() {
        Image ret;
        if (!freeList.isEmpty()) {
            ret = freeList.poll();
        } else {
            ret = config.createCompatibleImage(imageSize.width, imageSize.height, Transparency.TRANSLUCENT);
        }

        final Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imageSize.width, imageSize.height);
        g.dispose();

        return ret;
    }
}