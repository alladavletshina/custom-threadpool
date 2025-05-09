package org.example.executor;

import org.example.executor.balance.LoadBalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CustomThreadPoolExecutor implements Executor {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int minSpareThreads;


    private final AtomicInteger workerCount = new AtomicInteger(0);
    private final List<TaskQueue> queues;
    private final List<Worker> workers = new ArrayList<>();
    private final ThreadFactory threadFactory;
    private final RejectionPolicy rejectionPolicy;
    private final LoadBalancer loadBalancer;
    private final ReentrantLock mainLock = new ReentrantLock();
    private volatile boolean isShutdown = false;
    private final AtomicInteger completedTaskCount = new AtomicInteger(0);

    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize,
                                    long keepAliveTime, TimeUnit timeUnit,
                                    int queueSize, int minSpareThreads,
                                    ThreadFactory threadFactory,
                                    RejectionPolicy rejectionPolicy,
                                    LoadBalancer loadBalancer) {
        
        if (corePoolSize < 0 || maxPoolSize <= 0 || maxPoolSize < corePoolSize)
            throw new IllegalArgumentException("Invalid pool size");
        if (keepAliveTime < 0)
            throw new IllegalArgumentException("Invalid keep alive time");
        if (queueSize <= 0)
            throw new IllegalArgumentException("Invalid queue size");
        if (minSpareThreads < 0 || minSpareThreads > corePoolSize)
            throw new IllegalArgumentException("Invalid min spare threads");

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = threadFactory;
        this.rejectionPolicy = rejectionPolicy;
        this.loadBalancer = loadBalancer;

        
        this.queues = new ArrayList<>();
        for (int i = 0; i < corePoolSize; i++) {
            queues.add(new TaskQueue(queueSize, "queue-" + i));
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException();
        if (isShutdown) throw new RejectedExecutionException("Pool is shutting down");

        // Try to add to queue first
        TaskQueue queue = loadBalancer.selectQueue(queues);
        if (queue.offer(command)) {
            if (workerCount.get() < minSpareThreads) {
                addWorker(null);
            }
            return;
        }

        
        if (workerCount.get() < maxPoolSize) {
            addWorker(command);
            return;
        }

        
        rejectionPolicy.rejectedExecution(command, this);
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        FutureTask<T> future = new FutureTask<>(task);
        execute(future);
        return future;
    }

    public void shutdown() {
        mainLock.lock();
        try {
            isShutdown = true;
            for (Worker worker : workers) {
                worker.stop();
            }
        } finally {
            mainLock.unlock();
        }
    }

    public List<Runnable> shutdownNow() {
        mainLock.lock();
        try {
            isShutdown = true;
            List<Runnable> remainingTasks = new ArrayList<>();
            for (TaskQueue queue : queues) {
                queue.clear();
            }
            for (Worker worker : workers) {
                worker.stop();
            }
            return remainingTasks;
        } finally {
            mainLock.unlock();
        }
    }

    
    public boolean isShutdown() { return isShutdown; }
    public int getPoolSize() { return workerCount.get(); }
    public int getActiveCount() { return (int) workers.stream().filter(Worker::isRunning).count(); }
    public int getQueueSize() { return queues.stream().mapToInt(TaskQueue::size).sum(); }
    public long getCompletedTaskCount() { return completedTaskCount.get(); }
    public int getCorePoolSize() { return corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public long getKeepAliveTime() { return keepAliveTime; }
    public TimeUnit getTimeUnit() { return timeUnit; }
    public TaskQueue getQueue() { return queues.get(0); }

    private void addWorker(Runnable firstTask) {
        mainLock.lock();
        try {
            if (workerCount.get() >= maxPoolSize) return;

            Worker worker = new Worker(firstTask);
            workers.add(worker);
            workerCount.incrementAndGet();
            worker.start();
        } finally {
            mainLock.unlock();
        }
    }

    private void workerExited(Worker worker) {
        mainLock.lock();
        try {
            workers.remove(worker);
            workerCount.decrementAndGet();
            completedTaskCount.incrementAndGet();

            if (!isShutdown && workerCount.get() < minSpareThreads) {
                addWorker(null);
            }
        } finally {
            mainLock.unlock();
        }
    }

    private class Worker implements Runnable {
        private Runnable firstTask;
        private volatile boolean running = true;
        private Thread thread;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        void start() {
            this.thread = threadFactory.newThread(this);
            this.thread.start();
        }

        @Override
        public void run() {
            try {
                Runnable task = firstTask;
                firstTask = null;

                while (running && !Thread.currentThread().isInterrupted()) {
                    if (task == null) {
                        task = getTask();
                        if (task == null) break;
                    }

                    runTask(task);
                    task = null;
                }
            } finally {
                workerExited(this);
                System.out.printf("[Worker] %s terminated%n", Thread.currentThread().getName());
            }
        }

        private Runnable getTask() {
            try {
                boolean timed = workerCount.get() > corePoolSize;
                TaskQueue queue = queues.get((int)(Math.random() * queues.size()));
                return timed ?
                        queue.poll(keepAliveTime, timeUnit) :
                        queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        private void runTask(Runnable task) {
            try {
                System.out.printf("[Worker] %s executing task%n", Thread.currentThread().getName());
                task.run();
            } catch (Exception e) {
                System.err.printf("[Worker] %s task failed: %s%n",
                        Thread.currentThread().getName(), e.getMessage());
            }
        }

        void stop() {
            running = false;
            if (thread != null) {
                thread.interrupt();
            }
        }

        boolean isRunning() {
            return running && !Thread.currentThread().isInterrupted();
        }
    }
}
