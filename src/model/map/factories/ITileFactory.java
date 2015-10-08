package model.map.factories;

import java.io.IOException;

import model.map.ITile;

public interface ITileFactory {

    ITile createTile(final int row, final int column, final int zoom) throws IOException;

}
