package model.map;

import model.IFactory;
import model.map.accessors.CollectiveAccessor;
import model.map.accessors.ICollectiveAccessor;

public class CollectiveAccessorFactory implements IFactory<ICollectiveAccessor> {
    protected final int[] distribution;
    protected final int[] addresses;
    protected int[] data;

    public CollectiveAccessorFactory(final int[] distribution, final int[] addresses) {
        this.distribution = distribution;
        this.addresses = addresses;
    }

    public void setData(final int[] data) {
        this.data = data;
    }

    @Override
    public ICollectiveAccessor create() {
        return new CollectiveAccessor(distribution, data, addresses);
    }
}
