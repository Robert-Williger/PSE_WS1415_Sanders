package model.map;

import model.IFactory;
import model.map.accessors.CollectiveAccessor;
import model.map.accessors.ICollectiveAccessor;

public class CollectiveAccessorFactory implements IFactory<ICollectiveAccessor> {
    protected final int[] x;
    protected final int[] y;
    protected final int[] distribution;
    protected int[] data;

    public CollectiveAccessorFactory(final int[] xPoints, final int[] yPoints, final int[] distribution) {
        this.distribution = distribution;
        this.x = xPoints;
        this.y = yPoints;
    }

    public void setData(final int[] data) {
        this.data = data;
    }

    @Override
    public ICollectiveAccessor create() {
        return new CollectiveAccessor(data, x, y, distribution);
    }
}
