package model.map;

public class OffsetTileSource implements ITileSource {

    private final ITile[][][] tiles;
    private final ITile emptyTile;
    private final int[][] offsets;
    private final int minZoomStep;

    public OffsetTileSource(final ITile[][][] tiles, final int[][] offsets, final int minZoomStep) {
        this.tiles = tiles;
        this.offsets = offsets;
        this.minZoomStep = minZoomStep;

        this.emptyTile = new EmptyTile(-1, -1, -1);
    }

    @Override
    public ITile getTile(final int row, final int column, final int zoom) {
        final int relativeZoom = zoom - minZoomStep;
        if (relativeZoom >= 0 && relativeZoom <= tiles.length) {
            if (row >= 0 && row < tiles[relativeZoom].length) {
                final int relativeColumn = column - offsets[relativeZoom][row];
                if (relativeColumn >= 0 && relativeColumn < tiles[relativeZoom][row].length) {
                    return tiles[relativeZoom][row][relativeColumn];
                }
            }
        }

        return emptyTile;
    }

}
