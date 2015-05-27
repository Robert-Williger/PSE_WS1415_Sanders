package model;

import javax.swing.event.ChangeListener;

public interface IModel {

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);

}