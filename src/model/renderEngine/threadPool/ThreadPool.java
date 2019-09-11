package model.renderEngine.threadPool;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadPool {

    private final ConcurrentLinkedQueue<Runnable> queue;
    private final Thread[] threads;

    public ThreadPool(final int workerCount) {
        queue = new ConcurrentLinkedQueue<>();
        threads = new Thread[workerCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }
    }

    public void add(final Runnable job, final int priority) {
        queue.offer(job);
    }

    public boolean contains(final Runnable job) {
        return queue.contains(job);
    }

    public void shutdown() {
        for (final Thread thread : threads) {
            thread.interrupt();
        }
    }

    public void awaitShutdown() {
        shutdown();
        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void flush() {
        queue.clear();
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            Runnable job = null;
            while (!interrupted()) {
                while ((job = queue.poll()) == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                    // Thread.yield();
                    // try {
                    // queue.wait();
                    // } catch (final InterruptedException e) {
                    // interrupt();
                    // return;
                    // }
                }
                job.run();
            }
        }
    }
}
