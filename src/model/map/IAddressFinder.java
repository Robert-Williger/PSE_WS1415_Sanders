package model.map;

import model.targets.AddressPoint;

@FunctionalInterface
public interface IAddressFinder {

    AddressPoint getAddress(int x, int y, int zoom);
}
