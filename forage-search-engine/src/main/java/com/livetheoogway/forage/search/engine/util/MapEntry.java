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

package com.livetheoogway.forage.search.engine.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Use this in the streaming functions
 *
 * @author tushar.naik
 * @version 1.0  2019-04-16 - 22:50
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MapEntry<K, V> {
    private K k;
    private V v;

    public static <K, V> MapEntry<K, V> of(K k, V v) {
        return new MapEntry<>(k, v);
    }

    /**
     * A collector to convert into a map (stream)
     *
     * @param <K> key
     * @param <V> value
     * @return collector
     */
    public static <K, V> Collector<MapEntry<K, V>, ?, Map<K, V>> mapCollector() {
        return Collectors.toMap(MapEntry::getK, MapEntry::getV, (v1, v2) -> v2);
    }

    public static <K, V, M extends Map<K, V>> Collector<MapEntry<K, V>, ?, M> mapCollector(Supplier<M> mapSupplier) {
        return Collectors.toMap(MapEntry::getK, MapEntry::getV, (v1, v2) -> v2, mapSupplier);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> mapEntryCollector() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}