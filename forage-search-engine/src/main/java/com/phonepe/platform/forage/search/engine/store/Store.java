package com.phonepe.platform.forage.search.engine.store;

import com.phonepe.platform.forage.search.engine.model.store.Storable;

import java.util.List;

/**
 * A simple key value store
 * @param <T>
 */
public interface Store<T> {
    void store(Storable<T> storable);

    void store(List<Storable<T>> storables);

    T get(String id);

    void cleanup();
}
