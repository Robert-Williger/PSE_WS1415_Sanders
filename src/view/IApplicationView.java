package view;

import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

public interface IApplicationView extends IView {

    void addWindowListener(WindowListener listener);

    void addMenuBarListener(ActionListener listener);

    ISidebarView getSidebar();

    IMapView getMap();

    void setHelpVisible(boolean visible);

}