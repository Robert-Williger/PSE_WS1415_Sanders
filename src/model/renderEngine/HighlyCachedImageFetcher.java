package model.renderEngine;

import model.map.IMapManager;

public class HighlyCachedImageFetcher extends AbstractImageFetcher {

    public HighlyCachedImageFetcher(final IRenderer renderer, final IMapManager manager) {
        super(renderer, manager);
    }

    @Override
    protected int getCacheSize() {
        return 1024;
    }

}
