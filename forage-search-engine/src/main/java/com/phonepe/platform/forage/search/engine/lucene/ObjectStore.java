package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.search.engine.model.store.Storable;
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
    public void store(final Storable<Object> storable) {
        hashMap.putIfAbsent(storable.id(), storable.data());
    }

    @Override
    public void store(final List<Storable<Object>> storables) {
        storables.forEach(this::store);
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
