package com.livetheoogway.forage.search.engine.store;


/**
 * A simple key value store
 * @param <T>
 */
public interface Store<T> {
    T get(String id);
}
