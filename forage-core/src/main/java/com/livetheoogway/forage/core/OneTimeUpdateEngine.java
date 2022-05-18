package com.livetheoogway.forage.core;

import com.livetheoogway.forage.models.StoredData;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a type of Update engine, that only does the bootstrap process during start up
 * Use this class, if you do not wish to periodically perform the bootstrap process at regular intervals
 */
@Slf4j
public class OneTimeUpdateEngine<D, S extends StoredData<D>> extends UpdateEngine<D, S> {

    public OneTimeUpdateEngine(final Bootstrapper<D, S> bootstrapper,
                               final ItemConsumer<S> itemConsumer) {
        this(bootstrapper, itemConsumer, new LoggingErrorHandler<>(OneTimeUpdateEngine.class));
    }

    public OneTimeUpdateEngine(final Bootstrapper<D, S> bootstrapper,
                               final ItemConsumer<S> itemConsumer,
                               final ErrorHandler<S> errorHandler) {
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
