package model.addressIndex;

import java.util.List;

import model.targets.AddressPoint;

public interface IAddressMatcher {

    List<String> suggest(String address);

    AddressPoint parse(String address);

}