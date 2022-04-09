package com.phonepe.platform.forage.search.engine.store;


import com.phonepe.platform.forage.models.StoredData;

import java.util.List;

/**
 * A simple key value store
 * @param <T>
 */
public interface Store<T> {
    void store(StoredData<T> storedData);

    void store(List<StoredData<T>> storedData);

    T get(String id);

    void cleanup();
}
