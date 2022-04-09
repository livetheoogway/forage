package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.models.StoredData;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PeriodicUpdateEngine<T, D extends StoredData<T>> extends UpdateEngine<T, D> {

    private final ScheduledExecutorService executorService;
    private final ErrorHandler errorHandler;
    private final int delay;
    private final TimeUnit timeUnit;

    public PeriodicUpdateEngine(final Bootstrapper<T, D> bootstrapper,
                                final UpdateConsumer<D> updateConsumer,
                                final int delay,
                                final TimeUnit timeUnit) {
        this(bootstrapper, updateConsumer, delay, timeUnit, new LoggingErrorHandler(PeriodicUpdateEngine.class));
    }

    public PeriodicUpdateEngine(final Bootstrapper<T, D> bootstrapper,
                                final UpdateConsumer<D> updateConsumer,
                                final int delay,
                                final TimeUnit timeUnit,
                                final ErrorHandler errorHandler) {
        super(bootstrapper, updateConsumer);
        this.errorHandler = errorHandler;
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                bootstrapper.bootstrap(updateConsumer);
            } catch (Exception e) {
                errorHandler.handleError(e);
            }
        }, 0, delay, timeUnit);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}
