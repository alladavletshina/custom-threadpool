package org.example.demo;

import org.example.executor.*;
import org.example.executor.balance.*;
import org.example.executor.policies.*;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(
                2, 4, 5, TimeUnit.SECONDS,
                10, 1, new CustomThreadFactory(),
                new AbortPolicy(), new RoundRobinBalancer()
        );

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    try {
                        System.out.printf("Task %d started in %s%n",
                                taskId, Thread.currentThread().getName());
                        TimeUnit.MILLISECONDS.sleep(500);
                        System.out.printf("Task %d completed%n", taskId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                System.err.printf("Task %d rejected: %s%n", taskId, e.getMessage());
            }
        }

        executor.shutdown();
    }
}