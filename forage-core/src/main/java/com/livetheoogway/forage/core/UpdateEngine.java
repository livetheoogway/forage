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

/**
 * This is the main update engine that is responsible for exposing the boostrap function
 *
 * @param <D> Data that can be referenced with an id
 */
@SuppressWarnings("java:S112")
@Slf4j
public abstract class UpdateEngine<D extends DataId> {
    private final Bootstrapper<D> bootstrapper;
    private final ItemConsumer<D> itemConsumer;
    private final ErrorHandler<D> errorHandler;

    protected UpdateEngine(final Bootstrapper<D> bootstrapper,
                           final ItemConsumer<D> itemConsumer,
                           final ErrorHandler<D> errorHandler) {
        this.bootstrapper = bootstrapper;
        this.itemConsumer = itemConsumer;
        this.errorHandler = errorHandler;
    }

    /**
     * the primary function that is supposed to bootstrap all items into the consumer
     */
    public void bootstrap() throws Exception {
        log.info("[forage] Bootstrapping forage ...");
        itemConsumer.init();
        bootstrapper.bootstrap(item -> {
            try {
                itemConsumer.consume(item);
            } catch (Exception e) {
                errorHandler.handleError(item, e);
            }
        });
        itemConsumer.finish();
        log.info("[forage] ... Bootstrapping forage done");
    }

    /**
     * start and operations of boostrap (eg: schedule any thread to bootstrap at regular intervals)
     *
     * @throws Exception if there was an issue during start-up
     */
    public abstract void start() throws Exception;

    /**
     * stop resources if any
     *
     * @throws Exception if there was an issue during stop
     */
    public abstract void stop() throws Exception;
}
