package model;

import java.util.List;

import model.targets.AddressPoint;

public interface ITextProcessor {

    List<String> suggest(String address);

    AddressPoint parse(String address);

}