package model.map.accessors;

public class TileConversion implements ITileIdConversion {
    private static final int COORD_BITS = 29;
    private static final int COORD_MASK = (1 << COORD_BITS) - 1;
    private static final int ZOOM_BITS = Long.SIZE - 2 * COORD_BITS - 1;
    private static final int MAX_ZOOM = (1 << ZOOM_BITS) - 1;

    @Override
    public long getId(final int row, final int column, final int zoom) {
        return isValid(row, column, zoom) ? getValidId(row, column, zoom) : -1;
    }

    private static long getValidId(final int row, final int column, final int zoom) {
        return ((((long) zoom << COORD_BITS) | row) << COORD_BITS) | column;
    }

    private static boolean isValid(final int row, final int column, final int zoom) {
        // return row > 0 && row < MAX_COORD && ...
        final int maxCoord = 1 << zoom;
        return (row | column | zoom | (maxCoord - row) | (maxCoord - column) | (MAX_ZOOM - zoom)) >= 0;
    }

    @Override
    public int getRow(final long id) {
        return (int) ((id >> COORD_BITS) & COORD_MASK);
    }

    @Override
    public int getColumn(final long id) {
        return (int) (id & COORD_MASK);
    }

    @Override
    public int getZoom(final long id) {
        return (int) (id >> (2 * COORD_BITS));
    }
}
