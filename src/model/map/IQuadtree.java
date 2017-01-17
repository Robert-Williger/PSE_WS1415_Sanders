package model.map;

import java.util.PrimitiveIterator;

public interface IQuadtree {

    PrimitiveIterator.OfLong iterator(int row, int column, int zoom);

}
