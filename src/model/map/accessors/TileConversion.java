package model.map.accessors;

import model.map.IMapState;

public class TileConversion implements ITileConversion {
    private static final int COORD_BITS = 29;
    private static final int COORD_MASK = (1 << COORD_BITS) - 1;
    private static final int ZOOM_BITS = Long.SIZE - 2 * COORD_BITS - 1;
    private static final int MAX_COORD = (1 << COORD_BITS) - 1;
    private static final int MAX_ZOOM = (1 << ZOOM_BITS) - 1;

    private final IMapState state;

    public TileConversion(final IMapState state) {
        this.state = state;
    }

    @Override
    public long getId(final int row, final int column, final int zoom) {
        return isValid(row, column, zoom) ? ((((long) zoom << COORD_BITS) | row) << COORD_BITS) | column : -1;
    }

    private boolean isValid(final int row, final int column, final int zoom) {
        // return row > 0 && row < MAX_COORD && ...
        return ((MAX_COORD - row) | (MAX_COORD - column) | (MAX_ZOOM - zoom) | row | column | zoom) >= 0;
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

    @Override
    public int getRow(final int yCoord, final int zoom) {
        return yCoord / state.getCoordTileSize(zoom);
    }

    @Override
    public int getColumn(final int xCoord, final int zoom) {
        return xCoord / state.getCoordTileSize(zoom);
    }

    @Override
    public int getX(int column, int zoom) {
        return column * state.getCoordTileSize(zoom);
    }

    @Override
    public int getY(int row, int zoom) {
        return row * state.getCoordTileSize(zoom);
    }

    @Override
    public int getX(final long id) {
        return getX(getColumn(id), getZoom(id));
    }

    @Override
    public int getY(final long id) {
        return getX(getRow(id), getZoom(id));
    }

}
