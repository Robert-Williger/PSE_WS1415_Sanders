package model.map;

import java.util.Arrays;
import java.util.PrimitiveIterator;

public class EmptyQuadtree implements IQuadtree {

    @Override
    public PrimitiveIterator.OfLong iterator(int row, int column, int zoom) {
        return Arrays.stream(new long[0]).iterator();
    }

}
