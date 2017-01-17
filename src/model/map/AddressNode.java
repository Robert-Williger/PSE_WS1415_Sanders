package model.map;

import model.elements.AccessPoint;

public class AddressNode {

    private String address;
    private AccessPoint accessPoint;

    public AddressNode() {

    }

    public AddressNode(final String address, final AccessPoint streetNode) {
        this.address = address;
        this.accessPoint = streetNode;
    }

    public String getAddress() {
        return address;
    }

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public void setAccessPoint(final AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((accessPoint == null) ? 0 : accessPoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AddressNode other = (AddressNode) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (accessPoint == null) {
            if (other.accessPoint != null) {
                return false;
            }
        } else if (!accessPoint.equals(other.accessPoint)) {
            return false;
        }
        return true;
    }

}