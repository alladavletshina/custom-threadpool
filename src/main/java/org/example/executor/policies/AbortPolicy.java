package org.example.executor.policies;

import org.example.executor.CustomThreadPoolExecutor;
import org.example.executor. RejectionPolicy;
import java.util.concurrent.RejectedExecutionException;

public class AbortPolicy implements RejectionPolicy {
    @Override
    public void rejectedExecution(Runnable task, CustomThreadPoolExecutor executor) {
        throw new RejectedExecutionException(
                "Task " + task + " rejected from " + executor +
                        " (pool size: " + executor.getPoolSize() +
                        ", queue size: " + executor.getQueueSize() + ")");
    }
}