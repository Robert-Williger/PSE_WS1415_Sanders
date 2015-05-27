package model.renderEngine;

import model.map.IMapManager;

public class SlightlyCachedImageFetcher extends AbstractImageFetcher {

    public SlightlyCachedImageFetcher(IRenderer renderer, IMapManager manager) {
        super(renderer, manager);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected int getCacheSize() {
        return 512;
    }

}
