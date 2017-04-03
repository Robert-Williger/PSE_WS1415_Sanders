package model.targets;

import java.awt.Point;

import model.map.IPixelConverter;

public class AddressPoint extends AccessPoint {

    private String address;
    private final Point location;
    private final IPixelConverter converter;

    public AddressPoint(final IPixelConverter converter) {
        this(null, converter);
    }

    public AddressPoint(final String address, final IPixelConverter converter) {
        this(address, 0, 0, converter);
    }

    public AddressPoint(final String address, final int x, final int y, final IPixelConverter converter) {
        location = new Point(x, y);
        this.address = address;
        this.converter = converter;
    }

    public void setLocation(final int x, final int y) {
        location.setLocation(x, y);
    }

    public int getX(final int zoom) {
        return converter.getPixelDistance(location.x, zoom);
    }

    public int getY(final int zoom) {
        return converter.getPixelDistance(location.y, zoom);
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