package model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProgressable implements Progressable {

    private final List<IProgressListener> listeners;

    public AbstractProgressable() {
        listeners = new ArrayList<>();
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        listeners.remove(listener);
    }

    protected void fireProgressDone(final int progress) {
        for (final IProgressListener l : listeners) {
            l.progressDone(progress);
        }
    }

    protected void fireErrorOccured(final String message) {
        for (final IProgressListener l : listeners) {
            l.errorOccured(message);
        }
    }

    protected void fireStepCommenced(final String message) {
        for (final IProgressListener l : listeners) {
            l.stepCommenced(message);
        }
    }

}
