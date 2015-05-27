package view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class FullLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(final String name, final Component comp) {

    }

    @Override
    public void removeLayoutComponent(final Component comp) {

    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        return new Dimension(20, 20);
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return null;
    }

    @Override
    public void layoutContainer(final Container parent) {
        synchronized (parent.getTreeLock()) {
            for (final Component component : parent.getComponents()) {
                component.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            }
        }
    }

}
