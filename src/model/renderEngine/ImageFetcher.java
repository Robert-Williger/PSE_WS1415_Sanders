package model.renderEngine;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import model.AbstractModel;
import model.map.IMapManager;
import model.map.accessors.ITileConversion;
import model.renderEngine.renderers.IRenderer;
import model.renderEngine.threadPool.ThreadPool;

public class ImageFetcher extends AbstractModel implements IImageFetcher {
    private static final GraphicsConfiguration config;

    private final IRenderer renderer;
    private final ThreadPool pool;
    private final LRUCache<Long, Image> cache;
    private final ConcurrentLinkedQueue<Image> freeList;
    private final List<ITileListener> listeners;

    private ITileConversion conversion;

    private int imageSize;
    private int run;

    static {
        config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    public ImageFetcher(final IRenderer renderer, final IMapManager manager, final ThreadPool pool,
            final int cacheSize) {
        this.pool = pool;
        this.renderer = renderer;
        this.freeList = new ConcurrentLinkedQueue<>();
        this.cache = new LRUCache<>(cacheSize, false, freeList);
        this.listeners = new ArrayList<>();

        setMapManager(manager);
        // updateTileSize(manager.getState().getPixelTileSize());
    }

    private void recycle(final Image image) {
        freeList.offer(image);
    }

    @Override
    public void flush() {
        ++run;
        cache.clear();
    }

    @Override
    public Image getImage(final long id) {
        return cache.get(id);
        // return img == null ? defaultImage : img;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        flush();

        conversion = manager.getTileConversion();
        renderer.setMapManager(manager);
        if (manager.getState().getPixelTileSize() != imageSize) {
            updateTileSize(manager.getState().getPixelTileSize());
        }
    }

    private void updateTileSize(final int size) {
        freeList.clear();
        imageSize = size;

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < cache.getSize(); i++) {
                    freeList.add(createImage());
                }
            }

        }.start();
    }

    private Image newImage() {
        Image ret = freeList.poll();
        return ret != null ? ret : createImage();
    }

    protected Image createImage() {
        return config.createCompatibleImage(imageSize, imageSize, Transparency.TRANSLUCENT);
    }

    @Override
    public void loadImage(final long id, final int priority) {
        if (id != -1 && !cache.contains(id)) {
            pool.add(new RenderJob(id, run), priority);
        }
    }

    protected void fireTileRendered(Image img, int row, int column, int zoom) {
        for (final ITileListener listener : listeners) {
            listener.rendered(img, row, column, zoom);
        }
    }

    private class RenderJob implements Runnable {
        private final long tileId;
        private final int run;

        public RenderJob(long tileId, int run) {
            super();
            this.tileId = tileId;
            this.run = run;
        }

        @Override
        public void run() {
            if (run != ImageFetcher.this.run)
                return;
            Image img = newImage();
            if (renderer.render(tileId, img)) {
                SwingUtilities.invokeLater(() -> {
                    if (run == ImageFetcher.this.run) { // rendering process can take some time..
                        cache.put(tileId, img);
                        fireTileRendered(img, conversion.getRow(tileId), conversion.getColumn(tileId),
                                conversion.getZoom(tileId));
                        fireChange();
                    }
                });
            } else {
                recycle(img);
            }
        }
    }

    @Override
    public void addTileListener(ITileListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTileListener(ITileListener listener) {
        listeners.remove(listener);
    }

}
