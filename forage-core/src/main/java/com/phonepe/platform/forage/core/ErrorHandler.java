package com.phonepe.platform.forage.core;

/**
 * a handler when exceptions occurs when handling an item
 *
 * @param <T> type of item
 */
public interface ErrorHandler<T> {
    void handleError(T t, Exception e);
}
