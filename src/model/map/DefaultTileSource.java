package model.map;

public class DefaultTileSource implements ITileSource {

    private final ITile[][][] tiles;
    private final ITile emptyTile;
    private final int minZoomStep;

    public DefaultTileSource(final ITile[][][] tiles, final int minZoomStep) {
        this.tiles = tiles;
        this.minZoomStep = minZoomStep;

        this.emptyTile = new EmptyTile(-1, -1, -1);
    }

    @Override
    public ITile getTile(final int row, final int column, final int zoom) {
        final int relativeZoom = zoom - minZoomStep;
        if (relativeZoom >= 0 && zoom < tiles.length) {
            if (row >= 0 && row < tiles[relativeZoom].length) {
                if (column >= 0 && column < tiles[relativeZoom][row].length) {
                    return tiles[relativeZoom][row][column];
                }
            }
        }

        return emptyTile;
    }

}
