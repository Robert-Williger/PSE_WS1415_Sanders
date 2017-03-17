package model;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AbstractModel implements IModel {

    private final List<ChangeListener> listeners;

    public AbstractModel() {
        listeners = new LinkedList<>();
    }

    @Override
    public void addChangeListener(final ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(final ChangeListener listener) {
        listeners.remove(listener);
    }

    protected void fireChange() {
        for (final ChangeListener listener : listeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

}
