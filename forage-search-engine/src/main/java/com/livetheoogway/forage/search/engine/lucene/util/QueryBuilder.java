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

package com.livetheoogway.forage.search.engine.lucene.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryBuilder {

    public Builders.InnerBooleanQueryBuilder booleanQuery() {
        return new Builders.InnerBooleanQueryBuilder();
    }

    public Builders.InnerMatchQueryBuilder matchQuery(String field, String value) {
        return new Builders.InnerMatchQueryBuilder(field, value);
    }

    public Builders.InnerFuzzyMatchQueryBuilder fuzzyMatchQuery(String field, String value) {
        return new Builders.InnerFuzzyMatchQueryBuilder(field, value);
    }

    public Builders.InnerIntRangeQueryBuilder intRangeQuery(String field, int low, int high) {
        return new Builders.InnerIntRangeQueryBuilder(field, low, high);
    }

    public Builders.InnerFloatRangeQueryBuilder floatRangeQuery(String field, float low, float high) {
        return new Builders.InnerFloatRangeQueryBuilder(field, low, high);
    }
}
