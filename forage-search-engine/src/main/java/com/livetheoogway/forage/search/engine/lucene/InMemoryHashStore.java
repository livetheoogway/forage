package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.DataId;
import com.livetheoogway.forage.search.engine.store.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InMemoryHashStore<T extends DataId> implements Store<T> {
    private Map<String, T> hashMap;

    public InMemoryHashStore() {
        this.hashMap = new HashMap<>();
    }

    public void store(final T data) {
        hashMap.putIfAbsent(data.id(), data);
    }

    public void store(final List<T> dataItems) {
        dataItems.forEach(this::store);
    }

    @Override
    public T get(final String id) {
        return hashMap.get(id);
    }

    public void cleanup() {
        hashMap = new HashMap<>();
    }
}
