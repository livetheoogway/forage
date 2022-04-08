package com.phonepe.platform.forage.core;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class AsyncQueuedConsumer<U> implements UpdateConsumer<U>, Runnable {
    private final BlockingQueue<U> queue;
    private List<UpdateListener<U>> listeners;

    public AsyncQueuedConsumer() {
        queue = new LinkedBlockingDeque<>();
    }

    @Override
    public void consume(U update) {
        queue.add(update);
    }

    @Override
    public void run() {
        while(true) {
            try {
                U take = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
