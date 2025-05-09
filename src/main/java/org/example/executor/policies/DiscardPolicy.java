package org.example.executor.policies;

import org.example.executor.CustomThreadPoolExecutor;
import org.example.executor.RejectionPolicy;

public class DiscardPolicy implements RejectionPolicy {
    @Override
    public void rejectedExecution(Runnable task, CustomThreadPoolExecutor executor) {
        System.out.println("[Rejected] Discarding task: " + task);
    }
}
