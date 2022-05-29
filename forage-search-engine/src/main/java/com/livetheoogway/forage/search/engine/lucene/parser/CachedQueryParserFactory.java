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

package com.livetheoogway.forage.search.engine.lucene.parser;

import com.livetheoogway.forage.search.engine.util.MaxSizeHashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.Map;

public class CachedQueryParserFactory extends QueryParserFactory {
    private final Map<String, QueryParser> cache;

    public CachedQueryParserFactory(final Analyzer analyzer, int maxSize) {
        super(analyzer);
        cache = new MaxSizeHashMap<>(maxSize);
    }

    @Override
    public QueryParser queryParser(final String field) {
        cache.computeIfAbsent(field, super::queryParser);
        return cache.get(field);
    }
}
