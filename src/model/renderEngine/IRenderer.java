package model.renderEngine;

import java.awt.Image;

import model.IModel;
import model.map.IPixelConverter;
import model.map.ITile;

public interface IRenderer extends IModel {

    boolean render(ITile tile, Image image);

    void setConverter(IPixelConverter converter);

}