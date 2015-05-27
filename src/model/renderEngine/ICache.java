package model.renderEngine;

import java.awt.Image;

public interface ICache {

    void reset();

    void setSize(int size);

    int getSize();

    void put(long id, Image image);

    Image get(long id);

    boolean contains(long id);

}