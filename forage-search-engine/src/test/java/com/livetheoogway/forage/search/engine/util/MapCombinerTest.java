/*
 * Copyright 2026. Live the Oogway, Tushar Naik
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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapCombinerTest {

    @Test
    void testDefaultConstructor_createsEmptyBaseMap() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();
        Map<String, Integer> result = combiner.currentCombinedMap();
        assertTrue(result.isEmpty());
    }

    @Test
    void testConstructorWithBaseMap_initializesWithProvidedMap() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        initialMap.put("key2", 20);

        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(2, result.size());
        assertEquals(10, result.get("key1"));
        assertEquals(20, result.get("key2"));
    }

    @Test
    void testCombine_newKeysOnly_addsAllToBaseMap() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();
        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 10);
        newMap.put("key2", 20);

        combiner.combine(newMap, Integer::sum);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(2, result.size());
        assertEquals(10, result.get("key1"));
        assertEquals(20, result.get("key2"));
    }

    @Test
    void testCombine_overlappingKeys_appliesCombinerFunction() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        initialMap.put("key2", 20);
        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);

        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 5);
        newMap.put("key3", 30);

        combiner.combine(newMap, Integer::sum);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(3, result.size());
        assertEquals(15, result.get("key1"));
        assertEquals(20, result.get("key2"));
        assertEquals(30, result.get("key3"));
    }

    @Test
    void testCombine_multipleCombines_accumulatesResults() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();

        Map<String, Integer> firstMap = Maps.newHashMap();
        firstMap.put("key1", 10);
        firstMap.put("key2", 20);

        Map<String, Integer> secondMap = Maps.newHashMap();
        secondMap.put("key1", 5);
        secondMap.put("key3", 30);

        Map<String, Integer> thirdMap = Maps.newHashMap();
        thirdMap.put("key1", 3);
        thirdMap.put("key2", 10);
        thirdMap.put("key4", 40);

        combiner.combine(firstMap, Integer::sum);
        combiner.combine(secondMap, Integer::sum);
        combiner.combine(thirdMap, Integer::sum);

        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(4, result.size());
        assertEquals(18, result.get("key1"));
        assertEquals(30, result.get("key2"));
        assertEquals(30, result.get("key3"));
        assertEquals(40, result.get("key4"));
    }

    @Test
    void testCombine_withDifferentCombinerFunction_takesMaxValue() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        initialMap.put("key2", 20);
        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);

        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 15);
        newMap.put("key2", 5);
        newMap.put("key3", 30);

        combiner.combine(newMap, Math::max);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(3, result.size());
        assertEquals(15, result.get("key1"));
        assertEquals(20, result.get("key2"));
        assertEquals(30, result.get("key3"));
    }

    @Test
    void testCombine_withStringConcatenation() {
        MapCombiner<String, String> combiner = new MapCombiner<>();

        Map<String, String> firstMap = Maps.newHashMap();
        firstMap.put("key1", "Hello");
        firstMap.put("key2", "World");

        Map<String, String> secondMap = Maps.newHashMap();
        secondMap.put("key1", " there");
        secondMap.put("key3", "!");

        combiner.combine(firstMap, String::concat);
        combiner.combine(secondMap, String::concat);

        Map<String, String> result = combiner.currentCombinedMap();

        assertEquals(3, result.size());
        assertEquals("Hello there", result.get("key1"));
        assertEquals("World", result.get("key2"));
        assertEquals("!", result.get("key3"));
    }

    @Test
    void testCombine_emptyMapToBeConsumed_noChangeToBaseMap() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);

        combiner.combine(new HashMap<>(), Integer::sum);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(1, result.size());
        assertEquals(10, result.get("key1"));
    }

    @Test
    void testCombine_combineIntoEmptyBase_createsNewEntries() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();

        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 10);
        newMap.put("key2", 20);
        newMap.put("key3", 30);

        combiner.combine(newMap, Integer::sum);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(3, result.size());
        assertEquals(10, result.get("key1"));
        assertEquals(20, result.get("key2"));
        assertEquals(30, result.get("key3"));
    }

    @Test
    void testCurrentCombinedMap_returnsNewInstance() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();
        Map<String, Integer> newMap = Maps.newHashMap();
        newMap.put("key1", 10);
        combiner.combine(newMap, Integer::sum);

        Map<String, Integer> result1 = combiner.currentCombinedMap();
        Map<String, Integer> result2 = combiner.currentCombinedMap();

        assertNotSame(result1, result2);
        assertEquals(result1, result2);
    }

    @Test
    void testCurrentCombinedMap_modifyingReturnedMapDoesNotAffectBaseMap() {
        MapCombiner<String, Integer> combiner = new MapCombiner<>();
        Map<String, Integer> newMap = Maps.newHashMap();
        newMap.put("key1", 10);
        combiner.combine(newMap, Integer::sum);

        Map<String, Integer> result = combiner.currentCombinedMap();
        result.put("key2", 20);
        result.put("key1", 100);

        Map<String, Integer> freshResult = combiner.currentCombinedMap();
        assertEquals(1, freshResult.size());
        assertEquals(10, freshResult.get("key1"));
        assertFalse(freshResult.containsKey("key2"));
    }

    @Test
    void testCombine_customCombinerFunction_replacesWithNewValue() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);

        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 99);

        combiner.combine(newMap, (oldVal, newVal) -> newVal);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(99, result.get("key1"));
    }

    @Test
    void testCombine_customCombinerFunction_keepsOldValue() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("key1", 10);
        MapCombiner<String, Integer> combiner = new MapCombiner<>(initialMap);

        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key1", 99);

        combiner.combine(newMap, (oldVal, newVal) -> oldVal);
        Map<String, Integer> result = combiner.currentCombinedMap();

        assertEquals(10, result.get("key1"));
    }
}

