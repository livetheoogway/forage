package com.livetheoogway.forage.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LoggingErrorHandler<T> implements ErrorHandler<T> {
    private Class<?> callerClass;

    @Override
    public void handleError(final T o, final Exception e) {
        log.error("[{}] Error during execution for item: {}", callerClass.getSimpleName(), o, e);
    }
}
