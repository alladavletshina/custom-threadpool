package org.example.executor;

public interface RejectionPolicy {
    void rejectedExecution(Runnable task, CustomThreadPoolExecutor executor);
}
