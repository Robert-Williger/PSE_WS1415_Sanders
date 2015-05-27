package model.elements;

import java.util.List;

public class Building extends Area {

    private final String address;
    private StreetNode node;

    public Building(final List<Node> nodes, final String address, final StreetNode node) {
        super(nodes, 0);
        this.address = address;
        this.node = node;
    }

    public String getAddress() {
        return address;
    }

    public StreetNode getStreetNode() {
        return node;
    }

    public void setStreetNode(final StreetNode node) {
        this.node = node;
    }
}