package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class SidebarLayout implements LayoutManager {

    private Component sidebar;
    private Component main;
    private Container target;

    public SidebarLayout(final Component sidebar, final Component main, final Container target) {
        this.sidebar = sidebar;
        this.main = main;
        this.target = target;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {

    }

    @Override
    public void removeLayoutComponent(Component comp) {

    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return target.getSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public void layoutContainer(Container parent) {
        sidebar.setSize(sidebar.getPreferredSize().width, target.getHeight());
        main.setSize(target.getWidth(), target.getHeight());
    }

}
