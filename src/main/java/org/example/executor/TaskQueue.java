package org.example.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskQueue {
    private final BlockingQueue<Runnable> queue;
    private final String name;

    public TaskQueue(int capacity, String name) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.name = name;
    }

    public boolean offer(Runnable task) {
        boolean result = queue.offer(task);
        if (result) {
            System.out.printf("[Queue] Task added to %s. Size: %d%n", name, queue.size());
        }
        return result;
    }

    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public Runnable take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }
}