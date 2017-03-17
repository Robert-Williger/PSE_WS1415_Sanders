package model.renderEngine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;

public abstract class ThreadPool<T, J extends ThreadJob<T>> {

    private Set<J> jobs;
    private IAddressablePriorityQueue<J> queue;
    private int workingCount;

    private final Executor executor;
    private final Thread receiver;
    private final CompletionService<ResultWrapper> poolService;
    private final int poolSize;
    private final Lock lock;
    private final Condition condVar;

    public ThreadPool(final int threadCount) {
        jobs = Collections.synchronizedSet(new HashSet<J>());
        lock = new ReentrantLock();
        condVar = lock.newCondition();

        this.poolSize = threadCount;
        queue = new AddressableBinaryHeap<>();
        final ExecutorService workerPool = new ThreadPoolExecutor(poolSize, poolSize + 1, 1000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(poolSize, true));
        poolService = new ExecutorCompletionService<>(workerPool);
        executor = new Executor();
        receiver = new Receiver();
        executor.start();
        receiver.start();
    }

    protected abstract void processResult(final J job, final T result);

    public void add(final J job, final int priority) {
        lock.lock();

        if (!queue.contains(job)) {
            jobs.add(job);
            queue.insert(job, priority);

            synchronized (executor) {
                executor.notify();
            }
        }

        condVar.signal();
        lock.unlock();
    }

    public void changeKey(final J job, final int priority) {
        lock.lock();

        queue.changeKey(job, priority);

        condVar.signal();
        lock.unlock();
    }

    public boolean remove(final J job) {
        lock.lock();

        final boolean ret = queue.remove(job);

        condVar.signal();
        lock.unlock();

        return ret;
    }

    public boolean contains(final J job) {
        return jobs.contains(job);
    }

    public void flush() {
        synchronized (queue) {
            queue = new AddressableBinaryHeap<>();
            jobs = Collections.synchronizedSet(new HashSet<J>());
        }
    }

    private class Worker implements Callable<ResultWrapper> {
        J job;

        public Worker(final J job) {
            this.job = job;
        }

        @Override
        public ResultWrapper call() {
            final T result = job.work();
            return new ResultWrapper(job, result);
        }
    }

    private class ResultWrapper {
        T result;
        J job;

        public ResultWrapper(final J job, final T result) {
            this.job = job;
            this.result = result;
        }
    }

    private class Executor extends Thread {

        @Override
        public void run() {
            while (!interrupted()) {
                while (workingCount == poolSize) {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                            interrupt();
                        }
                    }
                }

                lock.lock();

                while (queue.size() == 0) {
                    try {
                        condVar.await();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                        lock.unlock();
                        interrupt();
                    }
                }
                try {
                    synchronized (ThreadPool.class) {
                        while (workingCount < poolSize && queue.size() != 0) {
                            final J job = queue.deleteMin();
                            poolService.submit(new Worker(job));
                            workingCount++;
                        }
                    }
                } catch (final RejectedExecutionException e) {
                    e.printStackTrace();
                    lock.unlock();
                }

                lock.unlock();
            }

        }
    }

    private class Receiver extends Thread {

        @Override
        public void run() {
            ResultWrapper result;
            while (!interrupted()) {
                try {
                    result = poolService.take().get();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (final ExecutionException e) {
                    e.printStackTrace();
                    continue;
                }

                synchronized (ThreadPool.class) {
                    workingCount--;
                }

                if (workingCount == poolSize - 1) {
                    synchronized (executor) {
                        executor.notify();
                    }
                }

                processResult(result.job, result.result);
                jobs.remove(result.job);
            }
        }
    }
}
