package model.renderEngine;

public abstract class ThreadJob<T> {

    private final long id;

    public ThreadJob(final long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    protected abstract T work();

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final ThreadJob<?> other = (ThreadJob<?>) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
