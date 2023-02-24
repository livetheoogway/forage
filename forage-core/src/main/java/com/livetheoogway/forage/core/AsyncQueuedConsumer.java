/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.core;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

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
     * @param maxCapacity                 max capacity of the queue, after which, the consumption will get blocked
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
        log.info("[forage] Initializing and starting consumer thread");
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void consume(I item) {
        queue.add(new QueueItem<>(item, false));
    }

    @Override
    public void finish() throws InterruptedException {
        log.info("[forage] finish() has been called, we shall now add the poison pill");
        queue.add(new QueueItem<>(null, true));

        /* Since the consumption is async, we need to now wait for all items pushed to the queue, to be consumed.
         * If we don't do this, the scheduler might start a new Init process, which can potentially cause multiple
         * inits to be running in parallel. We don't want that to happen
         */
        synchronized (queue) {
            while (!queue.isEmpty()) {
                log.info("[forage] Waiting till all queue items are consumed, including the poison pill");
                queue.wait(); // wait until the queue consumption has finished
            }
        }
        log.info("[forage] Finished with the entire consumer process");
    }

    @Override
    @SneakyThrows
    public void run() {
        val consumedCounter = new AtomicInteger(0);
        while (true) {
            QueueItem<I> queueItem;
            try {
                queueItem = queue.take();
            } catch (InterruptedException e) {
                log.info("[forage] Queue consumption has been interrupted", e);
                throw e;
            }
            if (queueItem.isPoisonPill()) {
                log.info("[forage] We have now reached the poison pill, we will finish listening now");
                finishListener();
                break;
            }
            try {
                consumer.consume(queueItem.getItem());
                consumedCounter.incrementAndGet();
            } catch (Exception e) {
                log.error("[forage] Error while taking the update for item:{}", queueItem, e);
                itemConsumptionErrorHandler.handleError(queueItem.getItem(), e);
                /* we are not breaking here, to continue consuming next items */
            }
        }
        synchronized (queue) {
            log.info("[forage] Consumed:{} items. Notifying all threads waiting for the queue to become empty",
                     consumedCounter.get());
            queue.notifyAll(); // ensure that the wait was finished
        }
    }

    private void finishListener() {
        try {
            consumer.finish();
        } catch (Exception e) {
            log.error("[forage] Error while finishing the listener", e);
            itemConsumptionErrorHandler.handleError(null, e);
        }
    }

    @Value
    @ToString
    private static class QueueItem<I> {
        I item;
        boolean poisonPill;
    }
}
