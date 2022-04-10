package com.phonepe.platform.forage.core;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class AsyncQueuedConsumer<U> implements UpdateConsumer<U>, Runnable {
    private final BlockingQueue<QueueItem<U>> queue;
    private final UpdateListener<U> listener;
    private final ErrorHandler errorHandler;

    public AsyncQueuedConsumer(final UpdateListener<U> listener) {
        this(listener, Integer.MAX_VALUE);
    }

    public AsyncQueuedConsumer(final UpdateListener<U> listener, int queueSize) {
        this(listener, queueSize, new LoggingErrorHandler(AsyncQueuedConsumer.class));
    }

    public AsyncQueuedConsumer(final UpdateListener<U> listener, int queueSize,
                               final ErrorHandler errorHandler) {
        this.listener = listener;
        this.queue = new LinkedBlockingDeque<>(queueSize);
        this.errorHandler = errorHandler;
    }

    @Override
    public void init() {
        try {
            listener.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this);
        log.info("STARTING thread log");
        System.out.println("STARTING thread");
        thread.start();
    }

    @Override
    public void consume(U update) {
        queue.add(new QueueItem<>(update, false));
    }

    @Override
    public void finish() {
        queue.add(new QueueItem<>(null, true));
    }

    @Override
    public void run() {
        while (true) {
            try {
                QueueItem<U> queueItem = queue.take();
                if (queueItem.isPoisonPill()) {
                    System.out.println("reached POISON " );
                    listener.finish();
                    break;
                }
                listener.takeUpdate(queueItem.getU());
            } catch (InterruptedException e) {
                errorHandler.handleError(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Value
    private static class QueueItem<U> {
        U u;
        boolean poisonPill;
    }
}
