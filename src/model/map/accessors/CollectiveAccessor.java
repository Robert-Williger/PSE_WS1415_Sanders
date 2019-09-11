package model.map.accessors;

import java.awt.Point;
import java.util.Iterator;

public class CollectiveAccessor extends ElementAccessor implements ICollectiveAccessor {

    private final int[] addresses;
    private int id;
    private int elementStart;
    private int zoom;

    public CollectiveAccessor(final String[] attributes, final int[] distribution, final int[] data,
            final int[] addresses) {
        super(attributes, distribution, data);
        this.addresses = addresses;
        this.zoom = Integer.MAX_VALUE;
    }

    @Override
    public final int getX(final int index) {
        return data[elementStart + (index << 1) + 1];
    }

    @Override
    public final int getY(final int index) {
        return data[elementStart + (index << 1) + 2];
    }

    @Override
    public final int size() {
        return data[elementStart];
    }

    @Override
    public final void setId(int id) {
        this.id = id;
        update();
    }

    @Override
    public final void setZoom(final int zoom) {
        this.zoom = zoom;
        update();
    }

    @Override
    protected final int address() {
        return addresses[(int) getId()];
    }

    @Override
    public final int getId() {
        return id;
    }

    @Override
    public final int getZoom() {
        return zoom;
    }

    private void update() {
        elementStart = offset() + address();

        int offset = elementStart;
        while (zoom > data[offset]) {
            offset += 2;
        }

        elementStart += data[offset + 1];
    }

    @Override
    public Iterator<Point> iterator() {
        return new It(data, elementStart);
    }

    private static class It implements Iterator<Point> {
        private int[] data;
        private int cursor;
        private int last;

        public It(final int[] data, final int start) {
            this.data = data;
            this.last = start + 2 * data[start];
            this.cursor = start + 1;
        }

        public Point next() {
            return new Point(data[cursor++], data[cursor++]);
        }

        public boolean hasNext() {
            return cursor < last;
        }
    }
}
