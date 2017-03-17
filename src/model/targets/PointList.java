package model.targets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.AbstractModel;

public class PointList extends AbstractModel implements IPointList {
    private final List<IPointListListener> listener;
    private final List<IRoutePoint> pointList;

    public PointList() {
        listener = new ArrayList<>();
        pointList = new ArrayList<>();
    }

    private void fireRemoveEvent(final IRoutePoint point) {
        for (final IPointListListener e : listener) {
            e.pointRemoved(point);
        }
    }

    private void fireAddEvent(final IRoutePoint point) {
        for (final IPointListListener e : listener) {
            e.pointAdded(point);
        }
    }

    private void refreshIndexes(final int from) {
        for (int i = from; i < pointList.size(); i++) {
            pointList.get(i).setListIndex(i);
        }
    }

    @Override
    public Iterator<IRoutePoint> iterator() {
        return pointList.iterator();
    }

    @Override
    public int size() {
        return pointList.size();
    }

    @Override
    public boolean remove(final IRoutePoint point) {
        boolean ret = false;

        if (point.getListIndex() < pointList.size() && pointList.get(point.getListIndex()).equals(point)) {
            this.remove(point.getListIndex());
            ret = true;
        }
        return ret;
    }

    @Override
    public void remove(final int index) {
        if (index < pointList.size()) {
            fireRemoveEvent(pointList.remove(index));
            refreshIndexes(index);
        }
    }

    @Override
    public void add(final IRoutePoint point) {
        point.setListIndex(pointList.size());
        point.setTargetIndex(pointList.size());
        pointList.add(point);
        fireAddEvent(point);
    }

    @Override
    public IRoutePoint get(final int index) {
        return pointList.get(index);
    }

    @Override
    public void changeOrder(final int fromIndex, final int toIndex) {
        pointList.add(toIndex, pointList.remove(fromIndex));
        refreshIndexes(Math.min(fromIndex, toIndex));
    }

    @Override
    public void reset() {
        final List<IRoutePoint> temp = new ArrayList<>(pointList);

        pointList.clear();

        for (final IRoutePoint p : temp) {
            fireRemoveEvent(p);
        }
    }

    @Override
    public void addPointListListener(final IPointListListener listener) {
        this.listener.add(listener);
    }

}