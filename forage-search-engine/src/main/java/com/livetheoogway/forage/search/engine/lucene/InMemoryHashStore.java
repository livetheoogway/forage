package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.search.engine.store.Store;

import java.util.HashMap;
import java.util.Map;

class InMemoryHashStore<T> implements Store<T> {
    private Map<String, T> hashMap;

    public InMemoryHashStore() {
        this.hashMap = new HashMap<>();
    }
//
//    @Override
//    public void store(final StoredData<T> storedData) {
//        hashMap.putIfAbsent(storedData.id(), storedData.data());
//    }
//
//    @Override
//    public void store(final List<StoredData<T>> storedData) {
//        storedData.forEach(this::store);
//    }

    @Override
    public T get(final String id) {
        return hashMap.get(id);
    }
//
//    @Override
//    public void cleanup() {
//        hashMap = new HashMap<>();
//    }
}
