package model.map;

import java.util.Comparator;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntConsumer;

public interface IElementIterator {

    OfInt iterator(int row, int column, int zoom);

    void forEach(int row, int column, int zoom, IntConsumer consumer);

    IElementIterator filter(Predicate f);

    IElementIterator sort(Comparator<Integer> idComparator);
}
