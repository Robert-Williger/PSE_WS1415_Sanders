package model.routing;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProgressable implements Progressable {

    private final List<IProgressListener> listeners;
    private double progress;

    public AbstractProgressable() {
        listeners = new ArrayList<IProgressListener>();
        progress = 0;
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        listeners.remove(listener);
    }

    protected void fireProgressDone(final double progress) {
        if (progress < 0) {
            this.progress = 0;
        } else if (this.progress + progress < 100) {
            this.progress += progress;
        } else {
            this.progress = 100;
        }

        for (final IProgressListener l : listeners) {
            l.progressDone((int) this.progress);
        }
    }

    protected void fireErrorOccured(final String message) {
        for (final IProgressListener l : listeners) {
            l.errorOccured(message);
        }
    }

}
