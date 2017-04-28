package model.targets;

import model.IModel;

public interface IRoutePoint extends IModel {

    void addPointListener(IPointListener listener);

    void setAddressPoint(AddressPoint point);

    void setListIndex(int index);

    void setTargetIndex(int index);

    void setState(State state);

    void setLocation(int x, int y);

    AddressPoint getAddressPoint();

    int getX(int zoom);

    int getY(int zoom);

    int getListIndex();

    int getTargetIndex();

    State getState();

    default String getAddress() {
        final AddressPoint point = getAddressPoint();
        return point != null ? point.getAddress() : null;
    }

    public enum State {
        added(0), editing(1), unadded(2);

        public final static int STATES = 3;
        private final int index;

        private State(final int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}