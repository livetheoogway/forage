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

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Registry to store field-level boost values for query-time application.
 * Since Lucene 9.x deprecated index-time field boosting, we store boost
 * values separately and apply them during query generation.
 */
public class FieldBoostRegistry {
    
    private final Map<String, Float> fieldBoosts = new ConcurrentHashMap<>();
    
    /**
     * Register a field boost value
     */
    public void registerFieldBoost(String fieldName, Float boost) {
        if (boost != null && boost != 1.0f) {
            fieldBoosts.put(fieldName, boost);
        }
    }
    
    /**
     * Get the boost value for a field, returns 1.0f if not found
     */
    public float getFieldBoost(String fieldName) {
        return fieldBoosts.getOrDefault(fieldName, 1.0f);
    }
    
    /**
     * Check if a field has a custom boost
     */
    public boolean hasFieldBoost(String fieldName) {
        return fieldBoosts.containsKey(fieldName);
    }
    
    /**
     * Clear all field boosts
     */
    public void clear() {
        fieldBoosts.clear();
    }
}