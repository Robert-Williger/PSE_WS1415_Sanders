package model.map;

public class TileState implements ITileState {
    private final IMapSection mapSection;
    private final IPixelMapping mapping;
    private final int tileSize;

    public TileState(IMapSection mapSection, IPixelMapping mapping, int tileSize) {
        super();
        this.mapSection = mapSection;
        this.mapping = mapping;
        this.tileSize = tileSize;
    }

    @Override
    public int getFirstRow(final int zoom) {
        return gridPos(mapSection.getMidY(), -mapSection.getHeight() / 2, zoom);
    }

    @Override
    public int getLastRow(final int zoom) {
        return gridPos(mapSection.getMidY(), +mapSection.getHeight() / 2, zoom);
    }

    @Override
    public int getFirstColumn(final int zoom) {
        return gridPos(mapSection.getMidX(), -mapSection.getWidth() / 2, zoom);
    }

    @Override
    public int getLastColumn(final int zoom) {
        return gridPos(mapSection.getMidX(), +mapSection.getWidth() / 2, zoom);
    }

    private int gridPos(final int coordLocation, final int pixelOffset, final int zoom) {
        return (mapping.getPixelDistance(coordLocation, zoom) + pixelOffset) / tileSize;
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

}
