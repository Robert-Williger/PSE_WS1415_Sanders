package model.targets;

import java.util.ListIterator;

import model.IModel;

public interface IPointList extends Iterable<IRoutePoint>, IModel {

    int size();

    boolean isEmpty();

    boolean remove(IRoutePoint point);

    void remove(int index);

    void add(IRoutePoint point);

    IRoutePoint get(int index);

    void changeOrder(int fromIndex, int toIndex);

    void clear();

    void addPointListListener(IPointListListener listener);

    ListIterator<IRoutePoint> listIterator(final int index);

}