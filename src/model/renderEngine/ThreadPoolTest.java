package model.renderEngine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.routing.AddressableBinaryHeap;

public abstract class ThreadPoolTest<T, J extends ThreadJob<T>> {

    private final AddressableBinaryHeap<J> queue;
    private final Set<J> jobs;

    public ThreadPoolTest(final int threadCount) {
        queue = new AddressableBinaryHeap<J>();
        jobs = Collections.synchronizedSet(new HashSet<J>());
        for (int i = 0; i < threadCount; i++) {
            new Worker().start();
        }
    }

    public void add(final J job, final int priority) {
        synchronized (queue) {
            jobs.add(job);
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
            jobs.remove(job);
            return queue.remove(job);
        }
    }

    public boolean contains(final J job) {
        return jobs.contains(job);
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

    protected abstract void processResult(final J job, final T result);

    private class Worker extends Thread {
        public void run() {
            while (!interrupted()) {
                J job;
                J job2;
                J job3;
                synchronized (queue) {
                    while ((job = queue.deleteMin()) == null) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                    job2 = queue.deleteMin();
                    job3 = queue.deleteMin();
                }
                processResult(job, job.work());
                jobs.remove(job);

                if (job2 != null) {
                    processResult(job2, job2.work());
                    jobs.remove(job2);
                }
                if (job3 != null) {
                    processResult(job3, job3.work());
                    jobs.remove(job3);
                }

            }
        }
    }
}
