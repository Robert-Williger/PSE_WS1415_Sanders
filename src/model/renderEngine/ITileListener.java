package model.renderEngine;

import java.awt.Image;

@FunctionalInterface
public interface ITileListener {

    void rendered(Image img, int row, int column, int zoom);

}
