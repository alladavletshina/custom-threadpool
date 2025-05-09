package org.example.executor.balance;

import org.example.executor.TaskQueue;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer implements LoadBalancer {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public TaskQueue selectQueue(List<TaskQueue> queues) {
        return queues.get(index.getAndUpdate(i -> (i + 1) % queues.size()));
    }
}
