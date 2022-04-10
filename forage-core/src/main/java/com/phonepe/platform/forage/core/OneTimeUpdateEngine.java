package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.models.StoredData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneTimeUpdateEngine<T, D extends StoredData<T>> extends UpdateEngine<T, D> {

    private final ErrorHandler errorHandler;

    public OneTimeUpdateEngine(final Bootstrapper<T, D> bootstrapper,
                               final UpdateConsumer<D> updateConsumer) {
        this(bootstrapper, updateConsumer, new LoggingErrorHandler(OneTimeUpdateEngine.class));
    }

    public OneTimeUpdateEngine(final Bootstrapper<T, D> bootstrapper,
                               final UpdateConsumer<D> updateConsumer,
                               final ErrorHandler errorHandler) {
        super(bootstrapper, updateConsumer);
        this.errorHandler = errorHandler;
    }

    @Override
    public void start() {
        try {
            bootstrap();
        } catch (Exception e) {
            errorHandler.handleError(e);
        }
    }

    @Override
    public void stop() {
        /* nothing to do here */
    }
}
