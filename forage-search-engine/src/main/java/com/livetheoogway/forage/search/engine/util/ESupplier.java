package com.livetheoogway.forage.search.engine.util;

/**
 * A Supplier that could throw an exception
 *
 * @param <T>
 */
@FunctionalInterface
public interface ESupplier<T> {
    /**
     * Gets a result, could be throwing an exception.
     *
     * @return a result
     */
    @SuppressWarnings("java:S112")
    T get() throws Exception;
}

