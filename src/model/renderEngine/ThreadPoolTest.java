package model.renderEngine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import util.AddressableBinaryHeap;

public abstract class ThreadPoolTest<T, J extends ThreadJobTest<T>> {

    private final AddressableBinaryHeap<J> queue;
    private final Thread[] threads;
    private final Set<Long> jobs;

    public ThreadPoolTest(final int workerCount) {
        queue = new AddressableBinaryHeap<>();
        jobs = Collections.synchronizedSet(new HashSet<Long>());
        this.threads = new Thread[workerCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = createWorker();
            threads[i].start();
        }
    }

    protected abstract Worker createWorker();

    public void add(final J job, final int priority) {
        synchronized (queue) {
            jobs.add(job.getID());
            queue.insert(job, priority);
            queue.notifyAll();
        }
    }

    public void changeKey(final J job, final int priority) {
        synchronized (queue) {
            queue.changeKey(job, priority);
            queue.notify();
        }
    }

    public boolean remove(final J job) {
        synchronized (queue) {
            jobs.remove(job.getID());
            return queue.remove(job);
        }
    }

    public boolean contains(final long id) {
        return jobs.contains(id);
    }

    public void shutdown() {
        for (final Thread thread : threads) {
            thread.interrupt();
        }
    }

    public void flush() {
        synchronized (queue) {
            // TODO add reset / removeAll / clear method in
            // AddressableBinaryHeap
            while (!queue.isEmpty()) {
                queue.deleteMin();
            }
            jobs.clear();
        }
    }

    protected abstract void processResult(final J job);

    abstract class Worker extends Thread {

        protected abstract void work(final J job);

        @Override
        public void run() {
            while (!interrupted()) {
                J job;
                synchronized (queue) {
                    while ((job = queue.deleteMin()) == null) {
                        try {
                            queue.wait();
                        } catch (final InterruptedException e) {
                            interrupt();
                            return;
                        }
                    }
                }
                work(job);
                processResult(job);
                jobs.remove(job.getID());
            }
        }
    }
}
