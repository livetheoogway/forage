package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.models.StoredData;
import com.phonepe.platform.forage.search.engine.store.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ObjectStore implements Store<Object> {
    private Map<String, Object> hashMap;

    public ObjectStore() {
        this.hashMap = new HashMap<>();
    }

    @Override
    public void store(final StoredData<Object> storedData) {
        hashMap.putIfAbsent(storedData.id(), storedData.data());
    }

    @Override
    public void store(final List<StoredData<Object>> storedData) {
        storedData.forEach(this::store);
    }

    @Override
    public Object get(final String id) {
        return hashMap.get(id);
    }

    @Override
    public void cleanup() {
        hashMap = new HashMap<>();
    }
}
