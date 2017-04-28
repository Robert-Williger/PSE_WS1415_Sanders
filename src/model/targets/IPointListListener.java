package model.targets;

public interface IPointListListener {

    void pointAdded(IRoutePoint point);

    void pointRemoved(IRoutePoint point);

    void listCleared(int oldSize);

}