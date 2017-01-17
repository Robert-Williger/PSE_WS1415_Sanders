package model.renderEngine;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import util.AddressableBinaryHeap;

public class AddressableBinaryBlockingHeap<T> extends AddressableBinaryHeap<T> implements BlockingQueue<T> {

    @Override
    public synchronized T remove() {
        return super.deleteMin();
    }

    @Override
    public synchronized T poll() {
        return isEmpty() ? null : remove();
    }

    @Override
    public T element() {
        return min();
    }

    @Override
    public synchronized T peek() {
        return min();
    }

    @Override
    public Iterator<T> iterator() {
        return iterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final Object c1 : c) {
            if (!contains(c1)) {
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (final Object o : c) {
            if (!remove(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public synchronized void clear() {
        while (!isEmpty()) {
            deleteMin();
        }
    }

    @Override
    public synchronized boolean add(T e) {
        super.insert(e);
        return true;
    }

    @Override
    public synchronized boolean offer(T e) {
        return add(e);
    }

    @Override
    public synchronized void put(T e) throws InterruptedException {
        super.insert(e);
    }

    @Override
    public synchronized boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public synchronized T take() throws InterruptedException {
        return min();
    }

    @Override
    public synchronized T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean remove(final Object o) {
        return super.remove((T) o);
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean contains(final Object o) {
        return super.contains((T) o);
    }

    @Override
    public synchronized void insert(final T element, final int key) {
        super.insert(element, key);
    }

    @Override
    public synchronized void changeKey(final T element, final int key) {
        super.changeKey(element, key);
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public Object[] toArray() {
        // TODO
        return null;
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        // TODO
        return null;
    }

}
