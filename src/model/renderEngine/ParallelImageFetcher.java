package model.renderEngine;

import java.awt.Image;

import model.IFactory;
import model.map.IMapManager;

public class ParallelImageFetcher extends AbstractImageFetcher implements IImageFetcher {

    private RenderPool threadPool;
    private IRenderer[] renderers;

    public ParallelImageFetcher(final IMapManager manager, final IFactory<IRenderer> factory) {
        super(manager);

        renderers = new IRenderer[getWorkerThreads()];
        for (int i = 0; i < renderers.length; i++) {
            renderers[i] = factory.create();
        }

        threadPool = new RenderPool();

        setMapManager(manager);
    }

    protected int getWorkerThreads() {
        return 20;
    }

    protected int getCacheSize() {
        return 1024;
    }

    @Override
    public void flush() {
        super.flush();
        threadPool.flush();
    }

    @Override
    public void loadImage(final long id, final int priority) {

        if (id != -1 && !getCache().contains(id)) {
            if (threadPool.contains(id)) {
                // TODO improve this
                threadPool.changeKey(new RenderJob(id, null), priority);
            } else {
                threadPool.add(new RenderJob(id, getNewImage()), priority);
            }
        }

    }

    @Override
    public void setMapManager(final IMapManager manager) {
        super.setMapManager(manager);
        for (final IRenderer renderer : renderers) {
            renderer.setMapManager(manager);
        }
    }

    private class RenderJob extends ThreadJobTest<Boolean> {

        private final Image image;
        private boolean result;

        public RenderJob(final long tileID, final Image image) {
            super(tileID);
            this.image = image;
        }

        @Override
        protected Boolean getResult() {
            return result;
        }

        public void setResult(final boolean result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "RenderJob [tile=" + getID() + ", image=" + image + "]";
        }
    }

    class RenderPool extends ThreadPoolTest<Boolean, RenderJob> {
        private int count;

        public RenderPool() {
            super(getWorkerThreads());
        }

        @Override
        protected void processResult(final RenderJob job) {
            if (job.getResult()) {
                getCache().put(job.getID(), job.image);
                fireChange();
            } else {
                recycle(job.image);
            }
        }

        @Override
        protected Worker createWorker() {
            return new Worker() {

                private final IRenderer renderer;

                {
                    this.renderer = renderers[count++];
                }

                @Override
                protected void work(RenderJob job) {
                    boolean result = renderer.render(job.getID(), job.image);
                    job.setResult(result);
                }
            };
        }
    }

}