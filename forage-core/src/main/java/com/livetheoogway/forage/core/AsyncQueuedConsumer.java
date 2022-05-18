package com.livetheoogway.forage.core;

import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This consumer is designed to make the Item consumption thread safe
 * This uses a simple linked blocking queue to store items and consume using a single thread
 * The thread is created during init, and then closed after all items are consumed using a traditional poison pill
 *
 * <p>
 * Be vary of the fact the following:
 * - if you queue up too many items (too many items to bootstrap), you might get impacted on memory.
 * - if you try to control the above by using a lower {maxCapacity}, your consumption will get blocked by the blocking
 * queue, as long as your bootstrapping operation is async, you should be good with this.
 * - by default, the capacity is set to Integer.MAX_VALUE
 *
 * @param <I> type of item been queued and consumed
 */
@Slf4j
public class AsyncQueuedConsumer<I> implements ItemConsumer<I>, Runnable {
    private final BlockingQueue<QueueItem<I>> queue;

    /* the inner consumer that consumes items one by one */
    private final ItemConsumer<I> consumer;

    /* a handler for errors during consumption of individual items (you can try to collect and push again, in this
    handler, if you wish to do so) */
    private final ErrorHandler<I> itemConsumptionErrorHandler;

    public AsyncQueuedConsumer(final ItemConsumer<I> consumer) {
        this(consumer, Integer.MAX_VALUE);
    }

    public AsyncQueuedConsumer(final ItemConsumer<I> consumer, final int maxCapacity) {
        this(consumer, maxCapacity, (item, exception) -> {});
    }

    /**
     * @param consumer                    the update listener is the one that finally consumes items one by one (need
     *                                    not be thread safe)
     * @param maxCapacity                 max capacity of the queue, after which, the {@link this#consume(Object)}
     *                                    will be blocked
     * @param itemConsumptionErrorHandler a handler for errors during consumption of individual items (you can try
     *                                    to collect and push again, in this handler, if you wish to do so)
     */
    public AsyncQueuedConsumer(final ItemConsumer<I> consumer,
                               final int maxCapacity,
                               final ErrorHandler<I> itemConsumptionErrorHandler) {
        this.consumer = consumer;
        this.queue = new LinkedBlockingDeque<>(maxCapacity);
        this.itemConsumptionErrorHandler = itemConsumptionErrorHandler;
    }

    @Override
    public void init() throws Exception {
        consumer.init();
        log.info("Initializing and starting consumer thread");
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void consume(I item) {
        queue.add(new QueueItem<>(item, false));
    }

    @Override
    public void finish() {
        log.info("Finished has been called, we shall now add the poison pill");
        queue.add(new QueueItem<>(null, true));
    }

    @Override
    public void run() {
        while (true) {
            QueueItem<I> queueItem;
            try {
                queueItem = queue.take();
            } catch (InterruptedException e) {
                log.info("Queue consumption has been interrupted", e);
                break;  /* we are breaking here to close the thread */
            }
            if (queueItem.isPoisonPill()) {
                log.info("We have now reached the poison pill, we will finish listening now");
                finishListener();
                break;
            }
            try {
                consumer.consume(queueItem.getItem());
            } catch (Exception e) {
                log.info("Error while taking the update for item:{}", queueItem, e);
                itemConsumptionErrorHandler.handleError(queueItem.getItem(), e);
                /* we are not breaking here, to continue consuming next items */
            }
        }
    }

    private void finishListener() {
        try {
            consumer.finish();
        } catch (Exception e) {
            log.error("Error while finishing the listener", e);
        }
    }

    @Value
    @ToString
    private static class QueueItem<I> {
        I item;
        boolean poisonPill;
    }
}
