package model.renderEngine;

public abstract class ThreadJobTest<T> {

    private final long id;

    public ThreadJobTest(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    protected abstract T getResult();

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final ThreadJobTest<?> other = (ThreadJobTest<?>) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
