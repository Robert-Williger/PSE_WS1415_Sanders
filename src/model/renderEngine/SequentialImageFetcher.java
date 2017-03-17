package model.renderEngine;

import java.awt.Image;

import util.AddressableBinaryHeap;
import model.map.IMapManager;

public class SequentialImageFetcher extends AbstractImageFetcher {
    private final AddressableBinaryHeap<Long> queue;
    private final IRenderer renderer;

    public SequentialImageFetcher(final IRenderer renderer, final IMapManager manager) {
        super(manager);

        this.renderer = renderer;
        this.queue = new AddressableBinaryHeap<>();
        new Worker().start();
    }

    @Override
    protected int getCacheSize() {
        return 128;
    }

    @Override
    public void setMapManager(final IMapManager manager) {
        renderer.setMapManager(manager);
    }

    @Override
    public void loadImage(final long id, final int priority) {
        if (id != -1 && !getCache().contains(id)) {
            synchronized (queue) {
                if (!queue.contains(id)) {
                    queue.insert(id);
                } else {
                    queue.changeKey(id, priority);
                }
                queue.notifyAll();
            }
        }
    }

    @Override
    public void flush() {
        super.flush();
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.deleteMin();
            }
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            interrupt();
                            return;
                        }
                    }
                    final long id = queue.deleteMin();
                    final Image image = getNewImage();

                    if (renderer.render(id, image)) {
                        getCache().put(id, image);
                    }
                }
            }
        }
    }
}
