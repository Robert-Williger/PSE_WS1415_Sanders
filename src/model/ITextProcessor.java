package model;

import java.util.List;

import model.elements.StreetNode;

public interface ITextProcessor {

    List<String> suggest(String address);

    StreetNode parse(String address);

}