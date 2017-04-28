package view;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IPointListener;
import model.targets.IRoutePoint;

public class RoutePointListModel extends AbstractListModel<IRoutePoint> implements ListModel<IRoutePoint> {
    private static final long serialVersionUID = 1L;

    private IPointList list;
    private int size;

    public RoutePointListModel(final IPointList list) {
        setPointList(list);
    }

    public void setPointList(final IPointList list) {
        this.list = list;
        calculateSize();

        fireContentsChanged(this, 0, getSize() - 1);
        list.addPointListListener(new IPointListListener() {

            @Override
            public void pointRemoved(final IRoutePoint point) {
                --size;
                final int index = point.getListIndex();
                fireIntervalRemoved(this, index, index);
                fireContentsChanged(this, index, getSize() - 1);
            }

            @Override
            public void pointAdded(final IRoutePoint point) {
                point.addPointListener(new IPointListener() {
                    private boolean added;

                    @Override
                    public void targetIndexChanged() {
                    }

                    @Override
                    public void stateChanged() {
                        final int index = point.getListIndex();
                        // this will also fire, if the point is removed from list, because point.getState() == null
                        if (point.getState() != IRoutePoint.State.unadded) {
                            if (!added) {
                                ++size;
                                added = true;
                                fireIntervalAdded(this, index, index);
                            }
                        }

                        fireContentsChanged(this, index, getSize() - 1);
                    }

                    @Override
                    public void locationChanged() {
                    }

                    @Override
                    public void listIndexChanged() {
                        // final int index = point.getListIndex();
                        // fireContentsChanged(this, index, index);
                    }

                    @Override
                    public void addressChanged() {
                    }
                });

            }

            @Override
            public void listCleared(final int oldSize) {
                size = 0;
                fireIntervalRemoved(this, 0, oldSize - 1);
            }
        });
    }

    private void calculateSize() {
        for (final IRoutePoint point : list) {
            if (point.getState() != IRoutePoint.State.unadded) {
                ++size;
            }
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public IRoutePoint getElementAt(final int index) {
        return list.get(index);
    }

    public void fireContentChanged(final int index) {
        fireContentsChanged(this, index, index);
    }
}
