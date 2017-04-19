package model.map;

import model.IFactory;
import model.map.accessors.CollectiveAccessor;
import model.map.accessors.ICollectiveAccessor;

public class CollectiveAccessorFactory implements IFactory<ICollectiveAccessor> {
    protected final int[] x;
    protected final int[] y;
    protected final int[] distribution;
    protected final int[] addresses;
    protected int[] data;

    public CollectiveAccessorFactory(final int[] distribution, final int[] addresses, final int[] xPoints,
            final int[] yPoints) {
        this.distribution = distribution;
        this.addresses = addresses;
        this.x = xPoints;
        this.y = yPoints;
    }

    public void setData(final int[] data) {
        this.data = data;
    }

    @Override
    public ICollectiveAccessor create() {
        return new CollectiveAccessor(distribution, data, addresses, x, y);
    }
}
