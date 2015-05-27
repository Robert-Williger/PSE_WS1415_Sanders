package view;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentListener;

public interface IMapView extends IView {

    void addMapListener(IMapListener listener);

    void addRoutePointListener(IDragListener listener);

    void setEnabled(boolean enabled);

    Image createScreenshot();

    void addComponentListener(ComponentListener listener);

    Dimension getSize();

}