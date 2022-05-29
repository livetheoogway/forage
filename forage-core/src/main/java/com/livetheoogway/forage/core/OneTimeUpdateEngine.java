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
