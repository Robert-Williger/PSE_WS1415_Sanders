package model.map;

import model.IFactory;
import model.map.accessors.CollectiveAccessor;
import model.map.accessors.ICollectiveAccessor;

public class CollectiveAccessorFactory implements IFactory<ICollectiveAccessor> {
    protected final String[] attributes;
    protected final int[] distribution;
    protected final int[] addresses;
    protected final int[] data;

    public CollectiveAccessorFactory(final String[] attributes, final int[] distribution, final int[] addresses,
            final int[] data) {
        this.attributes = attributes;
        this.distribution = distribution;
        this.addresses = addresses;
        this.data = data;
    }

    @Override
    public ICollectiveAccessor create() {
        return new CollectiveAccessor(attributes, distribution, data, addresses);
    }
}
