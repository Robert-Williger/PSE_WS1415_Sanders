package model.targets;

import model.IModel;

public interface IPointList extends Iterable<IRoutePoint>, IModel {

    int size();

    boolean remove(IRoutePoint point);

    void remove(int index);

    void add(IRoutePoint point);

    IRoutePoint get(int index);

    void changeOrder(int fromIndex, int toIndex);

    void reset();

    void addPointListListener(IPointListListener listener);

}