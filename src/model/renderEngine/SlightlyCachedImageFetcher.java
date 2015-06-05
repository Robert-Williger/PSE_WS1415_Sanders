package model.renderEngine;

import model.map.IMapManager;

public class SlightlyCachedImageFetcher extends AbstractImageFetcher {

    public SlightlyCachedImageFetcher(final IRenderer renderer, final IMapManager manager) {
        super(renderer, manager);
    }

    @Override
    protected int getCacheSize() {
        return 128;
    }

}
