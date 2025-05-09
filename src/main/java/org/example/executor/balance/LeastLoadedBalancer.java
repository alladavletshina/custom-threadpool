package org.example.executor.balance;


import org.example.executor.TaskQueue;
import java.util.Comparator;
import java.util.List;

public class LeastLoadedBalancer implements LoadBalancer {
    @Override
    public TaskQueue selectQueue(List<TaskQueue> queues) {
        return queues.stream()
                .min(Comparator.comparingInt(TaskQueue::size))
                .orElseThrow();
    }
}