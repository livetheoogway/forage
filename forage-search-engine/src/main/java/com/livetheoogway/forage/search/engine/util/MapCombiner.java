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

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * A class to combine maps into a base initial map
 * This isn't thread safe
 *
 * @author tushar.naik
 * @version 1.0  2019-04-16 - 23:54
 */
@NoArgsConstructor
@AllArgsConstructor
public class MapCombiner<K, V> {

    /* initial map */
    private Map<K, V> baseMap = Maps.newHashMap();

    /**
     * using the combiner function, combine all key values of the baseMap
     *
     * @param mapToBeConsumed  new map to be combined
     * @param combinerFunction combiner function for all values with same key between the two maps
     */
    public void combine(Map<K, V> mapToBeConsumed, BinaryOperator<V> combinerFunction) {
        mapToBeConsumed.forEach((key, value) -> {
            if (!baseMap.containsKey(key)) {
                baseMap.put(key, value);
            } else {
                V v = baseMap.get(key);
                baseMap.put(key, combinerFunction.apply(v, value));
            }
        });
    }

    /**
     * @return a copy of the current state of the base map
     */
    public Map<K, V> currentCombinedMap() {
        return new HashMap<>(baseMap);
    }
}
