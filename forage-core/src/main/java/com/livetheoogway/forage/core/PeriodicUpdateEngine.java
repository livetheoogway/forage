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

import com.livetheoogway.forage.models.DataId;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is a type of Update engine, that performs the bootstrap process at regular intervals
 * It uses a {@link ScheduledExecutorService} that schdules the thread
 * Handles the following:
 * - Exceptions during bootstrap
 * - runs at fixed delay, to prevent multiple bootstraps from happening. So if the previous one is still in progress
 * (ie, it is taking more time than the delay interval specified during init), it will not schedule the next bootstrap
 */
@Slf4j
public class PeriodicUpdateEngine<D extends DataId> extends UpdateEngine<D> {

    private final ScheduledExecutorService executorService;
    private final int delay;
    private final TimeUnit timeUnit;

    public PeriodicUpdateEngine(final Bootstrapper<D> bootstrapper,
                                final ItemConsumer<D> itemConsumer,
                                final int delay,
                                final TimeUnit timeUnit) {
        this(bootstrapper, itemConsumer, delay, timeUnit, new LoggingErrorHandler<>(PeriodicUpdateEngine.class));
    }

    /**
     * @param bootstrapper bootstrapper of data items
     * @param itemConsumer item consumer
     * @param delay        delay between each bootstrap (not guaranteed if the bootstrap operation itself, takes more
     *                     time)
     * @param timeUnit     time unit of the delay specified above
     * @param errorHandler a handler for errors when individual items are being consumed
     */
    public PeriodicUpdateEngine(final Bootstrapper<D> bootstrapper,
                                final ItemConsumer<D> itemConsumer,
                                final int delay,
                                final TimeUnit timeUnit,
                                final ErrorHandler<D> errorHandler) {
        super(bootstrapper, itemConsumer, errorHandler);
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                bootstrap();
            } catch (Exception e) {
                log.error("Error while doing bootstrap", e);
            }
        }, 0, delay, timeUnit);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}
