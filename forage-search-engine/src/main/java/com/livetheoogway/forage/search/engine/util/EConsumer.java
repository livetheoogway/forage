package com.livetheoogway.forage.search.engine.util;

@FunctionalInterface
public interface EConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws Exception;
}
