package model.map;

import model.elements.AccessPoint;

public class AddressNode extends AccessPoint {

    private final String address;

    public AddressNode(final String address, final float offset, final int street) {
        super(offset, street);
        this.address = address;
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
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AddressNode)) {
            return false;
        }
        AddressNode other = (AddressNode) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

}