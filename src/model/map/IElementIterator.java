package model.map;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;

public interface IElementIterator {

    OfLong iterator(int row, int column, int zoom);

    void forEach(int row, int column, int zoom, LongConsumer consumer);
}
