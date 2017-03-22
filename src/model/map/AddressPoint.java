package model.map;

import java.awt.Point;

import model.elements.AccessPoint;
import model.map.accessors.CollectiveUtil;
import model.map.accessors.ICollectiveAccessor;

public class AddressPoint extends AccessPoint {

    private String address;
    private final Point location;
    private final IMapState state;
    private final IPixelConverter converter;

    public AddressPoint(final IMapState state, final IPixelConverter converter) {
        this(null, state, converter);
    }

    public AddressPoint(final String address, final IMapState state, final IPixelConverter converter) {
        this(address, 0, 0, state, converter);
    }

    public AddressPoint(final String address, final int x, final int y, final IMapState state,
            final IPixelConverter converter) {
        location = new Point(x, y);
        this.address = address;
        this.converter = converter;
        this.state = state;
    }

    @Deprecated
    public AddressPoint(final String address, final float offset, final ICollectiveAccessor accessor,
            final IMapState state, final IPixelConverter converter) {
        location = CollectiveUtil.getLocation(accessor, offset);
        this.address = address;
        this.state = state;
        this.converter = converter;
    }

    public void setLocation(final int x, final int y) {
        location.setLocation(x, y);
    }

    public int getX() {
        return converter.getPixelDistance((int) (location.getX() - state.getX()), state.getZoomStep());
    }

    public int getY() {
        return converter.getPixelDistance((int) (location.getY() - state.getY()), state.getZoomStep());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AddressPoint other = (AddressPoint) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }

}