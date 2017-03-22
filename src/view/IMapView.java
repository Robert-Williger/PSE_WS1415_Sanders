package view;

import java.awt.Image;

import model.targets.IRoutePoint;

public interface IMapView extends IView {

    void addMapListener(IMapListener listener);

    // void addRoutePointListener(IDragListener listener);

    IRoutePoint getRoutePoint(int x, int y);

    void setEnabled(boolean enabled);

    Image createScreenshot();

    // void addComponentListener(ComponentListener listener);

    int getWidth();

    int getHeight();

}