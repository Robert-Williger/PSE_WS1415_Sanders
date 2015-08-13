package model.renderEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

import model.map.IMapManager;
import model.map.ITile;

public abstract class AbstractImageFetcher implements IImageFetcher {

    private IMapManager mapManager;
    private final IRenderer renderer;
    protected final LRUCache cache;
    private final RenderPool threadPool;
    private final ConcurrentLinkedQueue<Image> freeList;

    private Image defaultImage;
    private final Dimension imageSize;

    public AbstractImageFetcher(final IRenderer renderer, final IMapManager manager) {
        this.renderer = renderer;
        mapManager = manager;

        imageSize = new Dimension();

        freeList = new ConcurrentLinkedQueue<Image>();
        cache = new LRUCache(getCacheSize(), true, freeList);
        threadPool = new RenderPool(getWorkerThreads());

        setMapManager(manager);
    }

    protected int getWorkerThreads() {
        return 20;
    }

    protected abstract int getCacheSize();

    @Override
    public void flush() {
        threadPool.flush();
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
    public void loadImage(final long id, final int priority) {

        final ITile tile = mapManager.getTile(id);

        if (tile.getID() != -1) {
            final RenderJob job = new RenderJob(tile, defaultImage);
            if (threadPool.contains(job)) {
                threadPool.changeKey(job, priority);
            } else if (!cache.contains(id)) {
                threadPool.add(new RenderJob(tile, getNewImage()), priority);
            }
        }

    }

    @Override
    public IRenderer getRenderer() {
        return renderer;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        mapManager = manager;

        flush();

        if (!manager.getTileSize().equals(imageSize)) {
            freeList.clear();
            imageSize.setSize(manager.getTileSize());

            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < getCacheSize(); i++) {
                        final BufferedImage img = new BufferedImage(imageSize.width, imageSize.height,
                                BufferedImage.TYPE_INT_ARGB);
                        freeList.add(img);
                    }
                }

            }.start();

            defaultImage = getNewImage();
            final Graphics g = defaultImage.getGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, imageSize.width, imageSize.height);

            g.dispose();
        }
    }

    private Image getNewImage() {
        if (!freeList.isEmpty()) {
            final Image img = freeList.poll();

            final Graphics2D g = (Graphics2D) img.getGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, imageSize.width, imageSize.height);
            g.dispose();

            return img;
        } else {
            return new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private class RenderJob extends ThreadJob<Boolean> {
        private final ITile tile;
        private final Image image;

        public RenderJob(final ITile tile, final Image image) {
            super(tile.getID());
            this.tile = tile;
            this.image = image;
        }

        @Override
        protected Boolean work() {
            return renderer.render(tile, image);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((tile == null) ? 0 : ((int) (tile.getID() ^ (tile.getID() >>> 32))));
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RenderJob other = (RenderJob) obj;
            if (image == null) {
                if (other.image != null) {
                    return false;
                }
            }
            if (tile == null) {
                if (other.tile != null) {
                    return false;
                }
            } else if (tile.getID() != other.tile.getID()) {
                return false;
            }
            return true;
        }
    }

    private class RenderPool extends ThreadPool<Boolean, RenderJob> {

        public RenderPool(final int threadCount) {
            super(threadCount);
        }

        @Override
        protected void processResult(final RenderJob job, final Boolean result) {
            if (result) {
                cache.put(job.getID(), job.image);
            } else {
                freeList.add(job.image);
            }
        }
    }
}