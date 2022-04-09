package com.phonepe.platform.forage.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LoggingErrorHandler implements ErrorHandler {
    private Class<?> callerClass;

    @Override
    public void handleError(final Exception e) {
        log.error("Error during execution" + callerClass.getSimpleName(), e);
    }
}
