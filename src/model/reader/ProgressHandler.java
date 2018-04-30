package model.reader;

import java.util.ArrayList;
import java.util.List;

import model.IProgressListener;
import model.Progressable;

class ProgressHandler implements Progressable {
    private final List<IProgressListener> list;
    private long totalBytes;
    private long currentBytes;
    private int progress;
    private boolean canceled;

    public ProgressHandler() {
        list = new ArrayList<>();
    }

    @Override
    public void cancelCalculation() {
        this.canceled = true;
    }

    public void addProgressListener(final IProgressListener listener) {
        list.add(listener);
    }

    public void removeProgressListener(final IProgressListener listener) {
        list.remove(listener);
    }

    public void fireProgressDone(final int progress) {
        for (final IProgressListener listener : list) {
            listener.progressDone(progress);
        }
    }

    public void fireErrorOccured(final String message) {
        for (final IProgressListener listener : list) {
            listener.errorOccured(message);
        }
    }

    public void fireStepCommenced(final String step) {
        for (final IProgressListener listener : list) {
            listener.stepCommenced(step);
        }
    }

    public void add(final long bytes) {
        this.currentBytes += bytes;
        this.progress = (int) (100 * currentBytes / totalBytes);
    }

    public void reset(final long totalBytes) {
        this.totalBytes = totalBytes;
        this.canceled = false;
        this.currentBytes = 0;
        this.progress = -1;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public int getProgress() {
        return progress;
    }
}