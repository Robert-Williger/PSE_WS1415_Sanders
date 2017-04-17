package model.targets;

import model.map.IPixelConverter;

public class AddressPoint extends AccessPoint {

    private final String address;
    private final int x;
    private final int y;
    private final IPixelConverter converter;

    public AddressPoint(final String address, final int x, final int y, final int street, final float offset,
            final IPixelConverter converter) {
        super(offset, street);
        this.x = x;
        this.y = y;
        this.address = address;
        this.converter = converter;
    }

    public int getX(final int zoom) {
        return converter.getPixelDistance(x, zoom);
    }

    public int getY(final int zoom) {
        return converter.getPixelDistance(y, zoom);
    }

    public String getAddress() {
        return address;
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