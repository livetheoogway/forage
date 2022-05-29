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
 * This is a type of Update engine, that only does the bootstrap process during start up
 * Use this class, if you do not wish to periodically perform the bootstrap process at regular intervals
 */
@Slf4j
public class OneTimeUpdateEngine<D extends DataId> extends UpdateEngine<D> {

    public OneTimeUpdateEngine(final Bootstrapper<D> bootstrapper,
                               final ItemConsumer<D> itemConsumer) {
        this(bootstrapper, itemConsumer, new LoggingErrorHandler<>(OneTimeUpdateEngine.class));
    }

    public OneTimeUpdateEngine(final Bootstrapper<D> bootstrapper,
                               final ItemConsumer<D> itemConsumer,
                               final ErrorHandler<D> errorHandler) {
        super(bootstrapper, itemConsumer, errorHandler);
    }

    @Override
    public void start() throws Exception {
        bootstrap();
    }

    @Override
    public void stop() {
        /* nothing to do here */
    }
}
