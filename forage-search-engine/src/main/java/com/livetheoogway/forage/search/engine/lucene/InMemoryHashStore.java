/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

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
