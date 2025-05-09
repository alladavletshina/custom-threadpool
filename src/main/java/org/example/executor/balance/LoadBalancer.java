package org.example.executor.balance;

import org.example.executor.TaskQueue;
import java.util.List;

public interface LoadBalancer {
    TaskQueue selectQueue(List<TaskQueue> queues);
}