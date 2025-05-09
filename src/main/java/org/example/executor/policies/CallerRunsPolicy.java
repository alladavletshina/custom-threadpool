package org.example.executor.policies;

import org.example.executor.CustomThreadPoolExecutor;
import org.example.executor.RejectionPolicy;

public class CallerRunsPolicy implements RejectionPolicy {
    @Override
    public void rejectedExecution(Runnable task, CustomThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            System.out.println("[Rejected] Executing task in caller thread");
            task.run();
        }
    }
}
