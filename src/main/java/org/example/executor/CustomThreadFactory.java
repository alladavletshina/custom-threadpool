package org.example.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public CustomThreadFactory() {
        this.group = new ThreadGroup("CustomPoolGroup-" + poolNumber.getAndIncrement());
        this.namePrefix = "pool-" + group.getName() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
        configureThread(t);
        System.out.printf("[ThreadFactory] Created new thread: %s%n", t.getName());
        return t;
    }

    private void configureThread(Thread t) {
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        t.setUncaughtExceptionHandler((thread, ex) -> {
            System.err.printf("[ThreadError] Exception in thread %s: %s%n",
                    thread.getName(), ex.getMessage());
        });
    }
}