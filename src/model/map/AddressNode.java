package model.map;

import model.elements.StreetNode;

public class AddressNode {

    private final String address;
    private final StreetNode streetNode;

    public AddressNode(final String address, final StreetNode streetNode) {
        this.address = address;
        this.streetNode = streetNode;
    }

    public String getAddress() {
        return address;
    }

    public StreetNode getStreetNode() {
        return streetNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((streetNode == null) ? 0 : streetNode.hashCode());
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
        if (streetNode == null) {
            if (other.streetNode != null) {
                return false;
            }
        } else if (!streetNode.equals(other.streetNode)) {
            return false;
        }
        return true;
    }

}